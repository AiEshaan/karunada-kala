package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 🥇 1) Bouncy Clickable Modifier
 * Animates scale and elevation on press and triggers haptics.
 */
@Composable
fun Modifier.bouncyClickable(
    onClick: () -> Unit,
    haptic: Boolean = true
): Modifier {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 400f
        ),
        label = "bouncy"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        animationSpec = spring(stiffness = 400f),
        label = "elevation"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            shadowElevation = elevation.toPx()
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null // remove ripple
        ) {
            if (haptic) hapticFeedback.performHapticFeedback(
                HapticFeedbackType.TextHandleMove
            )
            onClick()
        }
}

/**
 * 🥇 2) Staggered Entrance Item
 * Choreographed entry for list items.
 */
@Composable
fun StaggeredItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = 400f
            )
        ) + fadeIn(spring(dampingRatio = 0.7f, stiffness = 400f))
    ) {
        content()
    }
}

/**
 * 🥇 3) Cultural Gold Shimmer
 * Premium shimmer effect with heritage colors.
 */
@Composable
fun CulturalShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "sweep"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFDF8F0).copy(alpha = 0.9f), // ArtBG
            Color(0xFFC9922A).copy(alpha = 0.3f), // Gold
            Color(0xFFFDF8F0).copy(alpha = 0.9f)
        ),
        start = androidx.compose.ui.geometry.Offset(sweep - 300f, 0f),
        end = androidx.compose.ui.geometry.Offset(sweep, 0f)
    )
    Box(modifier.background(brush))
}

/**
 * 🥇 4) Save/Bookmark Burst micro-interaction
 */
@Composable
fun BurstSaveButton(
    isSaved: Boolean,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var burst by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (burst) 1.4f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f, // overshoot!
            stiffness = 600f
        ),
        finishedListener = { burst = false },
        label = "burst"
    )
    val color by animateColorAsState(
        targetValue = if (isSaved) Color(0xFFC9922A) // Gold
        else Color.Gray,
        animationSpec = spring(stiffness = 400f),
        label = "color"
    )

    IconButton(
        onClick = {
            burst = true
            haptic.performHapticFeedback(
                HapticFeedbackType.LongPress
            )
            onToggle()
        }
    ) {
        Icon(
            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = color,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
    }
}

/**
 * 🥇 5) Morphing Button
 * A 3-state button that transforms from Idle to Loading to Success.
 */
enum class ButtonState { IDLE, LOADING, SUCCESS }

@Composable
fun MorphingButton(
    state: ButtonState,
    idleText: String,
    successText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    successColor: Color = Color(0xFF1D9E75)
) {
    val haptic = LocalHapticFeedback.current
    
    val color by animateColorAsState(
        targetValue = if (state == ButtonState.SUCCESS) successColor else containerColor,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "buttonColor"
    )

    LaunchedEffect(state) {
        if (state == ButtonState.SUCCESS) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Button(
        onClick = onClick,
        enabled = state == ButtonState.IDLE,
        modifier = modifier.bouncyClickable(onClick = onClick),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = if (state == ButtonState.SUCCESS) successColor else color.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                if (targetState == ButtonState.SUCCESS) {
                    (fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f)))
                        .togetherWith(fadeOut() + scaleOut())
                } else {
                    (fadeIn() + scaleIn(initialScale = 0.9f))
                        .togetherWith(fadeOut() + scaleOut())
                }
            },
            label = "buttonStateTransform"
        ) { targetState ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                when (targetState) {
                    ButtonState.IDLE -> {
                        Text(
                            text = idleText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    ButtonState.LOADING -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    ButtonState.SUCCESS -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = successText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🥇 6) Count-Up Animation
 * Animate numbers from 0 to target on appearance.
 */
@Composable
fun CountUpText(
    targetValue: Int,
    prefix: String = "",
    suffix: String = "",
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = Color.Unspecified
) {
    var count by remember { mutableIntStateOf(0) }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f), // Heavy spring for counting
        label = "countUp"
    )

    LaunchedEffect(targetValue) {
        count = targetValue
    }

    Text(
        text = "$prefix$animatedCount$suffix",
        style = style,
        color = color
    )
}

/**
 * 🥇 7) Pulse Animation
 * Infinite scale pulse for active markers or badges.
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * 🥇 8) Morphing Ring Progress
 * A premium loading ring that morphs between shapes.
 */
@Composable
fun MorphingRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ringMorph")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "ringRotation"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 4.dp,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
/**
 * 🥇 9) Unrolling Scroll Animation
 * A premium cultural loading state using path drawing.
 */
@Composable
fun UnrollingScrollAnimation(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val transition = rememberInfiniteTransition(label = "scrollUnroll")
    val unrollAmount by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "unrollAmount"
    )

    androidx.compose.foundation.Canvas(modifier = modifier.size(64.dp)) {
        val width = size.width
        val height = size.height
        val strokeWidth = 4.dp.toPx()
        
        // Scroll handles (circles)
        drawCircle(color = color, radius = 8.dp.toPx(), center = androidx.compose.ui.geometry.Offset(width / 2, 8.dp.toPx()))
        drawCircle(color = color, radius = 8.dp.toPx(), center = androidx.compose.ui.geometry.Offset(width / 2, 8.dp.toPx() + (height - 16.dp.toPx()) * unrollAmount))

        // Connecting lines
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(width / 2 - 8.dp.toPx(), 8.dp.toPx()),
            end = androidx.compose.ui.geometry.Offset(width / 2 - 8.dp.toPx(), 8.dp.toPx() + (height - 16.dp.toPx()) * unrollAmount),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(width / 2 + 8.dp.toPx(), 8.dp.toPx()),
            end = androidx.compose.ui.geometry.Offset(width / 2 + 8.dp.toPx(), 8.dp.toPx() + (height - 16.dp.toPx()) * unrollAmount),
            strokeWidth = strokeWidth
        )
    }
}
