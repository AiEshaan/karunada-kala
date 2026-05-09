package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KalaAmbientInsight(
    insight: String?,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(insight) {
        if (insight != null) {
            kotlinx.coroutines.delay(1500) // Delay to let screen settle
            visible = true
            kotlinx.coroutines.delay(10000) // Visible for 10 seconds
            visible = false
        } else {
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible && insight != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier.padding(16.dp)
    ) {
        insight?.let {
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF8B4513).copy(alpha = 0.9f), // Heritage Brown
                                    Color(0xFFD4AF37).copy(alpha = 0.9f)  // Gold
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
