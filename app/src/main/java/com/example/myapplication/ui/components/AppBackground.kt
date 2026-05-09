package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import com.example.myapplication.ui.theme.SoftGreen
import com.example.myapplication.ui.theme.SoftWhiteGlow
import com.example.myapplication.ui.theme.WarmCream

val AppBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF5F1E8), // warm cream
        Color(0xFFE6F2EC)  // light green tint
    )
)

@Composable
fun AppBackgroundContainer(
    textureAlpha: Float = 0.04f,
    showMotion: Boolean = true,
    overlayBrush: Brush? = null,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "backgroundMotion")
    
    val motionOffset by if (showMotion) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "gradientOffset"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Phase 1 & 5: Base Gradient & Motion
                val brush = if (showMotion) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF5F1E8), // warm cream
                            Color(0xFFE6F2EC)  // light green tint
                        ),
                        start = Offset(0f, motionOffset),
                        end = Offset(motionOffset, 0f)
                    )
                } else {
                    AppBackgroundBrush
                }
                drawRect(brush = brush)
            }
    ) {
        // Phase 2: Cultural Texture Layer
        Image(
            painter = painterResource(R.drawable.karnataka_texture),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(textureAlpha),
            contentScale = ContentScale.Crop
        )

        // Phase 1: Soft Glow Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Optional Overlay (for Phase 4 screen tuning)
        if (overlayBrush != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayBrush)
            )
        }

        // Subtle Noise Texture for premium feel
        GrainTexture(alpha = 0.03f)

        content()
    }
}

@Composable
fun GrainTexture(alpha: Float = 0.05f) {
    val grainPoints = remember {
        val points = mutableListOf<Offset>()
        val step = 30 // Increased from 10 to 30 for 9x performance boost
        val random = java.util.Random(42)
        for (x in 0..2000 step step) {
            for (y in 0..4000 step step) {
                if (random.nextInt(10) > 7) {
                    points.add(Offset(x.toFloat(), y.toFloat()))
                }
            }
        }
        points
    }

    val color = Color.Gray
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .graphicsLayer(renderEffect = null) // Ensure hardware acceleration
    ) {
        drawPoints(
            points = grainPoints,
            pointMode = PointMode.Points,
            color = color,
            strokeWidth = 2f // Slightly thicker since there are fewer points
        )
    }
}
