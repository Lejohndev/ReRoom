package com.example.revroom.features.auth.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.revroom.data.local.UserManager
import com.example.revroom.features.auth.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    userManager: UserManager,
    authViewModel: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    val userId = remember(userManager) { userManager.getDeviceId() }

    // Đăng ký Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        authViewModel.onImageSelected(uri)
    }

    // Tự động gọi API đòi ảnh khi mở màn hình
    LaunchedEffect(key1 = userId) {
        authViewModel.loadUserProfile(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // --- KHU VỰC AVATAR ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val selectedUri = authViewModel.selectedImageUri
            val currentAvatarUrl = authViewModel.avatarUrl

            if (selectedUri != null) {
                // Ưu tiên 1: Ảnh đang chọn tạm
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (!currentAvatarUrl.isNullOrEmpty()) {
                // Ưu tiên 2: Ảnh từ server
                AsyncImage(
                    model = currentAvatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Ưu tiên 3: Icon mặc định
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Nhấn vào ảnh để thay đổi", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        if (authViewModel.selectedImageUri != null) {
            Button(
                onClick = { authViewModel.uploadImage(context, userId) },
                enabled = !authViewModel.isUploading
            ) {
                if (authViewModel.isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("LƯU ẢNH ĐẠI DIỆN")
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- KHU VỰC HIỂN THỊ ID ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "USER SETTINGS", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = userId, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
