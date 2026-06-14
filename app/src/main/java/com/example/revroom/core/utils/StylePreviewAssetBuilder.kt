package com.example.revroom.core.utils

import android.util.Log

object StylePreviewAssetBuilder {

    private val styleFileNames = mapOf(
        "indochine" to "indochine.webp",
        "modern" to "modern.webp",
        "modern luxury" to "modern.webp",
        "japandi" to "japandi.webp",
        "scandinavian" to "scandinavian.webp",
        "industrial" to "industrial.webp",
        "minimalist" to "minimalist.webp",
        "minimalist luxury" to "minimalist.webp",
        "neo classic" to "neo_classic.webp",
        "neoclassic" to "neo_classic.webp",
        "tropical" to "tropical.webp"
    )

    private val supportedRoomTypes = setOf(
        "living_room",
        "master_bedroom",
        "kitchen",
        "bathroom",
        "study_room"
    )

    fun buildAssetPath(
        roomType: String?,
        styleName: String
    ): String? {
        Log.d("StylePreview", "Building path for: roomType=$roomType, styleName='$styleName'")

        if (roomType.isNullOrBlank()) return null

        // Chuyển đổi ID phòng nếu cần (ví dụ từ backend về format folder)
        val folderName = roomType.lowercase().trim()

        if (!supportedRoomTypes.contains(folderName)) {
            Log.w("StylePreview", "Folder '$folderName' not supported")
            return null
        }

        val normalizedStyle = styleName.trim().lowercase()
        val fileName = styleFileNames[normalizedStyle]

        if (fileName == null) {
            Log.w("StylePreview", "No file mapping for style: '$normalizedStyle'")
            return null
        }

        val path = "file:///android_asset/style_previews/$folderName/$fileName"
        Log.d("StylePreview", "Final Path: $path")
        return path
    }
}
