package com.example.revroom.data.local

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import java.util.UUID

// TODO: replace with AuthController.getUserId() when Duy's module is done
class LocalUserIdProvider(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrCreateUserId(): String {
        val existingUserId = preferences.getString(KEY_USER_ID, null)
        if (!existingUserId.isNullOrBlank()) {
            return existingUserId
        }

        // DEV_ONLY: generate temp UUID for Phase 4 development
        val userId = UUID.randomUUID().toString()
        Log.w(TAG, "DEV: using temp user-id: $userId")
        preferences.edit {
            putString(KEY_USER_ID, userId)
        }
        return userId
    }

    private companion object {
        const val TAG = "LocalUserIdProvider"
        const val PREFS_NAME = "InteriorAIPrefs"
        const val KEY_USER_ID = "DEVICE_ID"
    }
}
