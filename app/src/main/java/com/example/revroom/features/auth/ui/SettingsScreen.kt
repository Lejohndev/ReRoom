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
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Logic lấy ID vẫn có thể để ở đây vì nó gắn liền với UserManager truyền vào
    val userId = remember(userManager) { userManager.getDeviceId() }

    // 👉 (PHÉP THUẬT 1) TRIGGER KHI MỞ MÀN HÌNH:
    // Vừa vào app phát là sai ViewModel chạy xuống C# đòi ảnh luôn
    LaunchedEffect(key1 = userId) {
        authViewModel.loadUserProfile(userId)
    }

    // Đăng ký Photo Picker (Bắt buộc phải khai báo ở tầng UI)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // Chỉ đẩy dữ liệu sang ViewModel, không xử lý logic ở đây
        authViewModel.onImageSelected(uri)
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
                    // Mở thư viện chọn ảnh
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val selectedUri = authViewModel.selectedImageUri
            val networkAvatar = authViewModel.avatarUrl

            // 👉 (PHÉP THUẬT 2) LOGIC HIỂN THỊ CHUẨN UX:
            if (selectedUri != null) {
                // Ưu tiên 1: Đang chọn ảnh dở (chưa bấm lưu), thì hiện ảnh tạm cho xem trước
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (!networkAvatar.isNullOrEmpty()) {
                // Ưu tiên 2: Không có ảnh tạm thì hiện ảnh chính thức load từ server về
                AsyncImage(
                    model = networkAvatar,
                    contentDescription = "Network Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Ưu tiên 3: Mới dùng app, trắng trơn thì hiện icon xám
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

        // Nút Lưu ảnh (Chỉ hiện khi đã chọn ảnh và không đang upload)
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