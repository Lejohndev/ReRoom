package com.example.revroom.data.local

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

class UserManager(context: Context) {
    // Tạo một file lưu trữ ngầm trong điện thoại tên là "InteriorAIPrefs"
    private val prefs = context.getSharedPreferences("InteriorAIPrefs", Context.MODE_PRIVATE)
    var isNewUser = false
    fun getDeviceId(): String {
        var deviceId = prefs.getString("DEVICE_ID", null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit(commit = true) { // Dùng commit = true để đảm bảo lưu ngay
                putString("DEVICE_ID", deviceId)
            }
        }
        return deviceId
    }

    var isRegisteredOnServer: Boolean
        get() = prefs.getBoolean("IS_REGISTERED", false)
        set(value) = prefs.edit { putBoolean("IS_REGISTERED", value) }

}