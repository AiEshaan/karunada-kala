package com.example.myapplication.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.ui.theme.HeritageCream
import com.example.myapplication.ui.theme.HeritageGold
import com.example.myapplication.ui.theme.KarnatakaRed
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 🌀 GyroParallaxHero
 * A world-class motion component that uses the device's rotation sensors
 * to create a subtle 3D depth effect on hero images.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GyroParallaxHero(
    imageUrl: String,
    modifier: Modifier = Modifier,
    scrollOffset: Float = 0f,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedKey: String? = null
) {
    val context = LocalContext.current
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorMgr = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        
        var filteredX = 0f
        var filteredY = 0f
        val alpha = 0.1f // low-pass filter strength

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Low-pass filter to prevent jitter
                filteredX = alpha * event.values[1] + (1 - alpha) * filteredX
                filteredY = alpha * event.values[0] + (1 - alpha) * filteredY
                // Map to ±15px movement range
                tiltX = (filteredX * 150f).coerceIn(-15f, 15f)
                tiltY = (filteredY * 150f).coerceIn(-15f, 15f)
            }
            override fun onAccuracyChanged(s: Sensor, i: Int) {}
        }
        sensorMgr.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorMgr.unregisterListener(listener) }
    }

    val animX by animateFloatAsState(tiltX, spring(0.6f, 200f), label = "gyroX")
    val animY by animateFloatAsState(tiltY, spring(0.6f, 200f), label = "gyroY")

    val trimmedImageUrl = imageUrl.trim()

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(trimmedImageUrl)
            .crossfade(true)
            .setHeader("User-Agent", "Mozilla/5.0")
            .build(),
        contentDescription = null,
        modifier = if (sharedKey != null) {
            modifier
                .sharedElement(
                    rememberSharedContentState(key = sharedKey),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .graphicsLayer {
                    translationX = animX
                    translationY = animY + (scrollOffset * 0.5f)
                    scaleX = 1.15f
                    scaleY = 1.15f
                }
        } else {
            modifier.graphicsLayer {
                translationX = animX
                translationY = animY + (scrollOffset * 0.5f)
                scaleX = 1.15f
                scaleY = 1.15f
            }
        },
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(com.example.myapplication.R.drawable.placeholder),
                        contentDescription = "Error loading image",
                        tint = KarnatakaRed.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "CULTURAL ARCHIVE OFFLINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = KarnatakaRed.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    )
}
