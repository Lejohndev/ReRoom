package com.example.revroom.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

// --- CÁC CLASS ĐÓNG GÓI DỮ LIỆU ---

data class RegisterDeviceRequest(
    val userId: String,
    val name: String? = null,
)

data class UserProfileResponse(
    val userId: String,
    val name: String?,
    val avatarUrl: String?,
    val createdAt: String?,
)

// 🔥 THÊM CÁI NÀY: Class để gói ID và Tên mới ném lên Server
data class UpdateNameRequest(
    val userId: String,
    val newName: String
)

// --- INTERFACE GỌI API ---

interface AuthApi {
    @POST("api/Auth/register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<Any>

    @Multipart
    @POST("api/Auth/upload-avatar")
    suspend fun uploadAvatar(
        @Part("userId") userId: RequestBody,
        @Part file: MultipartBody.Part,
    ): Response<Any>

    @GET("api/Auth/profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<UserProfileResponse>

    // 🔥 THÊM CÁI NÀY: Khai báo cổng API update tên
    @PUT("api/Auth/update-name")
    suspend fun updateName(@Body request: UpdateNameRequest): Response<Any>
}