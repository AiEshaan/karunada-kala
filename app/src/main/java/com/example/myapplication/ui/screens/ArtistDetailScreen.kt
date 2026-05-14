package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.ui.components.GyroParallaxHero
import com.example.myapplication.ui.components.AppBackgroundContainer
import com.example.myapplication.data.model.Artist
import com.example.myapplication.viewmodel.ArtistViewModel
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.ui.components.bouncyClickable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel: ArtistViewModel = viewModel()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val artistState by viewModel.selectedArtistState.collectAsState()
    val artist = (artistState as? UiState.Success<Artist?>)?.data
    val isLoading = artistState is UiState.Loading

    LaunchedEffect(artistId) {
        viewModel.loadArtistById(artistId)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    AnimatedContent(
        targetState = isLoading,
        label = "artistDetailTransition",
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        }
    ) { loading ->
        if (loading) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                com.example.myapplication.ui.components.UnrollingScrollAnimation(color = MaterialTheme.colorScheme.primary)
            }
        } else if (artist == null || artistId.isBlank()) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👤", fontSize = 60.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Artist profile coming soon!", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "We are still indexing this artist's profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            artist?.let { currentArtist ->
                AppBackgroundContainer(textureAlpha = 0.03f) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            LargeTopAppBar(
                                title = { Text(currentArtist.name, style = MaterialTheme.typography.headlineMedium) },
                                scrollBehavior = scrollBehavior,
                                navigationIcon = {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                },
                                colors = TopAppBarDefaults.largeTopAppBarColors(
                                    containerColor = Color.Transparent,
                                    scrolledContainerColor = HeritageCream.copy(alpha = 0.95f),
                                    titleContentColor = KarnatakaRed,
                                    navigationIconContentColor = KarnatakaRed,
                                    actionIconContentColor = KarnatakaRed
                                ),
                                actions = {
                                    IconButton(onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Check out ${currentArtist.name}, a ${currentArtist.artType} artist on Karunada Kala!")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                        }
                    ) { padding ->

                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .verticalScroll(scrollState)
                        ) {

                            // 🔥 IMMERSIVE HERO
                            Box(modifier = Modifier.height(380.dp)) {
                                with(sharedTransitionScope) {
                                    GyroParallaxHero(
                                        imageUrl = currentArtist.photoUrl,
                                        modifier = Modifier.fillMaxSize(),
                                        scrollOffset = scrollState.value.toFloat(),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        sharedKey = "artist_image_$artistId"
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
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
                                        shape = RoundedCornerShape(50),
                                    ) {
                                        Text(
                                            text = currentArtist.artType,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentArtist.city,
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 📊 BADGE STATS
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BadgeStat(value = "${currentArtist.experienceYears}+", label = "Years", Modifier.weight(1f))
                                BadgeStat(value = "25+", label = "Works", Modifier.weight(1f))
                                BadgeStat(value = "100+", label = "Students", Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // 📝 BIO SECTION
                            Text(
                                text = "Legacy & Story",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = currentArtist.bio,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 26.sp
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            // 📲 CONTACT SECTION
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // WhatsApp / Message
                                Button(
                                    onClick = {
                                        val url = "https://wa.me/${currentArtist.phone}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Chat, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Message", fontWeight = FontWeight.Bold)
                                }

                                // Tap to Call (Requirement)
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentArtist.phone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Call, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Call Artist", fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = value, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
