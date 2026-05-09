package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object KalaElevation {
    val Low = 4.dp
    val Medium = 10.dp
    val High = 20.dp
}

@Composable
fun KalaGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 8.dp, // Phase 3: Floating feel
    alpha: Float = 0.85f, // Phase 3: Translucent cards
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = if (onClick != null) modifier.bounceClick().clickable { onClick() } else modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = alpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(content = content)
    }
}
