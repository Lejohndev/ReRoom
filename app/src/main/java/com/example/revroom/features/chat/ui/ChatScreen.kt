package com.example.revroom.features.chat.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.revroom.core.theme.RevroomTheme
import com.example.revroom.core.ui.*
import com.example.revroom.features.chat.model.ChatMessage
import com.example.revroom.features.chat.viewmodel.ChatViewModel
import com.example.revroom.features.design_studio.components.FanSpinnerAnimation

@Composable
fun ChatScreen(
    onInterior: () -> Unit,
    onExterior: () -> Unit,
    onChat: () -> Unit,
    onGallery: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(LocalContext.current))
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    StudioScaffold(
        selectedTab = StudioTab.Chat,
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        onInterior = onInterior,
        onExterior = onExterior,
        onChat = onChat,
        onGallery = onGallery
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudioText
                )
                IconButton(
                    onClick = { /* New Chat */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat", tint = StudioText)
                }
            }

            // Messages
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.messages) { message ->
                        ChatBubble(message)
                    }
                    if (uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                FanSpinnerAnimation(modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }
            }

            // Input Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Image Preview khi được chọn
                if (uiState.selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp, start = 52.dp)
                            .size(80.dp)
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Nút Close để hủy chọn ảnh
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .size(20.dp)
                                .clickable { viewModel.onImageSelected(null) },
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFF3F4F6), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (uiState.selectedImageUri != null) Icons.Outlined.Image else Icons.Default.Add,
                            contentDescription = "Attach",
                            tint = if (uiState.selectedImageUri != null) StudioPurple else StudioMuted
                        )
                    }

                    TextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputTextChanged,
                        placeholder = {
                            Text(
                                "Ask me anything...",
                                color = StudioMuted,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = StudioPurple
                        ),
                        shape = RoundedCornerShape(24.dp),
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (uiState.isLoading) {
                                            Modifier.background(StudioMuted)
                                        } else {
                                            Modifier.background(StudioGradient)
                                        }
                                    )
                                    .clickable(enabled = !uiState.isLoading) { viewModel.sendMessage() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        if (message.localImageUri != null || message.imageUrl != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .width(240.dp)
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = message.localImageUri ?: message.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (message.isFromUser) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text("Uploaded", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        if (message.text.isNotBlank()) {
            Surface(
                color = if (message.isFromUser) Color.White else Color(0xFFF3F4F6),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (message.isFromUser) 20.dp else 4.dp,
                    bottomEnd = if (message.isFromUser) 4.dp else 20.dp
                ),
                tonalElevation = 1.dp,
                shadowElevation = if (message.isFromUser) 2.dp else 0.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.weight(1f, fill = false),
                        fontSize = 14.sp,
                        color = StudioText,
                        lineHeight = 20.sp
                    )
                    if (message.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = StudioPurple
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    RevroomTheme {
        ChatScreen(
            onInterior = {},
            onExterior = {},
            onChat = {},
            onGallery = {}
        )
    }
}
