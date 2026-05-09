package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Artist
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.ArtistViewModel
import com.example.myapplication.viewmodel.ArtViewModel
import com.example.myapplication.viewmodel.ChatViewModel
import com.example.myapplication.ui.state.UiState
import kotlinx.coroutines.launch
import androidx.compose.animation.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    name: String,
    description: String,
    imageUrl: String,
    artistId: String,
    category: String,
    navController: NavController,
    viewModel: ArtViewModel,
    artistViewModel: ArtistViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val haptic = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()

    val aiDescriptions by viewModel.aiDescriptions.collectAsState()
    val artLegends by viewModel.artLegends.collectAsState()
    val artList by viewModel.artForms.collectAsState()
    val currentArt by remember(artList) { derivedStateOf { artList.find { it.name == name } } }
    val isSaved = currentArt?.isLiked ?: false
    val viewCount = currentArt?.viewCount ?: 0
    val ambientInsight by chatViewModel.ambientInsight.collectAsState()

    val selectedArtistState by artistViewModel.selectedArtistState.collectAsState()
    val selectedArtist = (selectedArtistState as? UiState.Success<Artist?>)?.data

    var entered by remember { mutableStateOf(false) }
    
    // 3D Flip State
    val rotation = remember { androidx.compose.animation.core.Animatable(0f) }

    val entranceScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.8f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        entered = true
        viewModel.addViewedArt(name)
        viewModel.generateAiDescriptionIfNeeded(name, description, category)
        if (artistId != "none" && artistId.isNotBlank()) {
            artistViewModel.loadArtistById(artistId)
        }
    }

    val scrollState = rememberScrollState()
    
    AppBackgroundContainer(
        overlayBrush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { Text(name, style = MaterialTheme.typography.headlineMedium) },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .graphicsLayer {
                            scaleX = entranceScale
                            scaleY = entranceScale
                        }
                ) {

                    // 🔥 3D HERO EFFECT (WOW Factor)
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(400.dp)
                            .graphicsLayer {
                                rotationY = rotation.value
                                cameraDistance = 12 * density
                            }
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        rotation.animateTo(
                                            targetValue = if (rotation.value == 0f) 180f else 0f,
                                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                                        )
                                    }
                                }
                            },
                        shape = RoundedCornerShape(32.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (rotation.value <= 90f || rotation.value > 270f) {
                                // FRONT SIDE
                                with(sharedTransitionScope) {
                                    GyroParallaxHero(
                                        imageUrl = imageUrl,
                                        modifier = Modifier.fillMaxSize(),
                                        scrollOffset = scrollState.value.toFloat(),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        sharedKey = "art_image_${currentArt?.id ?: ""}"
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(0.7f))
                                            )
                                        )
                                )
                                Text(
                                    text = "Tap to Reveal Legend",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                                )
                            } else {
                                // Card back showing art legend
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f }
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📜", fontSize = 48.sp)
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            text = artLegends[name] ?: "Unrolling the ancient story...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontStyle = FontStyle.Italic,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 28.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 👤 CUSTODIAN SECTION
                    selectedArtist?.let { artist ->
                        CustodianSection(artist) {
                            navController.navigate(NavRoutes.artistDetail(artist.id))
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // 📄 DESCRIPTION AREA
                    val displayDescription = aiDescriptions[name] ?: description
                    val isAiEnhanced = aiDescriptions.containsKey(name)

                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    LoadingToContent(
                        isLoading = viewModel.isGeneratingAiDescription.collectAsState().value,
                        loadingPlaceholder = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.height(8.dp))
                                Text("Weaving AI Narrative...", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    ) {
                        Column {
                            if (isAiEnhanced) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("✨ AI Enhanced Narrative", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👁", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                com.example.myapplication.ui.components.CountUpText(
                                    targetValue = viewCount,
                                    suffix = " Views",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            com.example.myapplication.ui.components.BurstSaveButton(
                                isSaved = isSaved,
                                onToggle = { viewModel.toggleLike(currentArt?.id ?: "") }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = displayDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 🎧 AUDIO NARRATIVE (Artisan Voice)
                    AudioNarrativeCard(
                        audioUrl = currentArt?.audioUrl ?: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                        title = name
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 🎥 VIDEO B-ROLL
                    VideoBrollCard(videoUrl = currentArt?.videoUrl ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")

                    Spacer(modifier = Modifier.height(32.dp))

                    // 🌳 LEGACY TREE
                    LegacyTree(
                        guru = selectedArtist?.guruName ?: "Traditional Master",
                        artist = selectedArtist?.name ?: "Current Custodian",
                        students = selectedArtist?.studentsDescription ?: "Dedicated Apprentices"
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // 🎓 MENTORSHIP CALL TO ACTION
                    MentorshipCTA(artistName = selectedArtist?.name ?: "the Master")

                    Spacer(modifier = Modifier.height(48.dp))

                    // 🎭 RELATED ARTS (Staggered)
                    RelatedArtsSection(
                        category = category,
                        currentArtName = name,
                        viewModel = viewModel,
                        navController = navController
                    )

                    Spacer(modifier = Modifier.height(64.dp))
                }

                KalaAmbientInsight(
                    insight = ambientInsight,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)
                )
            }
        }
    }
}

@Composable
fun CustodianSection(artist: Artist, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = artist.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(artist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Primary Custodian of this Art", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun MentorshipCTA(artistName: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Request Mentorship", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Learn the secrets of this craft directly from $artistName.", textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* Handle request */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SEND REQUEST")
            }
        }
    }
}

@Composable
fun RelatedArtsSection(
    category: String,
    currentArtName: String,
    viewModel: ArtViewModel,
    navController: NavController
) {
    val related = viewModel.getRelatedArts(currentArtName, category)
    if (related.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Related Art Forms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            related.forEachIndexed { index, art ->
                FadeInItem(delayMillis = index * 100) {
                    PressableCard(
                        onClick = { NavRoutes.navigateToDetail(navController, art) },
                        modifier = Modifier.width(160.dp).padding(end = 16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                AsyncImage(
                                    model = art.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.height(120.dp).fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                                Text(art.name, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
