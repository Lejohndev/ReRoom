package com.example.revroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier

// 1. Import Theme đã sửa chuẩn
import com.example.revroom.core.theme.RevroomTheme

// 2. Import đường dẫn chuẩn của 2 file m vừa di chuyển
import com.example.revroom.data.local.UserManager
import com.example.revroom.features.auth.ui.SettingsScreen

import com.example.revroom.features.auth.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo UserManager
        val userManager = UserManager(this)
        val authViewModel = AuthViewModel() // Gọi bộ não ra

        val userId = userManager.getDeviceId() // Móc ID ra

        // NẾU LÀ NGƯỜI DÙNG MỚI -> BẮN API!
        if (userManager.isNewUser) {
            authViewModel.sendIdToServer(userId)
            userManager.isNewUser = false // Bắn xong thì tắt cờ đi
        }

        enableEdgeToEdge()
        setContent {
            RevroomTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        // Ép chạy thẳng màn hình của m luôn, dẹp AppNavigation qua một bên
                        SettingsScreen(userManager = userManager)

                    }
                }
            }
        }
    }
}