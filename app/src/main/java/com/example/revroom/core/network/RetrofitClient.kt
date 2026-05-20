package com.example.revroom.core.network

import com.example.revroom.BuildConfig
import com.example.revroom.data.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java) // Thiếu dòng này là không gọi API được đâu nhé!
    }
}
