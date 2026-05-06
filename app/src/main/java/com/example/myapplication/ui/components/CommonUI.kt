package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.ScrollState
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
import com.example.myapplication.ui.state.UiState

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
 * 🥇 9. SHIMMER LOADING (Premium feel)
 */
@Composable
fun KalaShimmer(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .background(Color.Gray.copy(alpha = alpha), shape)
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
 */
@Composable
fun LikeButton(
    isLiked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isLiked) {
        if (isLiked) {
            scale.animateTo(1.3f, tween(150))
            scale.animateTo(1f, tween(150))
        }
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (isLiked) Color.Red else Color.Gray,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
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
    successColor: Color = Color(0xFF4CAF50)
) {
    val color by animateColorAsState(
        targetValue = if (isSuccess) successColor else containerColor,
        animationSpec = tween(400),
        label = "buttonColor"
    )

    Button(
        onClick = onClick,
        enabled = !isLoading && !isSuccess,
        modifier = modifier.bounceClick(),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = if (isSuccess) successColor else color.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Crossfade(targetState = isLoading to isSuccess, label = "buttonContent") { (loading, success) ->
            when {
                loading -> CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
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
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500, delayMillis)) + slideInVertically(
            animationSpec = tween(500, delayMillis),
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
    hapticType: HapticFeedbackType = HapticFeedbackType.LongPress,
    onClick: () -> Unit
) = composed {
    val haptic = LocalHapticFeedback.current
    this.clickable {
        haptic.performHapticFeedback(hapticType)
        onClick()
    }
}

/**
 * 🥇 8. PARALLAX SCROLL (Explore Hero)
 */
fun Modifier.parallaxScroll(scrollState: ScrollState, factor: Float = 0.5f) = graphicsLayer {
    translationY = scrollState.value * factor
}

/**
 * Micro-interaction: Scale down on press
 */
fun Modifier.bounceClick() = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bounceScale"
    )

    this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isPressed = false
                    }
                }
            )
        }
}

/**
 * Subtle entrance animation for list items
 */
fun Modifier.entranceAnimation(delay: Int = 0) = composed {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = delay),
        label = "entranceAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "entranceTranslate"
    )

    LaunchedEffect(Unit) {
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
    FilterChip(
        selected = selected,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        label = {
            Text(
                text = if (icon != null) "$icon $label" else label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier.padding(end = 8.dp).bounceClick(),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White,
            labelColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(24.dp),
        border = null
    )
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
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
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
            modifier = Modifier.padding(32.dp)
        ) {
            Text(icon, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("⚠️", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}
