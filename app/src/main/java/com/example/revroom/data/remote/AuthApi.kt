package com.example.revroom.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): UserProfileResponse
}

data class RegisterDeviceRequest(
    val userId: String,
    val name: String? = null
)

data class UserProfileResponse(
    val userId: String,
    val name: String?,
    val avatarUrl: String?,
    val createdAt: String
)
