package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.PointMode
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.KarnatakaRed
import com.example.myapplication.ui.theme.TempleGreen
import com.example.myapplication.ui.theme.HeritageCream
import com.example.myapplication.ui.theme.HeritageGold
import com.example.myapplication.ui.state.UiState

/**
 * 🥇 6. LOADING → CONTENT ANIMATION (AI / Data)
 */
@Composable
fun LoadingToContent(
    isLoading: Boolean, 
    loadingPlaceholder: @Composable () -> Unit = { Text("Kala is thinking...") },
    content: @Composable () -> Unit
) {
    Crossfade(targetState = isLoading, label = "loadingToContent") { loading ->
        if (loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                loadingPlaceholder()
            }
        } else {
            content()
        }
    }
}

/**
 * 🌿 KALA BACKGROUND (Legacy Wrapper)
 */
@Composable
fun KalaBackground(
    content: @Composable () -> Unit
) {
    // Now just a simple box to maintain compatibility, 
    // real background is in AppBackgroundContainer
    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}

/**
 * 🥇 9. SHIMMER LOADING (Premium Heritage feel)
 */
@Composable
fun CulturalShimmer(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        HeritageCream.copy(alpha = alpha),
                        HeritageGold.copy(alpha = alpha * 0.5f),
                        HeritageCream.copy(alpha = alpha)
                    )
                ), 
                shape
            )
    )
}

/**
 * 🥇 1. CARD PRESS ANIMATION (Explore / Cards)
 */
@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            awaitRelease()
                        } finally {
                            pressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        content()
    }
}

/**
 * 🥇 2. LIKE BUTTON ANIMATION ❤️ (Chronicles)
 * Sequence: 1.0 -> 0.8 -> 1.3 -> 1.0
 */
@Composable
fun LikeButton(
    isLiked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
            scope.launch {
                scale.animateTo(0.8f, spring(stiffness = 500f))
                scale.animateTo(1.3f, spring(dampingRatio = 0.4f, stiffness = 600f)) // Celebratory burst
                scale.animateTo(1f, spring(stiffness = 400f))
            }
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (isLiked) Color(0xFFC34A2C) else Color.Gray, // Terracotta color
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * 🥇 3. BUTTON STATE ANIMATION (Join / Enroll / Register)
 */
@Composable
fun KalaAnimatedActionButton(
    text: String,
    successText: String,
    isLoading: Boolean,
    isSuccess: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    successColor: Color = Color(0xFF1D9E75)
) {
    val color by animateColorAsState(
        targetValue = if (isSuccess) successColor else containerColor,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "buttonColor"
    )

    Button(
        onClick = onClick,
        enabled = !isLoading && !isSuccess,
        modifier = modifier.bouncyClickable(onClick = onClick),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = if (isSuccess) successColor else color.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Crossfade(targetState = isLoading to isSuccess, label = "buttonContent") { (loading, success) ->
            when {
                loading -> UnrollingScrollAnimation(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                success -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(successText, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("✓", fontWeight = FontWeight.Bold)
                }
                else -> Text(text, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 🥇 4. FADE-IN ANIMATION (Cards / Lists)
 */
@Composable
fun FadeInItem(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(dampingRatio = 0.7f, stiffness = 400f)) + slideInVertically(
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
            initialOffsetY = { 20 }
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * 🥇 10. HAPTIC FEEDBACK (Feels real 🔥)
 */
fun Modifier.kalaClickable(
    hapticType: HapticFeedbackType = HapticFeedbackType.TextHandleMove, // Default to light tap
    onClick: () -> Unit
) = composed {
    this.bouncyClickable(onClick = onClick)
}

/**
 * 🥇 8. PARALLAX SCROLL (Explore Hero)
 */
fun Modifier.parallaxScroll(scrollState: ScrollState, factor: Float = 0.5f) = graphicsLayer {
    translationY = scrollState.value * factor
}

fun Modifier.parallaxScroll(lazyListState: LazyListState, factor: Float = 0.5f) = graphicsLayer {
    translationY = lazyListState.firstVisibleItemScrollOffset * factor
}

/**
 * Micro-interaction: Scale down on press
 */
fun Modifier.bounceClick() = composed { bouncyClickable(onClick = {}) }

/**
 * Subtle entrance animation for list items
 */
fun Modifier.entranceAnimation(delay: Int = 0) = composed {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "entranceAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "entranceTranslate"
    )

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    this
        .graphicsLayer(alpha = alpha, translationY = translateY)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalaFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    val haptic = LocalHapticFeedback.current
    
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) KarnatakaRed else Color.White.copy(alpha = 0.7f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "chipBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else TempleGreen.copy(alpha = 0.8f),
        label = "chipContent"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 12.dp else 2.dp,
        label = "chipElevation"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1.0f,
        label = "chipScale"
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        selected = selected,
        modifier = modifier
            .padding(end = 12.dp, bottom = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Text(text = icon, modifier = Modifier.padding(end = 8.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun KalaActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    isLoading: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.bounceClick(),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun <T> UiStateHandler(
    uiState: UiState<T>,
    onRetry: () -> Unit,
    loadingContent: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            UnrollingScrollAnimation(color = MaterialTheme.colorScheme.primary)
        }
    },
    emptyContent: @Composable () -> Unit = {
        DefaultEmptyState()
    },
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = uiState,
        label = "uiStateTransition",
        transitionSpec = {
            fadeIn(animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)) togetherWith 
            fadeOut(animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f))
        }
    ) { state ->
        when (state) {
            is UiState.Loading -> loadingContent()
            is UiState.Error -> ErrorState(message = state.message, onRetry = onRetry)
            is UiState.Success -> {
                val data = state.data
                if (data is List<*> && data.isEmpty()) {
                    emptyContent()
                } else {
                    content(data)
                }
            }
            else -> Box(Modifier.fillMaxSize())
        }
    }
}

@Composable
fun DefaultEmptyState(
    icon: String = "🏺",
    title: String = "The archives are quiet...",
    description: String = "Every masterpiece starts with a single step. Discover something new today!"
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Surface(
                modifier = Modifier.size(140.dp),
                color = HeritageGold.copy(alpha = 0.05f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 72.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = KarnatakaRed
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = TempleGreen.copy(alpha = 0.6f),
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            modifier = Modifier.padding(32.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text("⚠️", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Unable to unroll the story",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = KarnatakaRed
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    message.ifBlank { "Please check your connection and unroll the story again." },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = KarnatakaRed)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RETRY ARCHIVE", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
