package com.example.revroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.revroom.core.theme.RevroomTheme
import com.example.revroom.data.local.UserManager
import com.example.revroom.features.auth.viewmodel.AuthViewModel
import com.example.revroom.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Khởi tạo đồ nghề Backend
        val userManager = UserManager(this)
        val authViewModel = AuthViewModel()
        val userId = userManager.getDeviceId()

        // 2. Logic kiểm tra người mới -> Bắn ID
        if (userManager.isNewUser) {
            authViewModel.sendIdToServer(userManager, userId)
            userManager.isNewUser = false
        }

        // 3. Khởi chạy Giao diện
        enableEdgeToEdge()
        setContent {
            RevroomTheme {
                // Trả lại sân khấu cho AppNavigation của team chạy.
                AppNavigation()
            }
        }
    }
}
