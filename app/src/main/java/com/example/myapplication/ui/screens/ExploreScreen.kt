package com.example.myapplication.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.data.model.ArtForm
import com.example.myapplication.ui.components.ArtCard
import com.example.myapplication.ui.components.ArtCardShimmer
import com.example.myapplication.ui.components.KalaFilterChip
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.viewmodel.ArtViewModel
import com.example.myapplication.viewmodel.ChatViewModel
import com.example.myapplication.viewmodel.JourneyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import com.example.myapplication.ui.components.AppBackgroundContainer
import com.example.myapplication.ui.components.StaggeredItem
import com.example.myapplication.ui.components.KalaAmbientInsight
import java.util.Locale

private const val TAG = "ExploreScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ArtViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val artList by viewModel.artForms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    val listState = rememberLazyListState()
    var showChat by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var isAiAssistantEnabled by remember { mutableStateOf(true) }

    val filteredList by remember(artList, selectedCategory, searchQuery) {
        derivedStateOf {
            artList.filter {
                (selectedCategory == "All" || it.category == selectedCategory) &&
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val firstItemScrollOffset by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset }
    }
    val firstItemIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    val ambientInsight by chatViewModel.ambientInsight.collectAsState()

    AppBackgroundContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 1. Animated Hero Section
                item {
                    Box(modifier = Modifier
                        .parallaxScroll(listState)
                        .graphicsLayer(alpha = 0.95f) // Phase 4.1: Hero Glow
                    ) {
                        HeaderSection(
                            isAiAssistantEnabled = isAiAssistantEnabled,
                            onAiAssistantToggle = { isAiAssistantEnabled = it }
                        )
                    }
                }

                // 2. Search & Categories
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SearchSection(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                            isSearchFocused = isSearchFocused,
                            onFocusChange = { isSearchFocused = it },
                            suggestions = suggestions,
                            recentSearches = recentSearches,
                            onSuggestionClick = { suggestion ->
                                viewModel.onSearchQueryChange(suggestion)
                                viewModel.addRecentSearch(suggestion)
                                isSearchFocused = false
                            }
                        )

                        CategoriesSection(
                            selectedCategory = selectedCategory,
                            onCategorySelect = { selectedCategory = it }
                        )
                    }
                }

                // 3. Recommended Section
                item {
                    RecommendedSection(
                        artList = artList,
                        navController = navController,
                        isLoading = isLoading,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onLikeToggle = { viewModel.toggleLike(it) }
                    )
                }

                // 4. Staggered Art Grid
                if (isLoading && artList.isEmpty()) {
                    items(4) { ArtCardShimmer() }
                } else {
                    itemsIndexed(filteredList) { index, art ->
                        if (index >= filteredList.size - 1 && searchQuery.isEmpty() && selectedCategory == "All") {
                            LaunchedEffect(Unit) {
                                viewModel.loadMoreArtForms()
                            }
                        }
                        FadeInItem(delayMillis = (index % 6).let { it * 100 }) {
                            with(sharedTransitionScope) {
                                ArtCard(
                                    art = art,
                                    onNavigate = {
                                        NavRoutes.navigateToDetail(navController, art)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    onLikeToggle = {
                                        viewModel.toggleLike(art.id)
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    SanghaSection(onJoinClick = { navController.navigate(NavRoutes.Community.route) })
                }
            }

            // Ambient Insight Overlay
            LoadingToContent(
                isLoading = chatViewModel.isThinking.collectAsState().value,
                loadingPlaceholder = { 
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Kala is thinking...", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            ) {
                KalaAmbientInsight(
                    insight = ambientInsight,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)
                )
            }

            // FAB Overlay
            if (isAiAssistantEnabled) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.BottomEnd) {
                    KalaAiFab(onClick = { showChat = true })
                }
            }
        }
    }

    if (showChat) {
        ChatBottomSheet(
            viewModel = chatViewModel,
            onDismiss = { showChat = false }
        )
    }
}

@Composable
fun HeaderSection(
    isAiAssistantEnabled: Boolean,
    onAiAssistantToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "KARUNADA",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "KALA",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = { onAiAssistantToggle(!isAiAssistantEnabled) }) {
                Icon(
                    if (isAiAssistantEnabled) Icons.Default.AutoAwesome else Icons.Default.AutoAwesomeMotion,
                    contentDescription = null,
                    tint = if (isAiAssistantEnabled) MaterialTheme.colorScheme.secondary else Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Living heritage archives of Karnataka",
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    suggestions: List<String>,
    recentSearches: List<String>,
    isSearchFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search the archives...", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .onFocusChanged { onFocusChange(it.isFocused) },
            shape = RoundedCornerShape(24.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
            )
        )

        if (isSearchFocused && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .zIndex(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            "TRENDING & RECENT",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    suggestions.forEach { suggestion ->
                        TextButton(
                            onClick = { onSuggestionClick(suggestion) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val icon = if (recentSearches.contains(suggestion)) "🕒" else "🔥"
                            Text("$icon $suggestion", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesSection(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        val categories = listOf(
            "All" to "🏛",
            "Dance" to "💃",
            "Craft" to "🏺",
            "Music" to "🎵",
            "Painting" to "🎨"
        )
        categories.forEach { (category, emoji) ->
            KalaFilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelect(category) },
                label = category,
                icon = emoji
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecommendedSection(
    artList: List<ArtForm>,
    navController: NavController,
    isLoading: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLikeToggle: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "RECOMMENDED FOR YOU ✨",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && artList.isEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) {
                    com.example.myapplication.ui.components.CulturalShimmer(
                        modifier = Modifier
                            .size(220.dp, 280.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                }
            }
        } else {
            val recommended by remember(artList) {
                derivedStateOf {
                    artList.sortedByDescending {
                        (if (it.isLiked) 10 else 0) + (it.viewCount / 5)
                    }.take(5)
                }
            }

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                recommended.forEachIndexed { index, art ->
                    StaggeredItem(index = index) {
                        Box(modifier = Modifier.width(220.dp).animateContentSize()) {
                            with(sharedTransitionScope) {
                                ArtCard(
                                    art = art,
                                    onNavigate = {
                                        NavRoutes.navigateToDetail(navController, art)
                                    },
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    onLikeToggle = { onLikeToggle(art.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SanghaSection(onJoinClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Become a Guardian", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Join the Sangha and contribute to our living heritage archives.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onJoinClick, modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))) {
            Text("JOIN THE SANGHA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun KalaAiFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val density = LocalDensity.current
    var badgeSize by remember { mutableStateOf(IntSize.Zero) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(targetOffset) {
        coroutineScope {
            launch { offsetX.animateTo(targetOffset.x, spring(stiffness = 300f, dampingRatio = 0.6f)) }
            launch { offsetY.animateTo(targetOffset.y, spring(stiffness = 300f, dampingRatio = 0.6f)) }
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { badgeSize = it.size }
            .offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.first().position
                        val centerX = badgeSize.width / 2f
                        val centerY = badgeSize.height / 2f
                        
                        if (event.changes.first().pressed) {
                            targetOffset = Offset(
                                (position.x - centerX) * 0.4f,
                                (position.y - centerY) * 0.4f
                            )
                        } else {
                            targetOffset = Offset.Zero
                        }
                    }
                }
            }
            .rotate(-12f)
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text("ASK KALA", style = MaterialTheme.typography.labelLarge, color = Color.White, fontSize = 10.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheet(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) }
    ) {
        ChatScreen(viewModel = viewModel)
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                val alignment = if (message.isUser) Alignment.End else Alignment.Start
                val color = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSecondaryContainer

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                    Surface(
                        color = color,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(vertical = 4.dp).widthIn(max = 280.dp)
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
            if (isLoading) {
                item {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(8.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about our heritage...") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    viewModel.sendMessage(text)
                    text = ""
                },
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}
