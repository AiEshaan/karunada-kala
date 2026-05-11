package com.example.myapplication.ui.components

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.myapplication.ui.theme.KarnatakaRed
import com.example.myapplication.ui.theme.HeritageGold
import com.example.myapplication.ui.theme.HeritageCream
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun EventCard(
    title: String,
    date: String,
    location: String,
    artType: String,
    imageUrl: String = "",
    isRegistered: Boolean = false,
    isRegistering: Boolean = false,
    onRegister: () -> Unit = {},
    onViewOnMap: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        KalaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 12.dp,
            shape = RoundedCornerShape(36.dp),
            alpha = 0.95f
        ) {
            Column {
                if (imageUrl.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl.trim())
                                .crossfade(true)
                                .setHeader("User-Agent", "Mozilla/5.0")
                                .build(),
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                CulturalShimmer(modifier = Modifier.fillMaxSize())
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    HeritageCream,
                                                    HeritageGold.copy(alpha = 0.2f),
                                                    HeritageCream
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(com.example.myapplication.R.drawable.placeholder),
                                        contentDescription = "Error loading image",
                                        tint = KarnatakaRed.copy(alpha = 0.3f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        )
                        
                        // Premium Glass Date Badge
                        Surface(
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopEnd),
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val dateParts = date.split(" ").filter { it.isNotBlank() }
                                if (dateParts.size >= 2) {
                                    Text(
                                        text = dateParts[0].uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = dateParts[1],
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                } else {
                                    Text(date, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                        startY = 100f
                                    )
                                )
                        )
                    }
                }
                
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = artType.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = HeritageGold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 30.sp
                            )
                        }
                        
                        Row {
                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Join me for $title, a $artType event in Karnataka! Discover more on Karunada Kala.")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Event"))
                            }, modifier = Modifier.background(Color.Black.copy(0.05f), CircleShape)) {
                                Icon(Icons.Default.Share, null, tint = KarnatakaRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = KarnatakaRed.copy(0.6f), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onViewOnMap,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            border = BorderStroke(1.dp, KarnatakaRed.copy(alpha = 0.3f))
                        ) {
                            Text("Map View", fontWeight = FontWeight.Bold, color = KarnatakaRed)
                        }
                        
                        val registerColor by animateColorAsState(
                            targetValue = if (isRegistered) Color(0xFF1D9E75) else KarnatakaRed,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                            label = "registerColor"
                        )
                        
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onRegister()
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .graphicsLayer {
                                    scaleX = if (isRegistering) 0.95f else 1f
                                    scaleY = if (isRegistering) 0.95f else 1f
                                },
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = registerColor,
                                disabledContainerColor = registerColor.copy(alpha = 0.5f)
                            ),
                            enabled = !isRegistering
                        ) {
                            if (isRegistering) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                AnimatedContent(targetState = isRegistered, label = "regIcon") { registered ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (registered) Icons.Default.Check else Icons.Default.ConfirmationNumber, 
                                            null, 
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            if (registered) "JOINED" else "GET PASS", 
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
