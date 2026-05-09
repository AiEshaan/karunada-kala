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
import androidx.compose.animation.*

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

    AsyncImage(
        model = imageUrl,
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
        contentScale = ContentScale.Crop
    )
}
