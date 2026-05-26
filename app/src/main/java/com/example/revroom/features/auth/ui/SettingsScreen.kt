package com.example.revroom.features.auth.ui

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // State quản lý chữ trong ô nhập liệu dưới Local màn hình
    var userNameInput by remember { mutableStateOf("") }

    // Lấy tên chuẩn hiện tại đang có trên Server thông qua ViewModel
    val currentNameFromServer = authViewModel.userName

    // Đăng ký Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        authViewModel.onImageSelected(uri)
    }

    // Tự động gọi API đòi dữ liệu khi mở màn hình
    LaunchedEffect(key1 = userId) {
        authViewModel.loadUserProfile(userId)
    }

    // 🔥 BÙA ĐỒNG BỘ: Cứ khi nào cục currentNameFromServer đổi màu (API trả về kết quả)
    // là lập tức đập cái tên đó vào ô nhập liệu cho user thấy!
    LaunchedEffect(key1 = currentNameFromServer) {
        currentNameFromServer?.let {
            userNameInput = it
        }
    }

    // Kiểm tra xem User có sửa đổi gì không để quyết định hiện nút Lưu
    val isNameChanged = userNameInput != (currentNameFromServer ?: "")
    val shouldShowSaveButton = authViewModel.selectedImageUri != null || isNameChanged

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF3E5F5), Color(0xFFFFEBEE)) // Nền hường sang trọng
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

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
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!currentAvatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = currentAvatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Avatar",
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Nhấn vào ảnh để thay đổi", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // --- KHU VỰC SỬA TÊN USER  ---
            OutlinedTextField(
                value = userNameInput,
                onValueChange = { userNameInput = it },
                label = { Text("User Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6C63FF),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- NÚT LƯU THAY ĐỔI (Chỉ hiện khi có sửa đổi tên hoặc ảnh mới) ---
            if (shouldShowSaveButton) {
                Button(
                    onClick = { authViewModel.updateUserProfile(context, userId, userNameInput) },
                    enabled = !authViewModel.isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                ) {
                    if (authViewModel.isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("LƯU THAY ĐỔI")
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- KHU VỰC HIỂN THỊ ID ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Device ID", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = userId, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- APP VERSION (Hiện dưới ID) ---
            Text(
                text = "App Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }
    }
}