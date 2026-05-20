package com.example.revroom.features.auth.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revroom.BuildConfig
import com.example.revroom.core.network.RetrofitClient
import com.example.revroom.core.utils.FileUtils
import com.example.revroom.data.remote.DeviceRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthViewModel : ViewModel() {

    // --- QUẢN LÝ TRẠNG THÁI UI ---
    // Lưu URI ảnh đang chọn từ điện thoại (Chưa bấm lưu)
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    // Trạng thái đang upload (để hiện vòng xoay Loading)
    var isUploading by mutableStateOf(false)
        private set

    // 👉 (MỚI) Lưu đường link ảnh lấy từ Database về để hiển thị
    var avatarUrl by mutableStateOf<String?>(null)
        private set

    // --- CÁC HÀM XỬ LÝ LOGIC ---

    // Hàm cập nhật URI khi người dùng chọn ảnh xong
    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    // (GIỮ NGUYÊN) Hàm bắn ID
    fun sendIdToServer(userId: String) {
        viewModelScope.launch {
            try {
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

    // 👉 (MỚI) HÀM KÉO THÔNG TIN USER TỪ SERVER VỀ
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    val rawFileName = response.body()?.avatarUrl

                    // Nếu C# trả về chuỗi tên file, tự ghép link hoàn chỉnh
                    if (!rawFileName.isNullOrEmpty()) {
                        avatarUrl = "${BuildConfig.BASE_URL}uploads/$rawFileName"
                        Log.d("API_TEST", "Lấy ảnh thành công: $avatarUrl")
                    }
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Lỗi kéo Profile: ${e.message}")
            }
        }
    }

    // HÀM BẮN ẢNH LÊN SERVER
    fun uploadImage(context: Context, userId: String) {
        val uri = selectedImageUri ?: return

        viewModelScope.launch {
            isUploading = true
            try {
                // 1. Chuyển Uri thành File tạm
                val file = FileUtils.uriToFile(context, uri)
                if (file == null) {
                    Log.e("API_TEST", "Lỗi: Không đọc được file ảnh!")
                    return@launch
                }

                // 2. Đóng gói Multipart
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Gọi API
                val response = RetrofitClient.apiService.uploadAvatar(userIdBody, body)

                if (response.isSuccessful) {
                    Log.d("API_TEST", "Thành công! Đã đẩy ảnh lên C#")

                    // 👉 Xóa ảnh tạm m vừa chọn đi (để nhường chỗ cho ảnh thật từ mạng load về)
                    selectedImageUri = null

                    // 👉 Gọi hàm kéo Profile để cập nhật lại link ảnh mới nhất vào giao diện
                    loadUserProfile(userId)
                } else {
                    Log.e("API_TEST", "Lỗi C# trả về: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Sập mạng: ${e.message}")
            } finally {
                isUploading = false
            }
        }
    }
}