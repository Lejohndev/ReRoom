package com.example.revroom.core.utils

import android.util.Log

object StylePreviewAssetBuilder {

    private val styleFileNames = mapOf(
        "indochine" to "indochine.webp",
        "modern" to "modern.webp",
        "modern luxury" to "modern_luxury.webp",
        "japandi" to "japandi.webp",
        "scandinavian" to "scandinavian.webp",
        "industrial" to "industrial.webp",
        "minimalist" to "minimalist.webp",
        "neo classic" to "neo_classic.webp"
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

        if (roomType.isNullOrBlank()) {
            return null
        }

        if (!supportedRoomTypes.contains(roomType)) {
            return null
        }

        val fileName =
            styleFileNames[styleName.trim().lowercase()]
                ?: return null

        val path = "file:///android_asset/style_previews/$roomType/$fileName"
        Log.d("StylePreviewAssetBuilder", "room=$roomType style=$styleName path=$path")
        return path
    }
}
