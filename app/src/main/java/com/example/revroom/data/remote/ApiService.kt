package com.example.revroom.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// 1. Tạo các cục hàng để gửi đi / nhận về
data class DeviceRequest(val userId: String)

data class UserProfileResponse(
    val userId: String,
    val name: String?,
    val avatarUrl: String?,
    val createdAt: String?
)

// 2. Khai báo cổng API (CHỈ CÓ 1 INTERFACE NÀY THÔI NHÉ)
interface ApiService {

    // Bắn ID
    @POST("api/Auth/register-device")
    suspend fun registerDevice(@Body request: DeviceRequest): Response<Any>

    // Bắn Ảnh
    @Multipart
    @POST("api/Auth/upload-avatar")
    suspend fun uploadAvatar(
        @Part("userId") userId: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Any>

    // Kéo thông tin User (kèm link ảnh) về
    @GET("api/Auth/profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<UserProfileResponse>
}