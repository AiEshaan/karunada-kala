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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.KarnatakaRed
import com.example.myapplication.ui.theme.TempleGreen
import com.example.myapplication.ui.theme.HeritageGold
import com.example.myapplication.viewmodel.ArtViewModel
import com.example.myapplication.viewmodel.ChatViewModel
import com.example.myapplication.viewmodel.JourneyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import com.example.myapplication.ui.components.AIRecommendationStrip
import com.example.myapplication.ui.components.AppBackgroundContainer
import com.example.myapplication.ui.components.StaggeredItem
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

    val scrollOffset = remember { derivedStateOf { 
        if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset.toFloat() else 500f
    } }

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
                        .graphicsLayer {
                            alpha = (1f - (scrollOffset.value / 400f)).coerceIn(0f, 1f)
                            translationY = scrollOffset.value * 0.3f
                            scaleX = (1f - (scrollOffset.value / 1000f)).coerceAtLeast(0.8f)
                            scaleY = (1f - (scrollOffset.value / 1000f)).coerceAtLeast(0.8f)
                        }
                    ) {
                        ExploreHeroSection()
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
                        
                        Spacer(Modifier.height(8.dp))

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

                // AI Recommendations Section
                item {
                    val recommendations by viewModel.aiRecommendations.collectAsState()
                    if (recommendations.isNotEmpty()) {
                        AIRecommendationStrip(
                            recommendations = recommendations,
                            onNavigate = { name ->
                                val art = artList.find { it.name == name }
                                if (art != null) NavRoutes.navigateToDetail(navController, art)
                            }
                        )
                    }
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
            placeholder = { 
                Text(
                    "Search the archives...", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = TempleGreen.copy(alpha = 0.5f)
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .onFocusChanged { onFocusChange(it.isFocused) }
                .shadow(
                    elevation = if (isSearchFocused) 24.dp else 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(28.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KarnatakaRed) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    val infinite = rememberInfiniteTransition(label = "micPulse")
                    val micScale by infinite.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                        label = "micScale"
                    )
                    
                    IconButton(onClick = { /* Voice Search */ }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = if (searchQuery.isNotEmpty()) HeritageGold else KarnatakaRed.copy(alpha = 0.6f),
                            modifier = if (isSearchFocused && searchQuery.isEmpty()) Modifier.graphicsLayer {
                                scaleX = micScale
                                scaleY = micScale
                            } else Modifier
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KarnatakaRed.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                cursorColor = KarnatakaRed
            ),
            singleLine = true
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
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                "RECOMMENDED FOR YOU ✨",
                style = MaterialTheme.typography.labelLarge,
                color = KarnatakaRed,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Curated from Karnataka’s living heritage",
                style = MaterialTheme.typography.bodySmall,
                color = TempleGreen.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading && artList.isEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) {
                    CulturalShimmer(
                        modifier = Modifier
                            .size(220.dp, 280.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                }
            }
        } else {
            val recommended by remember(artList) {
                derivedStateOf {
                    artList.distinctBy { it.name }.sortedByDescending {
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
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "fabGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val shadowElev by infiniteTransition.animateValue(
        initialValue = 8.dp,
        targetValue = 24.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadowElev"
    )

    Surface(
        modifier = modifier
            .size(76.dp)
            .graphicsLayer {
                scaleX = glowScale
                scaleY = glowScale
            },
        shape = CircleShape,
        color = Color.Transparent, // Radial gradient instead
        shadowElevation = shadowElev,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            HeritageGold,
                            KarnatakaRed
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Simulated AI Waveform Circles
            repeat(3) { i ->
                val waveScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.4f + (i * 0.2f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000 + (i * 500), easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "wave$i"
                )
                val waveAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000 + (i * 500), easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "alpha$i"
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = waveScale
                            scaleY = waveScale
                            alpha = waveAlpha
                        }
                        .background(HeritageGold, CircleShape)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AutoAwesome, 
                    contentDescription = null, 
                    tint = Color.White, 
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "KALA AI", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.White, 
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
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
    val suggestions by viewModel.suggestions.collectAsState()
    var text by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
            if (isLoading) {
                item {
                    KalaThinkingIndicator()
                }
            }
        }

        // Suggestion Chips
        if (messages.size <= 1 && !isLoading) {
            Text(
                "Try asking:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { viewModel.sendMessage(suggestion) },
                        label = { Text(suggestion, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    )
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
                placeholder = { Text("Ask about our heritage...", fontSize = 14.sp) },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                trailingIcon = {
                    if (text.isNotBlank()) {
                        IconButton(onClick = { text = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                onClick = {
                    viewModel.sendMessage(text)
                    text = ""
                },
                enabled = text.isNotBlank() && !isLoading,
                shape = CircleShape,
                color = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: com.example.myapplication.data.model.ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }
    val containerColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isUser) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✨", fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
            
            Surface(
                color = containerColor,
                shape = shape,
                modifier = Modifier.widthIn(max = 280.dp),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun KalaThinkingIndicator() {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("✨", fontSize = 14.sp)
            }
        }
        Spacer(Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val alphas = listOf(
                        infiniteTransition.animateFloat(0.2f, 1f, infiniteRepeatable(tween(600, delayMillis = 0), RepeatMode.Reverse)),
                        infiniteTransition.animateFloat(0.2f, 1f, infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse)),
                        infiniteTransition.animateFloat(0.2f, 1f, infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse))
                    )
                    alphas.forEach { alpha ->
                        Box(
                            Modifier
                                .size(6.dp)
                                .graphicsLayer { this.alpha = alpha.value }
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
