package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.KarnatakaRed
import com.example.myapplication.ui.theme.HeritageCream

@Composable
fun KalaGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(32.dp),
    elevation: Dp = 8.dp,
    alpha: Float = 0.95f,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = if (onClick != null) {
            modifier
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .bouncyClickable(onClick = onClick)
                .clip(shape)
        } else {
            modifier
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(shape)
        },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = alpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            // Subtle Heritage Glow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.05f)
                    .background(
                        Brush.verticalGradient(
                            listOf(KarnatakaRed, Color.Transparent)
                        )
                    )
            )
            
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}
