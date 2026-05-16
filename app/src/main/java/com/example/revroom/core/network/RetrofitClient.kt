package com.example.revroom.core.network

import com.example.revroom.data.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Sửa số 5000 thành cái Port C# của m (Cái lúc m chạy dotnet run nó báo Now listening on: http://localhost:xxxx ấy)
    private const val BASE_URL = "http://10.0.2.2:5207/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}