package com.example.revroom.features.auth.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revroom.BuildConfig
import com.example.revroom.core.network.ApiClient
import com.example.revroom.core.utils.FileUtils
import com.example.revroom.data.remote.RegisterDeviceRequest
import com.example.revroom.data.remote.UpdateNameRequest
import com.example.revroom.data.local.UserManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthViewModel : ViewModel() {

    // --- QUẢN LÝ TRẠNG THÁI UI ---
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var isUploading by mutableStateOf(value = false)
        private set

    var avatarUrl by mutableStateOf<String?>(null)
        private set

    var userName by mutableStateOf<String?>(null)
        private set

    // --- CÁC HÀM XỬ LÝ LOGIC ---
    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    fun sendIdToServer(userManager: UserManager, userId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.authApi.registerDevice(RegisterDeviceRequest(userId))
                if (response.isSuccessful) {
                    Log.d("API_TEST", "Thành công! Đã bắn ID: $userId lên C#")
                    // NẾU DÒNG NÀY VẪN BÁO ĐỎ THÌ TẠM THỜI COMMENT NÓ LẠI NHÉ (Thêm // ở đầu dòng)
                    // userManager.isRegisteredOnServer = true
                } else {
                    Log.e("API_TEST", "Lỗi từ C#: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Mất mạng hoặc Backend chưa bật: ${e.message}")
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.authApi.getProfile(userId)
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        userName = profile.name
                        val rawFileName = profile.avatarUrl
                        if (!rawFileName.isNullOrEmpty()) {
                            avatarUrl = "${BuildConfig.API_BASE_URL}uploads/$rawFileName"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Lỗi kéo Profile: ${e.message}")
            }
        }
    }

    fun uploadImage(context: Context, userId: String) {
        val uri = selectedImageUri ?: return
        viewModelScope.launch {
            isUploading = true
            try {
                val file = FileUtils.uriToFile(context, uri)
                if (file == null) {
                    isUploading = false
                    return@launch
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = ApiClient.authApi.uploadAvatar(userIdBody, body)
                if (response.isSuccessful) {
                    selectedImageUri = null
                    loadUserProfile(userId)
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Sập mạng khi up ảnh: ${e.message}")
            } finally {
                isUploading = false
            }
        }
    }

    fun updateUserProfile(context: Context, userId: String, newName: String) {
        viewModelScope.launch {
            isUploading = true
            try {
                val response = ApiClient.authApi.updateName(UpdateNameRequest(userId, newName))
                if (response.isSuccessful) {
                    userName = newName
                    if (selectedImageUri != null) {
                        uploadImage(context, userId)
                    } else {
                        isUploading = false
                        Toast.makeText(context, "Cập nhật Profile thành công!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    isUploading = false
                    Toast.makeText(context, "Lỗi cập nhật tên!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isUploading = false
            }
        }
    }
}