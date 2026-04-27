package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
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
                Marker(
                    state = MarkerState(
                        position = LatLng(artist.lat, artist.lng)
                    ),
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
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(50),
            tonalElevation = 4.dp
        ) {
            Text(
                text = "${artistList.size} Cultural Guardians nearby",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
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
                                    color = Color(0xFFD4AF37), // Gold accent
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { selectedArtist = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }

                        if (artist.bio.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = artist.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                navController.navigate(NavRoutes.artistDetail(artist.id))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View Artist Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
