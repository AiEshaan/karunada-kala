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
import java.util.Locale

private const val TAG = "ExploreScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ArtViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val artList by viewModel.artForms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val artLegends by viewModel.artLegends.collectAsState()
    val isGeneratingLegend by viewModel.isGeneratingLegend.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    val listState = rememberLazyListState()
    var showChat by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var isAiAssistantEnabled by remember { mutableStateOf(true) }

    var selectedArtForLegend by remember { mutableStateOf<ArtForm?>(null) }
    var showLegendSheet by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        viewModel.fetchArtForms()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        GrainOverlay()

        Scaffold(
            containerColor = Color.Transparent,
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    val alpha by remember {
                        derivedStateOf {
                            if (firstItemIndex == 0) {
                                (1f - (firstItemScrollOffset / 500f)).coerceIn(0f, 1f)
                            } else 0f
                        }
                    }
                    val translationY by remember {
                        derivedStateOf {
                            if (firstItemIndex == 0) {
                                (firstItemScrollOffset * 0.3f)
                            } else 0f
                        }
                    }

                    Box(modifier = Modifier.graphicsLayer {
                        this.alpha = alpha
                        this.translationY = translationY
                    }) {
                        HeaderSection(
                            isAiAssistantEnabled = isAiAssistantEnabled,
                            onAiAssistantToggle = { isAiAssistantEnabled = it }
                        )
                    }
                }

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
                            }
                        )

                        CategoriesSection(
                            selectedCategory = selectedCategory,
                            onCategorySelect = { selectedCategory = it }
                        )
                    }
                }

                if (artList.isNotEmpty() && !isLoading) {
                    item {
                        RecommendedSection(
                            artList = artList,
                            navController = navController,
                            onLikeToggle = { viewModel.toggleLike(it) }
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
                        StaggeredAnimatedItem(index = index) {
                            ArtCard(
                                art = art,
                                onNavigate = {
                                    viewModel.addRecentSearch(art.name)
                                    NavRoutes.navigateToDetail(navController, art)
                                },
                                onLikeToggle = {
                                    viewModel.toggleLike(art.id)
                                },
                                onShowLegend = {
                                    selectedArtForLegend = art
                                    viewModel.generateLegend(art.name)
                                    showLegendSheet = true
                                }
                            )
                        }
                    }
                }

                item {
                    SanghaSection(onJoinClick = { navController.navigate(NavRoutes.Community.route) })
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            AnimatedVisibility(
                visible = isAiAssistantEnabled,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                KalaAiFab(
                    onClick = { showChat = true }
                )
            }
        }

        if (showChat) {
            ChatBottomSheet(
                viewModel = chatViewModel,
                onDismiss = { showChat = false }
            )
        }

        if (showLegendSheet && selectedArtForLegend != null) {
            ModalBottomSheet(
                onDismissRequest = { showLegendSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📜 THE ANCIENT LEGEND",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        selectedArtForLegend?.name ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    if (isGeneratingLegend && !artLegends.containsKey(selectedArtForLegend?.name)) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Kala is unrolling the archives...", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                    } else {
                        Text(
                            text = artLegends[selectedArtForLegend?.name] ?: "The archives are quiet for this art form, but its beauty speaks for itself.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 28.sp
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Button(
                        onClick = { 
                            showLegendSheet = false
                            NavRoutes.navigateToDetail(navController, selectedArtForLegend!!)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("DIVE DEEPER INTO HISTORY")
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    isAiAssistantEnabled: Boolean,
    onAiAssistantToggle: (Boolean) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scrollIndicator")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "HERITAGE OF KARNATAKA",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Text(
            "Explore",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.offset(x = (-4).dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Discover the timeless arts, where tradition meets technology.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                "Kala AI Assistant",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isAiAssistantEnabled,
                onCheckedChange = onAiAssistantToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().graphicsLayer { translationY = offset }
        ) {
            Text(
                "SCROLL TO UNROLL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    suggestions: List<String>,
    recentSearches: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Box {
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

@Composable
fun RecommendedSection(
    artList: List<ArtForm>,
    navController: NavController,
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
            recommended.forEach { art ->
                Box(modifier = Modifier.width(220.dp).animateContentSize()) {
                    ArtCard(
                        art = art,
                        onNavigate = {
                            NavRoutes.navigateToDetail(navController, art)
                        },
                        onLikeToggle = { onLikeToggle(art.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StaggeredAnimatedItem(index: Int, content: @Composable () -> Unit) {
    val animatable = remember { Animatable(50f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        launch { animatable.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.8f)) }
        launch { alpha.animateTo(1f, tween(500)) }
    }
    Box(modifier = Modifier.offset(y = animatable.value.dp).alpha(alpha.value)) { content() }
}

@Composable
fun GrainOverlay() {
    // Optimized: Use a fixed set of points and drawPoints for efficiency
    val grainPoints = remember {
        val points = mutableListOf<Offset>()
        val step = 12 // Increased step for performance
        for (x in 0..2000 step step) {
            for (y in 0..3000 step step) {
                if ((0..10).random() > 8) {
                    points.add(Offset(x.toFloat(), y.toFloat()))
                }
            }
        }
        points
    }

    Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
        drawPoints(
            points = grainPoints,
            pointMode = PointMode.Points,
            color = Color.Black,
            strokeWidth = 1f
        )
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
    val scope = rememberCoroutineScope()

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
                        val pullX = (position.x - centerX) * 0.4f
                        val pullY = (position.y - centerY) * 0.4f

                        scope.launch {
                            offsetX.animateTo(pullX, spring(stiffness = 300f, dampingRatio = 0.6f))
                        }
                        scope.launch {
                            offsetY.animateTo(pullY, spring(stiffness = 300f, dampingRatio = 0.6f))
                        }

                        if (event.changes.first().pressed.not()) {
                            scope.launch {
                                offsetX.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.4f))
                            }
                            scope.launch {
                                offsetY.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.4f))
                            }
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
            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text("ASK KALA", style = MaterialTheme.typography.labelLarge, color = Color.White, fontSize = 10.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheet(
    viewModel: ChatViewModel,
    journeyViewModel: JourneyViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var input by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                }
            } else {
                Log.e(TAG, "Initialization failed")
            }
        }
    }
    DisposableEffect(Unit) { onDispose { tts?.shutdown() } }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            if (spokenText.isNotBlank()) {
                viewModel.sendMessage(spokenText)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) { viewModel.analyzeImage(bitmap) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.8f).fillMaxWidth().padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding())) {
            Text(text = "Kala AI Assistant", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary.copy(0.1f))

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 24.dp)) {
                items(messages) { msg ->
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = if(msg.isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                        Surface(
                            color = if(msg.isUser) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = if(msg.isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column {
                                Text(msg.text, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = if(msg.isUser) Color.White else MaterialTheme.colorScheme.onBackground)
                                if (!msg.isUser) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                        IconButton(onClick = { tts?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, "KalaSpeak") }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                        }
                                        if (msg.text.contains("day", ignoreCase = true) || msg.text.contains("itinerary", ignoreCase = true) || msg.text.contains("plan", ignoreCase = true)) {
                                            IconButton(onClick = { journeyViewModel.addManualEntry("AI Cultural Itinerary", "Planned by Kala") }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Book, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (isLoading) {
                    item { Text("Kala is thinking...", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.secondary) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val suggestions = listOf("Cultural trip itinerary", "Tell me a legend", "Regional foods", "Nearby workshops")
                suggestions.forEach { suggestion ->
                    SuggestionChip(onClick = { viewModel.sendMessage(suggestion) }, label = { Text(suggestion) }, shape = RoundedCornerShape(50))
                }
            }

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                placeholder = { Text("Query the archives...") },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { IconButton(onClick = { cameraLauncher.launch(null) }) { Icon(Icons.Default.CameraAlt, contentDescription = "Heritage Vision", tint = MaterialTheme.colorScheme.secondary) } },
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Kala anything...")
                            }
                            isListening = true
                            speechLauncher.launch(intent)
                        }) {
                            val micColor by animateColorAsState(targetValue = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "micAnim")
                            Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = if (isListening) micColor else MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = { if(input.isNotBlank()) { viewModel.sendMessage(input); input = "" } }) { Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    }
                }
            )
        }
    }
}
