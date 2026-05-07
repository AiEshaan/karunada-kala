package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.myapplication.ui.components.AudioNarrativeCard
import com.example.myapplication.ui.components.LegacyTree
import com.example.myapplication.ui.components.VideoBrollCard
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.ArtistViewModel
import com.example.myapplication.viewmodel.ArtViewModel
import com.example.myapplication.ui.components.AppBackgroundContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    name: String,
    description: String,
    imageUrl: String,
    artistId: String,
    category: String,
    navController: NavController,
    viewModel: ArtViewModel,
    artistViewModel: ArtistViewModel = viewModel()
) {
    val haptic = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()

    val aiDescriptions by viewModel.aiDescriptions.collectAsState()
    val artLegends by viewModel.artLegends.collectAsState()
    val selectedArtist by artistViewModel.selectedArtist.collectAsState()

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

    AppBackgroundContainer(textureAlpha = 0.03f) {
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

            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
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
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
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
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    title = name
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 🎥 VIDEO B-ROLL
                VideoBrollCard(videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")

                Spacer(modifier = Modifier.height(32.dp))

                // 🌳 LEGACY TREE
                LegacyTree(
                    guru = selectedArtist?.guruName ?: "Traditional Master",
                    artist = selectedArtist?.name ?: "Current Custodian",
                    students = selectedArtist?.studentsDescription ?: "Dedicated Apprentices"
                )

                Spacer(modifier = Modifier.height(48.dp))

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun CustodianSection(artist: Artist, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = artist.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD4AF37), CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Preservation Custodian",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${artist.experienceYears} Years of Legacy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onClick,
                modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
