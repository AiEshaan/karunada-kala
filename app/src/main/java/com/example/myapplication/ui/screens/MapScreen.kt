package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.ArtistViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(viewModel: ArtistViewModel = viewModel()) {

    val artistList by viewModel.artists.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchArtists()
    }

    val defaultLocation = LatLng(12.9716, 77.5946) // Bangalore

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(defaultLocation, 6f)
        }
    ) {
        artistList.forEach { artist ->
            Marker(
                state = MarkerState(
                    position = LatLng(artist.lat, artist.lng)
                ),
                title = artist.name,
                snippet = artist.artType
            )
        }
    }
}