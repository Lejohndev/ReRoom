package com.example.revroom.features.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revroom.core.network.RetrofitClient
import com.example.revroom.data.remote.DeviceRequest
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    fun sendIdToServer(userId: String) {
        viewModelScope.launch {
            try {
                // Gọi sang đường ống nước
                val response = RetrofitClient.apiService.registerDevice(DeviceRequest(userId))

                if (response.isSuccessful) {
                    Log.d("API_TEST", "Thành công! Đã bắn ID: $userId lên C#")
                } else {
                    Log.e("API_TEST", "Lỗi từ C#: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Mất mạng hoặc Backend chưa bật: ${e.message}")
            }
        }
    }
}