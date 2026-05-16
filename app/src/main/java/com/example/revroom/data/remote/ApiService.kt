package com.example.revroom.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// 1. Tạo cục hàng để gửi đi (Biến userId phải viết hoa chữ cái đầu hay không phụ thuộc vào code C# của m, thường C# mặc định nhận camelCase)
data class DeviceRequest(val userId: String)

// 2. Khai báo cổng API
interface ApiService {
    @POST("api/Auth/register-device")
    suspend fun registerDevice(@Body request: DeviceRequest): Response<Any>
}