package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.ui.components.ArtCard
import com.example.myapplication.ui.components.ArtCardShimmer
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.theme.RoyalRed
import com.example.myapplication.viewmodel.ArtViewModel
import com.example.myapplication.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController, 
    viewModel: ArtViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {

    val artList by viewModel.artForms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val listState = rememberLazyListState()

    var showChat by remember { mutableStateOf(false) }

    val filteredList = artList.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val offset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset * 0.5f
            } else {
                0f
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchArtForms()
    }

    // Load recommendations once artList is ready
    LaunchedEffect(artList) {
        if (artList.isNotEmpty()) {
            viewModel.loadRecommendations(artList.map { it.name })
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChat = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Face, contentDescription = "Ask Kala AI")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Karunada Kala", 
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            
            AnimatedContent(
                targetState = isLoading,
                label = "shimmerTransition",
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                }
            ) { loading ->
                if (loading) {
                    LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        item { HeroSection(offset) }
                        items(5) {
                            ArtCardShimmer()
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        state = listState,
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item { HeroSection(offset) }

                        item {
                            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                                // ✨ AI RECOMMENDATIONS SECTION
                                if (recommendations.isNotEmpty()) {
                                    Text(
                                        "✨ Personalized for You",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                    
                                    recommendations.forEach { rec ->
                                        Card(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                                .fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = rec.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = rec.reason,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search Karnataka's heritage...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    listOf("All", "Dance", "Craft", "Music", "Painting").forEach { category ->
                                        FilterChip(
                                            selected = selectedCategory == category,
                                            onClick = { selectedCategory = category },
                                            label = { Text(category) },
                                            modifier = Modifier.padding(end = 8.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = Color.White,
                                                labelColor = MaterialTheme.colorScheme.primary
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = selectedCategory == category,
                                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                selectedBorderColor = Color.Transparent
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (filteredList.isEmpty() && searchQuery.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No results found for \"$searchQuery\"",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(filteredList, key = { _, art -> art.id }) { index, art ->
                                val shouldAnimate = index < 10
                                var visible by remember { mutableStateOf(!shouldAnimate) }
                                
                                LaunchedEffect(art.id) {
                                    if (shouldAnimate) {
                                        delay(index * 80L)
                                        visible = true
                                    }
                                }

                                AnimatedVisibility(
                                    visible = visible,
                                    enter = slideInVertically(
                                        animationSpec = tween(400)
                                    ) { it / 2 } + fadeIn(
                                        animationSpec = tween(400)
                                    )
                                ) {
                                    ArtCard(
                                        art = art,
                                        listState = listState,
                                        onNavigate = {
                                            val encodedDescription = Uri.encode(art.description)
                                            val encodedImageUrl = Uri.encode(art.imageUrl)
                                            val encodedArtistId = Uri.encode(art.artistId)
                                            val encodedCategory = Uri.encode(art.category)
                                            navController.navigate(
                                                NavRoutes.detail(
                                                    art.name, 
                                                    encodedDescription, 
                                                    encodedImageUrl, 
                                                    encodedArtistId,
                                                    encodedCategory
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔥 KALA CHATBOT BOTTOM SHEET
        if (showChat) {
            ChatBottomSheet(
                viewModel = chatViewModel,
                onDismiss = { showChat = false }
            )
        }
    }
}

@Composable
fun HeroSection(offset: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer {
                translationY = offset
            }
    ) {
        AsyncImage(
            model = "https://upload.wikimedia.org/wikipedia/commons/c/c6/Yakshagana_vesha.jpg",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(0.8f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    "HERITAGE",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Discover Karnataka’s Living Heritage",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheet(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var input by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.75f)
                .fillMaxWidth()
                .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding())
        ) {
            // Header
            Text(
                text = "Namaskara! I'm Kala 🙏",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = "Your cultural guide to Karnataka.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            // Chat History
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                if (messages.isEmpty()) {
                    item {
                        ChatBubble(ChatMessage("How can I help you discover Karnataka's heritage today?", false))
                    }
                }
                items(messages) { msg ->
                    ChatBubble(msg)
                    Spacer(Modifier.height(12.dp))
                }
                if (isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            // Input Row
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Ask about Karnataka arts...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                viewModel.sendMessage(input)
                                input = ""
                            }
                        },
                        enabled = !isLoading && input.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (message.isUser) MaterialTheme.colorScheme.primary else Color(0xFFF5F5F5)
    val textColor = if (message.isUser) Color.White else Color.Black
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = color,
            shape = shape,
            tonalElevation = 1.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
