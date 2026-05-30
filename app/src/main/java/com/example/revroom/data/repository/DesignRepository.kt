package com.example.revroom.data.repository

import android.content.Context
import android.webkit.MimeTypeMap
import com.example.revroom.core.network.ApiClient
import com.example.revroom.core.utils.ImageCompressor
import com.example.revroom.data.local.LocalUserIdProvider
import com.example.revroom.data.remote.AuthApi
import com.example.revroom.data.remote.DesignApi
import com.example.revroom.data.remote.DesignResponse
import com.example.revroom.data.remote.DesignStatusResponse
import com.example.revroom.data.remote.RegisterDeviceRequest
import com.example.revroom.features.design_studio.model.DesignJobStatus
import com.example.revroom.features.design_studio.model.DesignRequest
import com.example.revroom.features.design_studio.model.DesignResult
import com.example.revroom.features.design_studio.model.DesignStyle
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class DesignRepository(
    private val context: Context,
    private val designApi: DesignApi = ApiClient.designApi,
    private val authApi: AuthApi = ApiClient.authApi,
    private val userIdProvider: LocalUserIdProvider = LocalUserIdProvider(context),
) {
    private var registeredUserId: String? = null

    suspend fun uploadDesign(request: DesignRequest): Result<DesignResult> {
        return runApiCall {
            val userId = ensureRegisteredUser()
            val imageBytes = ImageCompressor.readAndCompressIfNeeded(context, request.imageUri)
            val contentType = context.contentResolver.getType(request.imageUri) ?: "image/jpeg"
            val imagePart = createImagePart(imageBytes, contentType)
            val styleIdPart = request.styleId.toString().toRequestBody("text/plain".toMediaType())
            val roomTypePart = request.roomType
                ?.takeIf { it.isNotBlank() }
                ?.toRequestBody("text/plain".toMediaType())

            designApi.analyzeDesign(userId, imagePart, styleIdPart, roomTypePart).toDesignResult()
        }
    }

    suspend fun getDesignStyles(): Result<List<DesignStyle>> {
        return runApiCall {
            designApi.getDesignStyles().map { style ->
                DesignStyle(
                    styleId = style.styleId,
                    styleName = style.styleName,
                    coreAesthetic = style.coreAesthetic ?: "",
                    lightingOptions = style.lightingOptions ?: emptyList(),
                    materialOptions = style.materialOptions ?: emptyList(),
                    colorRuleOptions = style.colorRuleOptions ?: emptyList(),
                    atmosphereOptions = style.atmosphereOptions ?: emptyList()
                )
            }
        }
    }

    suspend fun getDesignStatus(designId: String): Result<DesignResult> {
        return runApiCall {
            val userId = ensureRegisteredUser()
            designApi.getDesignStatus(userId, designId).toDesignResult()
        }
    }

    private suspend fun ensureRegisteredUser(): String {
        registeredUserId?.let { return it }

        val userId = userIdProvider.getOrCreateUserId()
        authApi.registerDevice(RegisterDeviceRequest(userId = userId, name = "Revroom User"))
        registeredUserId = userId
        return userId
    }

    private fun createImagePart(imageBytes: ByteArray, contentType: String): MultipartBody.Part {
        val requestBody = imageBytes.toRequestBody(contentType.toMediaTypeOrNull())
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType) ?: "jpg"
        return MultipartBody.Part.createFormData("image", "room-upload.$extension", requestBody)
    }

    private suspend fun <T> runApiCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (error: HttpException) {
            Result.failure(DesignRepositoryException(parseHttpError(error)))
        } catch (_: IOException) {
            Result.failure(DesignRepositoryException("Không thể kết nối máy chủ."))
        } catch (error: Exception) {
            Result.failure(DesignRepositoryException(error.message ?: "Đã có lỗi xảy ra."))
        }
    }

    private fun parseHttpError(error: HttpException): String {
        val errorBody = error.response()?.errorBody()?.string()
        if (!errorBody.isNullOrBlank()) {
            val message = runCatching { JSONObject(errorBody).optString("message") }.getOrNull()
            if (!message.isNullOrBlank()) {
                return message
            }
        }

        return "Yêu cầu thất bại (${error.code()})."
    }

    private fun DesignResponse.toDesignResult(): DesignResult {
        return DesignResult(
            designId = designId,
            status = DesignJobStatus.fromApiValue(status),
            originalImageUrl = originalImageUrl,
            designedImageUrl = null,
            errorMessage = null
        )
    }

    private fun DesignStatusResponse.toDesignResult(): DesignResult {
        return DesignResult(
            designId = designId,
            status = DesignJobStatus.fromApiValue(status),
            originalImageUrl = originalImageUrl,
            designedImageUrl = designedImageUrl,
            errorMessage = errorMessage
        )
    }
}

class DesignRepositoryException(message: String) : Exception(message)
