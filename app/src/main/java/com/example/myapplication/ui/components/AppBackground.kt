package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R
import com.example.myapplication.ui.theme.HeritageCream
import com.example.myapplication.ui.theme.SoftParchment

@Composable
fun AppBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundAnimation")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HeritageCream,
                        SoftParchment,
                        HeritageCream
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.karnataka_texture),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = offset * 0.02f
                },
            contentScale = ContentScale.Crop,
            alpha = 0.05f
        )
    }
}

@Composable
fun AppBackgroundContainer(
    modifier: Modifier = Modifier,
    textureAlpha: Float = 0.05f,
    showMotion: Boolean = true,
    overlayBrush: Brush? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        AppBackground()
        overlayBrush?.let {
            Box(modifier = Modifier.fillMaxSize().background(it))
        }
        content()
    }
}
