package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.KarnatakaRed
import com.example.myapplication.ui.theme.TempleGreen

@Composable
fun ExploreHeroSection() {
    val infinite = rememberInfiniteTransition(label = "heroFloat")

    val floatY by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .graphicsLayer {
                translationY = floatY
            }
    ) {
        Text(
            text = "HERITAGE OF KARNATAKA",
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = KarnatakaRed,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "KARUNADA",
            style = MaterialTheme.typography.displayLarge,
            color = KarnatakaRed,
            lineHeight = 42.sp
        )

        Text(
            text = "KALA",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
            color = KarnatakaRed.copy(alpha = 0.8f),
            letterSpacing = 6.sp,
            modifier = Modifier.offset(y = (-8).dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Living heritage archives of Karnataka",
            style = MaterialTheme.typography.titleLarge,
            color = TempleGreen.copy(alpha = 0.8f),
            lineHeight = 28.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
