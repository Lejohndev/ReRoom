package com.example.revroom.features.design_studio.model

import android.net.Uri

data class DesignRequest(
    val imageUri: Uri,
    val styleId: Int,
    val roomType: String? = null,
    val featureId: String? = null
)

data class DesignStyle(
    val styleId: Int,
    val styleName: String,
    val coreAesthetic: String = "",
    val lightingOptions: List<String> = emptyList(),
    val materialOptions: List<String> = emptyList(),
    val colorRuleOptions: List<String> = emptyList(),
    val atmosphereOptions: List<String> = emptyList()
)

enum class DesignMode {
    Interior,
    Exterior
}

data class DesignResult(
    val designId: String,
    val status: DesignJobStatus,
    val originalImageUrl: String,
    val designedImageUrl: String?,
    val errorMessage: String?
)

enum class DesignJobStatus {
    Pending,
    Completed,
    Failed,
    Unknown;

    companion object {
        fun fromApiValue(value: String): DesignJobStatus {
            return when (value.lowercase()) {
                "pending" -> Pending
                "completed" -> Completed
                "failed" -> Failed
                else -> Unknown
            }
        }
    }
}
