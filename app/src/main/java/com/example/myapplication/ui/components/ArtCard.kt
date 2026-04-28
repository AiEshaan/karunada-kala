package com.example.myapplication.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.model.ArtForm

@Composable
fun ArtCard(
    art: ArtForm,
    listState: LazyListState,
    onNavigate: () -> Unit
) {
    val context = LocalContext.current
    var flipped by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "pressScale")

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 90f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "cardFlip"
    )

    // 🔥 Navigation trigger
    LaunchedEffect(rotation) {
        if (rotation >= 89f && !hasNavigated) {
            hasNavigated = true
            onNavigate()
        }
    }
    
    // Reset state when we return to this screen
    LaunchedEffect(flipped) {
        if (!flipped) {
            hasNavigated = false
        }
    }

    // Parallax effect logic
    val offset = try { listState.firstVisibleItemScrollOffset } catch (e: Exception) { 0 }
    val parallaxTranslation = (offset * 0.03f).coerceAtMost(40f)

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(240.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { flipped = true }
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box {
            // 🔥 Background Image with Parallax
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(art.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = art.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = parallaxTranslation },
                placeholder = rememberVectorPainter(Icons.Default.Info),
                error = rememberVectorPainter(Icons.Default.Warning)
            )

            // 🌑 Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // 📝 Text Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                if (art.artistName.isNotEmpty()) {
                    Text(
                        text = "Contributing: ${art.artistName}",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Text(
                    text = art.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = art.description,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            // 🏷 Category Chip
            if (art.category.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    color = Color(0xFFD4AF37),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = art.category,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
