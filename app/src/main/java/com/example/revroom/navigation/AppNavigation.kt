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
import android.content.Intent
import com.example.revroom.features.history.ui.HistoryActivity
import com.example.revroom.features.history.ui.HistoryScreen
import com.example.revroom.features.history.ui.ProjectDetailActivity
import com.example.revroom.data.local.UserManager // Nhớ Alt+Enter import nếu thiếu
import com.example.revroom.features.auth.ui.SettingsScreen

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val factory = remember(context) { DesignViewModel.Factory(context) }
    val viewModel: DesignViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val designStyles by viewModel.designStyles.collectAsState()
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

    fun goInterior() = go(Route.INTERIOR_HOME)
    fun goExterior() = go(Route.EXTERIOR_HOME)
    fun goChat() = go(Route.CHAT)
    fun goGallery() = go(Route.GALLERY)

    NavHost(
        navController = navController,
        startDestination = Route.INTERIOR_HOME,
    ) {
        composable(Route.INTERIOR_HOME) {
            DesignHomeScreen(
                title = "Interior Design",
                chips = listOf("Interior Design", "Furnish Empty Room", "Remove Furniture"),
                features = viewModel.interiorFeatures,
                selectedTab = StudioTab.Interior,
                onFeatureClick = { featureId ->
                    viewModel.startDesign(DesignMode.Interior, featureId)
                    navController.navigate(Route.UPLOAD)
                },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.EXTERIOR_HOME) {
            DesignHomeScreen(
                title = "Exterior Design",
                chips = listOf("Exterior Design", "Facade", "Landscaping", "Add Greenery"),
                features = viewModel.exteriorFeatures,
                selectedTab = StudioTab.Exterior,
                onFeatureClick = { featureId ->
                    viewModel.startDesign(DesignMode.Exterior, featureId)
                    navController.navigate(Route.UPLOAD)
                },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.UPLOAD) {
            val isExterior = uiState.designMode == DesignMode.Exterior
            UploadPhotoScreen(
                title = if (isExterior) "Exterior Design" else "Interior Design",
                selectedTab = if (isExterior) StudioTab.Exterior else StudioTab.Interior,
                selectedImageUri = uiState.selectedImageUri,
                onImageSelected = viewModel::selectImage,
                onNext = {
                    if (isExterior) {
                        navController.navigate(Route.STYLE_SELECT)
                    } else {
                        navController.navigate(Route.ROOM_TYPE)
                    }
                },
                onBack = { navController.popBackStack() },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.ROOM_TYPE) {
            RoomTypeScreen(
                selectedRoomType = uiState.selectedRoomType,
                roomTypes = viewModel.roomTypes,
                onRoomTypeSelected = viewModel::selectRoomType,
                onNext = { navController.navigate(Route.STYLE_SELECT) },
                onBack = { navController.popBackStack() },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.STYLE_SELECT) {
            val isExterior = uiState.designMode == DesignMode.Exterior
            StyleScreen(
                title = if (isExterior) "Exterior Design" else "Interior Design",
                selectedTab = if (isExterior) StudioTab.Exterior else StudioTab.Interior,
                selectedStyle = uiState.selectedStyle,
                styles = designStyles,
                onStyleSelected = viewModel::selectStyle,
                onCreateDesign = {
                    viewModel.createDesign()
                    navController.navigate(Route.PROCESSING_RESULT) {
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

        composable(Route.PROCESSING_RESULT) {
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
                    navController.navigate(Route.UPLOAD) {
                        popUpTo(Route.UPLOAD) { inclusive = true }
                    }
                },
                onBack = {
                    val destination = if (uiState.designMode == DesignMode.Exterior) {
                        Route.EXTERIOR_HOME
                    } else {
                        Route.INTERIOR_HOME
                    }
                    viewModel.reset()
                    navController.navigate(destination) {
                        popUpTo(Route.INTERIOR_HOME) { inclusive = false }
                    }
                },
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.CHAT) {
            ChatPlaceholderScreen(
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery
            )
        }

        composable(Route.GALLERY) {
            HistoryScreen(
                onInterior = ::goInterior,
                onExterior = ::goExterior,
                onChat = ::goChat,
                onGallery = ::goGallery,
                onProfileClick = { navController.navigate(Route.SETTINGS) },
                onProjectClick = { project ->
                    val intent = Intent(context, ProjectDetailActivity::class.java).apply {
                        putExtra(HistoryActivity.EXTRA_DESIGN_ID, project.designId)
                        putExtra(HistoryActivity.EXTRA_ORIGINAL_IMAGE, project.originalImageUrl)
                        putExtra(HistoryActivity.EXTRA_DESIGNED_IMAGE, project.designedImageUrl)
                        putExtra(HistoryActivity.EXTRA_STATUS, project.status)
                        putExtra(HistoryActivity.EXTRA_CREATED_AT, project.createdAt)
                    }
                    context.startActivity(intent)
                }
            )
        }
        composable(Route.SETTINGS) {
            // Gọi màn hình của m ra, UserManager thì xin từ Context
            SettingsScreen(userManager = UserManager(LocalContext.current))
        }
    }
}

private object Route {
    const val INTERIOR_HOME = "interior_home"
    const val EXTERIOR_HOME = "exterior_home"
    const val UPLOAD = "upload"
    const val ROOM_TYPE = "room_type"
    const val STYLE_SELECT = "style_select"
    const val PROCESSING_RESULT = "processing_result"
    const val CHAT = "chat"
    const val GALLERY = "gallery"
    const val SETTINGS = "settings"
}
