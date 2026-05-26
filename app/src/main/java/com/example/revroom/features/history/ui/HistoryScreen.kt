package com.example.revroom.features.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.revroom.core.ui.*
import com.example.revroom.features.history.model.ProjectModel
import com.example.revroom.features.history.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    onInterior: () -> Unit,
    onExterior: () -> Unit,
    onChat: () -> Unit,
    onGallery: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(LocalContext.current))
) {
    // Observe ViewModel states
    val projects by viewModel.projects.observeAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val error by viewModel.error.observeAsState(initial = null)

    LaunchedEffect(Unit) {
        viewModel.refreshProjects()
    }

    StudioScaffold(
        selectedTab = StudioTab.Gallery,
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        onInterior = onInterior,
        onExterior = onExterior,
        onChat = onChat,
        onGallery = onGallery
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopTitleBar(
                title = "My Projects",
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StudioGradient)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Person, "Profile", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (projects.isEmpty() && !isLoading) {
                    EmptyStateView(error)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        // items(items = ...) giúp trình biên dịch không bị nhầm lẫn
                        items(items = projects) { project ->
                            ProjectItem(project)
                        }
                    }
                }

                if (isLoading && projects.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFC62828))
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(error: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Collections, null, tint = StudioMuted, modifier = Modifier.size(48.dp))
        Text("No projects yet", Modifier.padding(top = 14.dp), color = StudioText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(error ?: "Your redesign projects will appear here", Modifier.padding(top = 7.dp), color = StudioMuted, fontSize = 12.sp)
    }
}

@Composable
fun ProjectItem(project: ProjectModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { /* Handle click */ }
    ) {
        AsyncImage(
            model = project.designedImageUrl ?: project.originalImageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            project.status ?: "Processing",
            Modifier.padding(8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (project.status?.equals("completed", ignoreCase = true) == true) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}
