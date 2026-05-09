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
import androidx.compose.material.icons.filled.CalendarToday
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
import coil.compose.AsyncImage

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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        KalaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = KalaElevation.Medium,
            alpha = 0.85f
        ) {
            Column {
                if (imageUrl.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(com.example.myapplication.R.drawable.placeholder),
                            error = painterResource(com.example.myapplication.R.drawable.placeholder)
                        )
                        
                        // Urgency Badge
                        com.example.myapplication.ui.components.PulseAnimation(
                            modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔥 Filling Fast",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                        startY = 100f
                                    )
                                )
                        )
                    }
                }
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row {
                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Join me for $title, a $artType event in Karnataka! Discover more on Karunada Kala.")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Event"))
                            }) {
                                Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = CalendarContract.Events.CONTENT_URI
                                    putExtra(CalendarContract.Events.TITLE, title)
                                    putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                                    putExtra(CalendarContract.Events.DESCRIPTION, "A traditional Karnataka event focusing on $artType.")
                                    
                                    try {
                                        val dateParts = date.split("-").map { it.trim() }
                                        val firstDateStr = dateParts[0]
                                        val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                        
                                        val startDate = sdf.parse(firstDateStr)
                                        startDate?.let {
                                            val cal = java.util.Calendar.getInstance()
                                            cal.time = it
                                            cal.set(java.util.Calendar.YEAR, currentYear)
                                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                                            
                                            if (dateParts.size > 1) {
                                                val endDate = sdf.parse(dateParts[1])
                                                endDate?.let { end ->
                                                    cal.time = end
                                                    cal.set(java.util.Calendar.YEAR, currentYear)
                                                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis)
                                                }
                                            } else {
                                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 3600000)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("EventCard", "Failed to parse date: $date", e)
                                    }
                                }
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📅", fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📍", fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onViewOnMap,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View on Map", fontWeight = FontWeight.Bold)
                        }
                        
                        val buttonState = when {
                            isRegistered -> ButtonState.SUCCESS
                            isRegistering -> ButtonState.LOADING
                            else -> ButtonState.IDLE
                        }

                        MorphingButton(
                            state = buttonState,
                            idleText = "Register Interest",
                            successText = "Registered",
                            onClick = onRegister,
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            successColor = Color(0xFF1D9E75)
                        )
                    }
                }
            }
        }
    }
}
