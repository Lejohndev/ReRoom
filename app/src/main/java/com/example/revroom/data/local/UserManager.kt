package com.example.revroom.data.local

import android.content.Context
import java.util.UUID

class UserManager(context: Context) {
    // Tạo một file lưu trữ ngầm trong điện thoại tên là "InteriorAIPrefs"
    private val prefs = context.getSharedPreferences("InteriorAIPrefs", Context.MODE_PRIVATE)
    var isNewUser = false
    fun getDeviceId(): String {
        // 1. Kiểm tra xem trong máy đã có ID chưa
        var deviceId = prefs.getString("DEVICE_ID", null)

        // 2. Nếu chưa có (nghĩa là mở app lần đầu), thì tạo mới một cái UUID
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString() // Tạo mã ID ngẫu nhiên không đụng hàng

            // Lưu lại vào máy để lần sau không bị tạo mới nữa
            prefs.edit().putString("DEVICE_ID", deviceId).apply()

            // TODO: Ở ĐÂY SẼ GỌI API BẮN XUỐNG C# CỦA M NÀY!
            // Ví dụ: api.registerDevice(RegisterDeviceRequest(userId = deviceId, name = "New User"))
            // Đánh dấu đây là người mới!
            isNewUser = true
        }


        return deviceId
    }

}