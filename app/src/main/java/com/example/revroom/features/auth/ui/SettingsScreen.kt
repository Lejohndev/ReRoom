package com.example.revroom.features.auth.ui
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.revroom.data.local.UserManager

@Composable
fun SettingsScreen(userManager: UserManager) {
    // Lấy ID từ máy ra để hiển thị
    val userId = remember { userManager.getDeviceId() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFFAFAFA)) // Màu nền hơi xám nhẹ giống ảnh
    ) {
        Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))

        // 1. Cái thẻ màu tím (Giao diện Free/Pro)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFB145FF)), // Màu tím gradient (tạm dùng màu đặc)
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Free", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Upgrade for more", color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /* TODO: Xử lý nâng cấp Pro */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upgrade to Pro", color = Color(0xFFB145FF))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Chữ USER SETTINGS
        Text("USER SETTINGS", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

        // 3. Khung chứa cái ID và nút Copy
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "User", tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                // Hiển thị đoạn mã ID lấy từ UserManager
                Text(
                    text = userId,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f)
                )
                // Nút Copy ID
                IconButton(onClick = { 
                    clipboardManager.setText(AnnotatedString(userId))
                    Toast.makeText(context, "Đã copy ID!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray)
                }
            }
        }
    }
}