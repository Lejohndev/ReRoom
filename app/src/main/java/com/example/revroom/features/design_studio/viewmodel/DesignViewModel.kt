package com.example.revroom.features.design_studio.viewmodel

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.revroom.R
import com.example.revroom.core.utils.StylePreviewAssetBuilder
import com.example.revroom.data.repository.DesignRepository
import com.example.revroom.features.design_studio.model.DesignJobStatus
import com.example.revroom.features.design_studio.model.DesignMode
import com.example.revroom.features.design_studio.model.DesignPhase
import com.example.revroom.features.design_studio.model.DesignRequest
import com.example.revroom.features.design_studio.model.DesignUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DesignViewModel(
    private val repository: DesignRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DesignUiState())
    val uiState: StateFlow<DesignUiState> = _uiState.asStateFlow()

    private val _designStyles = MutableStateFlow<List<SelectionItem>>(emptyList())
    val designStyles: StateFlow<List<SelectionItem>> = _designStyles.asStateFlow()

    private var pollingJob: Job? = null

    data class DesignFeatureItem(
        val id: String,
        val title: String,
        val subtitle: String,
        val colors: List<Color>,
        val beforeImageRes: Int? = null,
        val afterImageRes: Int? = null
    )

    data class SelectionItem(
        val id: String,
        val label: String,
        val colors: List<Color>,
        val description: String = "",
        val lighting: String = "",
        val material: String = "",
        val color: String = "",
        val atmosphere: String = "",
        val imageRes: Int? = null,
        val previewAssetPath: String? = null
    )

    val interiorFeatures = listOf(
        DesignFeatureItem(
            "interior_design",
            "Interior Design",
            "Redesign your interior space",
            listOf(Color(0xFFE8E2D8), Color(0xFF75675B), Color(0xFF181818)),
            beforeImageRes = R.drawable.feature_interior_before,
            afterImageRes = R.drawable.feature_interior_after
        ),
        DesignFeatureItem(
            "furnish_empty_room",
            "Furnish Empty Room",
            "Transform empty space into furnished room",
            listOf(Color(0xFFEDE8DE), Color(0xFFB7A58C), Color(0xFF1F1A15)),
            beforeImageRes = R.drawable.feature_furnish_before,
            afterImageRes = R.drawable.feature_furnish_after
        ),
        DesignFeatureItem(
            "remove_furniture",
            "Remove Furniture",
            "Clear and empty your room",
            listOf(Color(0xFFECE7DD), Color(0xFF9F8F7C), Color(0xFF171717)),
            beforeImageRes = R.drawable.feature_remove_before,
            afterImageRes = R.drawable.feature_remove_after
        )
    )

    val exteriorFeatures = listOf(
        DesignFeatureItem(
            "exterior_design",
            "Exterior Design",
            "Redesign your exterior space",
            listOf(Color(0xFFC6D8A8), Color(0xFF4D744B), Color(0xFF182512))
        ),
        DesignFeatureItem(
            "facade",
            "Facade",
            "Building exterior & materials",
            listOf(Color(0xFFD8D2C8), Color(0xFF56626B), Color(0xFF15191D))
        ),
        DesignFeatureItem(
            "landscaping",
            "Landscaping",
            "Beautiful outdoor spaces",
            listOf(Color(0xFFD5E3C4), Color(0xFF50705A), Color(0xFF132016))
        )
    )

    val roomTypes = listOf(
        SelectionItem("living_room", "Living Room", listOf(Color(0xFFD8C3A5), Color(0xFF735F4D)), imageRes = R.drawable.room_living_room),
        SelectionItem("master_bedroom", "Master Bedroom", listOf(Color(0xFFE6D8CC), Color(0xFF78909C)), imageRes = R.drawable.room_master_bedroom),
        SelectionItem("kitchen", "Kitchen", listOf(Color(0xFFDCE8E4), Color(0xFF7E8D85)), imageRes = R.drawable.room_kitchen),
        SelectionItem("bathroom", "Bathroom", listOf(Color(0xFFE7E1D4), Color(0xFFA79F93)), imageRes = R.drawable.room_bathroom),
        SelectionItem("study_room", "Study Room", listOf(Color(0xFFE8E2DB), Color(0xFFB9A18D)), imageRes = R.drawable.room_study_room),
    )

    private val stylePalettes = listOf(
        listOf(Color(0xFFE4E0D9), Color(0xFF7D7269)),
        listOf(Color(0xFFF4F2EE), Color(0xFFC9C4B9)),
        listOf(Color(0xFFF0D7BE), Color(0xFFB87555)),
        listOf(Color(0xFFD8EBF2), Color(0xFF7DA5B2)),
        listOf(Color(0xFFE1B686), Color(0xFF7A5135)),
    )

    init {
        loadDesignStyles()
    }

    fun startDesign(mode: DesignMode, featureId: String) {
        pollingJob?.cancel()
        _uiState.value = DesignUiState(
            designMode = mode,
            selectedFeature = featureId,
            selectedRoomType = if (mode == DesignMode.Exterior) "exterior" else null
        )
    }

    fun selectImage(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri, errorMessage = null)
    }

    fun selectRoomType(roomType: String) {
        _uiState.value = _uiState.value.copy(selectedRoomType = roomType, errorMessage = null)
        rebuildStylePreviews(roomType)
    }

    fun selectStyle(style: String) {
        _uiState.value = _uiState.value.copy(selectedStyle = style, errorMessage = null)
    }

    fun selectModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model, errorMessage = null)
    }

    private fun loadDesignStyles() {
        viewModelScope.launch {
            repository.getDesignStyles()
                .onSuccess { styles ->
                    val currentRoomType = _uiState.value.selectedRoomType
                    _designStyles.value = styles.mapIndexed { index, style ->
                        SelectionItem(
                            id = style.styleId.toString(),
                            label = style.styleName,
                            colors = stylePalettes[index % stylePalettes.size],
                            description = style.coreAesthetic,
                            lighting = style.lighting,
                            material = style.material,
                            color = style.color,
                            atmosphere = style.atmosphere,
                            previewAssetPath = StylePreviewAssetBuilder.buildAssetPath(
                                roomType = currentRoomType,
                                styleName = style.styleName
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Unable to load design styles."
                    )
                }
        }
    }

    private fun rebuildStylePreviews(roomType: String) {
        val currentStyles = _designStyles.value
        if (currentStyles.isEmpty()) return

        _designStyles.value = currentStyles.map { style ->
            style.copy(
                previewAssetPath = StylePreviewAssetBuilder.buildAssetPath(
                    roomType = roomType,
                    styleName = style.label
                )
            )
        }
    }

    private fun startPolling(designId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                repository.getDesignStatus(designId)
                    .onSuccess { result ->
                        _uiState.value = _uiState.value.copy(
                            designId = result.designId,
                            designedImageUrl = result.designedImageUrl,
                            phase = when (result.status) {
                                DesignJobStatus.Completed -> DesignPhase.Completed
                                DesignJobStatus.Failed -> DesignPhase.Failed
                                else -> DesignPhase.Processing
                            }
                        )
                        if (result.status == DesignJobStatus.Completed || result.status == DesignJobStatus.Failed) {
                            return@launch
                        }
                    }
                delay(3000)
            }
        }
    }

    fun createDesign() {
        val state = _uiState.value
        val styleIdInt = if (state.selectedFeature == "remove_furniture") 0 else state.selectedStyle?.toIntOrNull()
        
        if (state.selectedImageUri == null || (state.selectedFeature != "remove_furniture" && (state.selectedRoomType == null || styleIdInt == null))) {
            _uiState.value = state.copy(errorMessage = "Please complete all steps.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(phase = DesignPhase.Processing)
            
            val request = DesignRequest(
                imageUri = state.selectedImageUri,
                styleId = styleIdInt,
                roomType = state.selectedRoomType,
                featureId = state.selectedFeature
            )

            repository.uploadDesign(request)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        designId = result.designId,
                        originalImageUrl = result.originalImageUrl
                    )
                    startPolling(result.designId)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        phase = DesignPhase.Idle,
                        errorMessage = error.message ?: "Design generation failed."
                    )
                }
        }
    }

    fun retry() {
        createDesign()
    }

    fun reset() {
        pollingJob?.cancel()
        _uiState.value = DesignUiState()
    }

    class Factory(private val repository: DesignRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DesignViewModel(repository) as T
        }
    }
}
