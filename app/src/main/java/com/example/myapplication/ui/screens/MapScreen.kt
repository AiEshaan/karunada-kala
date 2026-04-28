package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Artist
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.ArtistViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: ArtistViewModel = viewModel()
) {
    val artistList by viewModel.artists.collectAsState()
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    val karnatakaCenter = LatLng(15.3173, 75.7139)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(karnatakaCenter, 6f)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchArtists()
        // Smooth cinematic zoom-in to Karnataka center
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(karnatakaCenter, 7f),
            durationMs = 1500
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { selectedArtist = null },
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            artistList.forEach { artist ->
                val markerColor = when (artist.artType) {
                    "Dance" -> BitmapDescriptorFactory.HUE_RED
                    "Music" -> BitmapDescriptorFactory.HUE_AZURE
                    "Craft" -> BitmapDescriptorFactory.HUE_ORANGE
                    "Painting" -> BitmapDescriptorFactory.HUE_VIOLET
                    else -> BitmapDescriptorFactory.HUE_YELLOW
                }

                Marker(
                    state = MarkerState(
                        position = LatLng(artist.lat, artist.lng)
                    ),
                    icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                    title = artist.name,
                    snippet = artist.artType,
                    onClick = {
                        selectedArtist = artist
                        false // return false to show default info window too
                    }
                )
            }
        }

        // ✨ Top Info Chip
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f)),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏰", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${artistList.size} Cultural Guardians in Karnataka",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 🔥 Bottom Sheet Interaction
        AnimatedVisibility(
            visible = selectedArtist != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedArtist?.let { artist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = artist.artType,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            IconButton(
                                onClick = { selectedArtist = null },
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    CircleShape
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (artist.bio.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = artist.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                lineHeight = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                navController.navigate(NavRoutes.artistDetail(artist.id))
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Explore Art of ${artist.name.split(" ").first()}", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}
