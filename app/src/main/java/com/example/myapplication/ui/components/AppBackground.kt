package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import com.example.myapplication.ui.theme.SoftGreen
import com.example.myapplication.ui.theme.SoftWhiteGlow
import com.example.myapplication.ui.theme.WarmCream

val AppGradientBackground = Brush.verticalGradient(
    colors = listOf(
        WarmCream,
        SoftGreen
    )
)

@Composable
fun AppBackgroundContainer(
    textureAlpha: Float = 0.04f,
    showMotion: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundMotion")
    
    val motionOffset by if (showMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                tween(30000, easing = LinearEasing),
                RepeatMode.Reverse
            ),
            label = "gradientOffset"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val backgroundBrush = if (showMotion) {
        Brush.linearGradient(
            colors = listOf(WarmCream, SoftGreen, WarmCream),
            start = Offset(0f, motionOffset),
            end = Offset(motionOffset, 1500f)
        )
    } else {
        AppGradientBackground
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // 🌿 Cultural texture layer (Phase 2)
        Image(
            painter = painterResource(R.drawable.karnataka_texture),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(textureAlpha),
            contentScale = ContentScale.Crop
        )

        // ✨ Soft radial glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SoftWhiteGlow.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(200f, 200f),
                        radius = 1500f
                    )
                )
        )

        // Subtle Noise Texture for premium feel
        GrainTexture(alpha = 0.03f)

        content()
    }
}

@Composable
fun GrainTexture(alpha: Float = 0.05f) {
    val grainPoints = remember {
        val points = mutableListOf<Offset>()
        val step = 10 
        val random = java.util.Random(42)
        for (x in 0..2000 step step) {
            for (y in 0..4000 step step) {
                if (random.nextInt(10) > 8) {
                    points.add(Offset(x.toFloat(), y.toFloat()))
                }
            }
        }
        points
    }

    val color = Color.Gray
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().alpha(alpha)) {
        drawPoints(
            points = grainPoints,
            pointMode = PointMode.Points,
            color = color,
            strokeWidth = 1f
        )
    }
}
