package com.example.revroom.features.design_studio.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    private val repository: DesignRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DesignUiState())
    val uiState: StateFlow<DesignUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    data class DesignFeatureItem(
        val id: String,
        val title: String,
        val subtitle: String,
        val badge: String,
        val colors: List<Color>
    )

    data class SelectionItem(
        val id: String,
        val label: String,
        val colors: List<Color>
    )

    val interiorFeatures = listOf(
        DesignFeatureItem(
            "interior_design",
            "Interior Design",
            "Redesign your interior space",
            "Before",
            listOf(Color(0xFFE8E2D8), Color(0xFF75675B), Color(0xFF181818))
        ),
        DesignFeatureItem(
            "furnish_empty_room",
            "Furnish Empty Room",
            "Transform empty space into furnished room",
            "Before",
            listOf(Color(0xFFEDE8DE), Color(0xFFB7A58C), Color(0xFF1F1A15))
        ),
        DesignFeatureItem(
            "remove_furniture",
            "Remove Furniture",
            "Clear and empty your room",
            "Before",
            listOf(Color(0xFFECE7DD), Color(0xFF9F8F7C), Color(0xFF171717))
        )
    )

    val exteriorFeatures = listOf(
        DesignFeatureItem(
            "exterior_design",
            "Exterior Design",
            "Redesign your exterior space",
            "After",
            listOf(Color(0xFFC6D8A8), Color(0xFF4D744B), Color(0xFF182512))
        ),
        DesignFeatureItem(
            "facade",
            "Facade",
            "Building exterior & materials",
            "After",
            listOf(Color(0xFFD8D2C8), Color(0xFF56626B), Color(0xFF15191D))
        ),
        DesignFeatureItem(
            "landscaping",
            "Landscaping",
            "Beautiful outdoor spaces",
            "After",
            listOf(Color(0xFFD5E3C4), Color(0xFF50705A), Color(0xFF132016))
        )
    )

    val roomTypes = listOf(
        SelectionItem("living_room", "Living Room", listOf(Color(0xFFD8C3A5), Color(0xFF735F4D))),
        SelectionItem("bedroom", "Bedroom", listOf(Color(0xFFE6D8CC), Color(0xFF78909C))),
        SelectionItem("kitchen", "Kitchen", listOf(Color(0xFFDCE8E4), Color(0xFF7E8D85))),
        SelectionItem("bathroom", "Bathroom", listOf(Color(0xFFE7E1D4), Color(0xFFA79F93))),
        SelectionItem("dining_room", "Dining Room", listOf(Color(0xFFECE4D7), Color(0xFFB48B58))),
        SelectionItem("hallway", "Hallway", listOf(Color(0xFFE8E2DB), Color(0xFFB9A18D))),
        SelectionItem("master", "Master", listOf(Color(0xFFE7DDD0), Color(0xFF917C66))),
        SelectionItem("kids_room", "Kids Room", listOf(Color(0xFFE9F0EF), Color(0xFFA7C7C5))),
        SelectionItem("guest_room", "Guest Room", listOf(Color(0xFFE5D7C6), Color(0xFF9E8065))),
    )

    val interiorStyles = listOf(
        SelectionItem("custom", "Custom style", listOf(Color(0xFFF9C4EA), Color(0xFFD9E3FF))),
        SelectionItem("modern", "Modern", listOf(Color(0xFFE4E0D9), Color(0xFF7D7269))),
        SelectionItem("luxury", "Luxury", listOf(Color(0xFFC7BBB0), Color(0xFF43362F))),
        SelectionItem("minimalist", "Minimalist", listOf(Color(0xFFF4F2EE), Color(0xFFC9C4B9))),
        SelectionItem("scandinavian", "Scandinavian", listOf(Color(0xFFF0D7BE), Color(0xFFB87555))),
        SelectionItem("industrial", "Industrial", listOf(Color(0xFFBCA88F), Color(0xFF48362A))),
        SelectionItem("coastal", "Coastal", listOf(Color(0xFFD8EBF2), Color(0xFF7DA5B2))),
        SelectionItem("bohemian", "Bohemian", listOf(Color(0xFFCFB48C), Color(0xFF6B4B32))),
        SelectionItem("warm", "Warm", listOf(Color(0xFFE1B686), Color(0xFF7A5135))),
    )

    val exteriorStyles = listOf(
        SelectionItem("custom", "Custom", listOf(Color(0xFF7B1FA2), Color(0xFFBA68C8))),
        SelectionItem("modern", "Modern", listOf(Color(0xFFDAD7CC), Color(0xFF6F7D70))),
        SelectionItem("traditional", "Traditional", listOf(Color(0xFFD8C5AA), Color(0xFF6E5A43))),
        SelectionItem("sleek", "Sleek", listOf(Color(0xFFBFC7C1), Color(0xFF516056))),
        SelectionItem("coastal", "Coastal", listOf(Color(0xFFD6EAF2), Color(0xFF6CA0B8))),
        SelectionItem("farmhouse", "Farmhouse", listOf(Color(0xFFE7E0D3), Color(0xFF7B8065))),
        SelectionItem("garden", "Garden", listOf(Color(0xFFD9E8C8), Color(0xFF558B5B))),
        SelectionItem("villa", "Villa", listOf(Color(0xFFE3D6C4), Color(0xFF8F6E52))),
        SelectionItem("minimal", "Minimal", listOf(Color(0xFFEDEDEA), Color(0xFF9EA4A1))),
    )

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
    }

    fun selectStyle(style: String) {
        _uiState.value = _uiState.value.copy(selectedStyle = style, errorMessage = null)
    }

    fun createDesign() {
        val currentState = _uiState.value
        val imageUri = currentState.selectedImageUri
        val roomType = currentState.selectedRoomType ?: "exterior"
        val style = currentState.selectedStyle

        if (imageUri == null || style == null || (currentState.designMode == DesignMode.Interior && currentState.selectedRoomType == null)) {
            _uiState.value = currentState.copy(
                phase = DesignPhase.Failed,
                errorMessage = "Please choose a photo, room type, and style."
            )
            return
        }

        pollingJob?.cancel()
        _uiState.value = currentState.copy(phase = DesignPhase.Uploading, errorMessage = null)

        viewModelScope.launch {
            val promptStyle = buildPromptStyle(currentState, roomType, style)

            repository.uploadDesign(
                DesignRequest(imageUri = imageUri, roomType = roomType, style = promptStyle)
            )
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        phase = DesignPhase.Processing,
                        designId = response.designId,
                        originalImageUrl = response.originalImageUrl,
                        errorMessage = null
                    )
                    startPolling(response.designId)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        phase = DesignPhase.Failed,
                        errorMessage = error.message ?: "Unable to create the design."
                    )
                }
        }
    }

    fun retry() {
        createDesign()
    }

    fun reset() {
        pollingJob?.cancel()
        _uiState.value = DesignUiState(designMode = _uiState.value.designMode)
    }

    private fun buildPromptStyle(state: DesignUiState, roomType: String, style: String): String {
        val mode = if (state.designMode == DesignMode.Interior) "interior" else "exterior"
        val feature = state.selectedFeature ?: "design"
        return "$mode $feature $roomType $style"
    }

    private fun startPolling(designId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            repeat(MAX_POLL_ATTEMPTS) {
                delay(POLL_INTERVAL_MS)

                val currentPhase = _uiState.value.phase
                if (currentPhase == DesignPhase.Completed || currentPhase == DesignPhase.Failed) {
                    return@launch
                }

                repository.getDesignStatus(designId)
                    .onSuccess { status ->
                        when (status.status) {
                            DesignJobStatus.Completed -> {
                                _uiState.value = _uiState.value.copy(
                                    phase = DesignPhase.Completed,
                                    originalImageUrl = status.originalImageUrl,
                                    designedImageUrl = status.designedImageUrl,
                                    errorMessage = null
                                )
                                return@launch
                            }

                            DesignJobStatus.Failed -> {
                                _uiState.value = _uiState.value.copy(
                                    phase = DesignPhase.Failed,
                                    errorMessage = status.errorMessage ?: "Design generation failed."
                                )
                                return@launch
                            }

                            else -> {
                                _uiState.value = _uiState.value.copy(
                                    phase = DesignPhase.Processing,
                                    originalImageUrl = status.originalImageUrl
                                )
                            }
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            phase = DesignPhase.Failed,
                            errorMessage = error.message ?: "Unable to check design status."
                        )
                        return@launch
                    }
            }

            _uiState.value = _uiState.value.copy(
                phase = DesignPhase.Failed,
                errorMessage = "The generation is taking too long. Please try again."
            )
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DesignViewModel(DesignRepository(appContext)) as T
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 2_000L
        const val MAX_POLL_ATTEMPTS = 150
    }
}
