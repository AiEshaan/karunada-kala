package com.example.myapplication.ui.components

import android.content.Intent
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.myapplication.ui.theme.KarnatakaRed
import com.example.myapplication.ui.theme.HeritageGold
import com.example.myapplication.ui.theme.TempleGreen
import com.example.myapplication.ui.theme.HeritageCream
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.myapplication.data.model.ArtForm
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ArtCard(
    art: ArtForm,
    onNavigate: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLikeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val trimmedImageUrl = art.imageUrl.trim()
    
    // Preload image for smooth transition
    LaunchedEffect(trimmedImageUrl) {
        Log.d("IMG_CHECK", "ArtForm ID: ${art.id}, Name: ${art.name}, URL = $trimmedImageUrl")
        val request = ImageRequest.Builder(context)
            .data(trimmedImageUrl)
            .crossfade(true)
            .build()
        context.imageLoader.enqueue(request)
    }
    
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "cardFlip"
    )

    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }
    val animatedTiltX by animateFloatAsState(tiltX, label = "tiltX")
    val animatedTiltY by animateFloatAsState(tiltY, label = "tiltY")
    
    KalaGlassCard(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        tiltX = (offset.y / size.height - 0.5f) * 15f
                        tiltY = (offset.x / size.width - 0.5f) * -15f
                        try {
                            awaitRelease()
                        } finally {
                            tiltX = 0f
                            tiltY = 0f
                        }
                    },
                    onTap = { onNavigate() },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isFlipped = !isFlipped
                    }
                )
            }
            .graphicsLayer {
                rotationX = animatedTiltX
                rotationY = rotation + animatedTiltY
                cameraDistance = 12 * density
                scaleX = if (animatedTiltX != 0f) 0.98f else 1f
                scaleY = if (animatedTiltY != 0f) 0.98f else 1f
            },
        shape = RoundedCornerShape(36.dp),
        elevation = 12.dp,
        alpha = 0.95f
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (rotation <= 90f) {
                // FRONT SIDE
                Box(Modifier.fillMaxSize()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(trimmedImageUrl)
                            .crossfade(true)
                            .setHeader("User-Agent", "Mozilla/5.0")
                            .build(),
                        contentDescription = art.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = animatedTiltX * 2f
                                translationX = -animatedTiltY * 2f
                            }
                            .sharedElement(
                                rememberSharedContentState(key = "art_image_${art.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CulturalShimmer(modifier = Modifier.fillMaxSize())
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                HeritageCream,
                                                HeritageGold.copy(alpha = 0.2f),
                                                HeritageCream
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        painter = painterResource(com.example.myapplication.R.drawable.placeholder),
                                        contentDescription = "Error loading image",
                                        tint = KarnatakaRed.copy(alpha = 0.3f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "HERITAGE ARCHIVE\nUNAVAILABLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = KarnatakaRed.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                            SideEffect {
                                Log.e("IMG_CHECK", "FAILED to load image for: ${art.name}, URL: $trimmedImageUrl")
                            }
                        }
                    )
                    
                    // Cinematic Gradient Overlay (Improved for readability)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Black.copy(alpha = 0.9f)
                                    ),
                                    startY = 200f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Surface(
                            color = KarnatakaRed,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = art.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = art.name,
                            style = MaterialTheme.typography.displayMedium.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 36.sp
                        )
                    }

                    // Floating Glass Actions
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glass Share
                            Surface(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Check out this amazing Art Form: ${art.name} on Karunada Kala! 🏺\n${art.description}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Art Form"))
                                },
                                modifier = Modifier.size(44.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            
                            Spacer(Modifier.width(12.dp))
                            
                            // Glass Like
                            Surface(
                                onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onLikeToggle() 
                                },
                                modifier = Modifier.size(44.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (art.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (art.isLiked) KarnatakaRed else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // BACK SIDE
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = art.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = art.description,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 6,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigate,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dive Deeper")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
