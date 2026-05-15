package com.example.revroom.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.revroom.core.ui.StudioTab
import com.example.revroom.features.chat.ui.ChatPlaceholderScreen
import com.example.revroom.features.design_studio.model.DesignMode
import com.example.revroom.features.design_studio.ui.DesignHomeScreen
import com.example.revroom.features.design_studio.ui.ProcessingResultScreen
import com.example.revroom.features.design_studio.ui.RoomTypeScreen
import com.example.revroom.features.design_studio.ui.StyleScreen
import com.example.revroom.features.design_studio.ui.UploadPhotoScreen
import com.example.revroom.features.design_studio.viewmodel.DesignViewModel
import com.example.revroom.features.history.ui.HistoryScreen

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val factory = remember(context) { DesignViewModel.Factory(context) }
    val viewModel: DesignViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    fun go(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun goInterior() = go(Route.InteriorHome)
    fun goExterior() = go(Route.ExteriorHome)
    fun goChat() = go(Route.Chat)
    fun goGallery() = go(Route.Gallery)

    NavHost(
        navController = navController,
        startDestination = Route.InteriorHome
    ) {
        composable(Route.InteriorHome) {
            DesignHomeScreen(
                title = "RoomGPT - Interior Design",
                chips = listOf("Interior Design", "Furnish Empty Room", "Remove Furniture"),
                features = viewModel.interiorFeatures,
                selectedTab = StudioTab.Interior,
                onFeatureClick = { featureId ->
                    viewModel.startDesign(DesignMode.Interior, featureId)
                    navController.navigate(Route.Upload)
                },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.ExteriorHome) {
            DesignHomeScreen(
                title = "RoomGPT - Exterior Design",
                chips = listOf("Exterior Design", "Facade", "Landscaping", "Add Greenery"),
                features = viewModel.exteriorFeatures,
                selectedTab = StudioTab.Exterior,
                onFeatureClick = { featureId ->
                    viewModel.startDesign(DesignMode.Exterior, featureId)
                    navController.navigate(Route.Upload)
                },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.Upload) {
            val isExterior = uiState.designMode == DesignMode.Exterior
            UploadPhotoScreen(
                title = if (isExterior) "Exterior Design" else "Interior Design",
                selectedTab = if (isExterior) StudioTab.Exterior else StudioTab.Interior,
                selectedImageUri = uiState.selectedImageUri,
                onImageSelected = viewModel::selectImage,
                onNext = {
                    if (isExterior) {
                        navController.navigate(Route.StyleSelect)
                    } else {
                        navController.navigate(Route.RoomType)
                    }
                },
                onBack = { navController.popBackStack() },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.RoomType) {
            RoomTypeScreen(
                selectedRoomType = uiState.selectedRoomType,
                roomTypes = viewModel.roomTypes,
                onRoomTypeSelected = viewModel::selectRoomType,
                onNext = { navController.navigate(Route.StyleSelect) },
                onBack = { navController.popBackStack() },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.StyleSelect) {
            val isExterior = uiState.designMode == DesignMode.Exterior
            StyleScreen(
                title = if (isExterior) "Exterior Design" else "Interior Design",
                selectedTab = if (isExterior) StudioTab.Exterior else StudioTab.Interior,
                selectedStyle = uiState.selectedStyle,
                styles = if (isExterior) viewModel.exteriorStyles else viewModel.interiorStyles,
                onStyleSelected = viewModel::selectStyle,
                onCreateDesign = {
                    viewModel.createDesign()
                    navController.navigate(Route.ProcessingResult) {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.ProcessingResult) {
            ProcessingResultScreen(
                uiState = uiState,
                onRetry = viewModel::retry,
                onSaveToHistory = { goGallery() },
                onCreateAnother = {
                    val mode = uiState.designMode
                    val feature = uiState.selectedFeature ?: if (mode == DesignMode.Exterior) {
                        "exterior_design"
                    } else {
                        "interior_design"
                    }
                    viewModel.startDesign(mode, feature)
                    navController.navigate(Route.Upload) {
                        popUpTo(Route.Upload) { inclusive = true }
                    }
                },
                onBack = {
                    val destination = if (uiState.designMode == DesignMode.Exterior) {
                        Route.ExteriorHome
                    } else {
                        Route.InteriorHome
                    }
                    viewModel.reset()
                    navController.navigate(destination) {
                        popUpTo(Route.InteriorHome) { inclusive = false }
                    }
                },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.Chat) {
            ChatPlaceholderScreen(
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.Gallery) {
            HistoryScreen(
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }
    }
}

private object Route {
    const val InteriorHome = "interior_home"
    const val ExteriorHome = "exterior_home"
    const val Upload = "upload"
    const val RoomType = "room_type"
    const val StyleSelect = "style_select"
    const val ProcessingResult = "processing_result"
    const val Chat = "chat"
    const val Gallery = "gallery"
}
