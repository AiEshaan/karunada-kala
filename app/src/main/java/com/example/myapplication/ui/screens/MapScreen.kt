package com.example.myapplication.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Workshop
import com.example.myapplication.ui.components.KalaActionButton
import com.example.myapplication.ui.components.KalaFilterChip
import com.example.myapplication.ui.model.MapItem
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.EventViewModel
import com.example.myapplication.viewmodel.MapViewModel
import com.example.myapplication.viewmodel.WorkshopViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.Clustering

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(
    navController: NavController,
    mapViewModel: MapViewModel = viewModel(),
    eventViewModel: EventViewModel = viewModel(),
    workshopViewModel: WorkshopViewModel = viewModel(),
    initialLat: Double? = null,
    initialLng: Double? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val mapItems by mapViewModel.mapItems.collectAsState()
    val stableItems = remember(mapItems) { mapItems.take(50) }
    val filterType by mapViewModel.selectedFilter.collectAsState()
    val isLoading by mapViewModel.isLoading.collectAsState()
    val userLocation by mapViewModel.userLocation.collectAsState()
    val nearestItem by mapViewModel.nearestItem.collectAsState()
    
    var selectedItem by remember { mutableStateOf<MapItem?>(null) }
    
    val karnatakaCenter = LatLng(15.3173, 75.7139)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(karnatakaCenter, 7f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            mapViewModel.requestUserLocation()
        }
    }

    val mapProperties by remember {
        mutableStateOf(MapProperties(isMyLocationEnabled = false, mapType = MapType.NORMAL))
    }

    // Handle deep link or initial position
    LaunchedEffect(initialLat, initialLng, mapItems) {
        if (initialLat != null && initialLng != null) {
            val target = LatLng(initialLat, initialLng)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(target, 14f),
                durationMs = 1000
            )
            // Auto-select the deep-linked item if it exists in the list
            val item = mapItems.find { it.lat == initialLat && it.lng == initialLng }
            if (item != null) {
                selectedItem = item
            }
        } else if (mapItems.isEmpty()) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(karnatakaCenter, 7f),
                durationMs = 1500
            )
        }
    }
    
    // 💡 Icons moved to be initialized safely inside GoogleMap or using a state that checks for initialization
    var isMapInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        mapViewModel.fetchData()
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFineLocation) {
            mapViewModel.requestUserLocation()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapLoaded = { isMapInitialized = true },
            onMapClick = { selectedItem = null },
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            if (isMapInitialized) {
                key(stableItems.size) {
                    Clustering(
                        items = stableItems,
                        onClusterItemClick = { item ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedItem = item
                            true
                        },
                        clusterItemContent = { item ->
                            val isDeepLinked = item.lat == initialLat && item.lng == initialLng

                            Marker(
                                state = rememberMarkerState(position = item.position),
                                alpha = if (isDeepLinked) 1.0f else 0.8f,
                                title = item.itemTitle,
                                zIndex = if (isDeepLinked) 1.0f else 0.0f
                            )
                        },
                        clusterContent = { cluster ->
                            val clusterSize = cluster.items.size
                            val items = cluster.items

                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                tonalElevation = 8.dp,
                                border = BorderStroke(2.dp, Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = clusterSize.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (items.any { it.type == "Artists" }) Text("🎭", fontSize = 8.sp)
                                        if (items.any { it.type == "Events" }) Text("🎪", fontSize = 8.sp)
                                        if (items.any { it.type == "Workshops" }) Text("🧑‍🏫", fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // ✨ Emotional Hook Overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Find stories, not just places",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        }

        // 📍 Nearby Auto-Alert
        nearestItem?.let { nearest ->
            AnimatedVisibility(
                visible = selectedItem == null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 160.dp)
            ) {
                Surface(
                    onClick = { 
                        selectedItem = nearest
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(nearest.position, 12f))
                    },
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Explore ${nearest.itemTitle} nearby",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 🎯 Filter Chips
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val filters = listOf("All", "Artists", "Events", "Workshops")
            filters.forEach { filter ->
                KalaFilterChip(
                    selected = filterType == filter,
                    onClick = { mapViewModel.setFilter(filter) },
                    label = filter
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // 🔥 Dynamic Bottom Sheet Card
        AnimatedVisibility(
            visible = selectedItem != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedItem?.let { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column {
                        val itemData = item.data
                        when (itemData) {
                            is Artist -> ArtistMapUI(itemData, navController) { selectedItem = null }
                            is Event -> EventMapUI(itemData, eventViewModel, navController) { selectedItem = null }
                            is Workshop -> WorkshopMapUI(itemData, workshopViewModel, navController) { selectedItem = null }
                            else -> {}
                        }
                        
                        TextButton(
                            onClick = {
                                when (itemData) {
                                    is Artist -> navController.navigate(NavRoutes.artistDetail(itemData.id))
                                    is Event -> {
                                        NavRoutes.navigateToDetail(
                                            navController,
                                            itemData.title,
                                            itemData.description,
                                            itemData.imageUrl,
                                            "none",
                                            "Event"
                                        )
                                    }
                                    is Workshop -> {
                                        NavRoutes.navigateToDetail(
                                            navController,
                                            itemData.title,
                                            "Learn from ${itemData.artistName}",
                                            itemData.imageUrl,
                                            itemData.artistId,
                                            "Workshop"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                        ) {
                            Text("View Full Details →", fontWeight = FontWeight.ExtraBold)
                        }
                        
                        KalaActionButton(
                            text = "Navigate to Location",
                            onClick = {
                                val uri = Uri.parse("google.navigation:q=${item.lat},${item.lng}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Google Maps not found", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).padding(bottom = 12.dp),
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistMapUI(artist: Artist, navController: NavController, onClose: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎭", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artist.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(artist.artType, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KalaActionButton(
                text = "Profile",
                onClick = { navController.navigate(NavRoutes.artistDetail(artist.id)) },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = {
                    val url = "https://api.whatsapp.com/send?phone=${artist.phone}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("WhatsApp")
            }
        }
    }
}

@Composable
fun EventMapUI(event: Event, viewModel: EventViewModel, navController: NavController, onClose: () -> Unit) {
    val registrationStatus by viewModel.registrationStatus.collectAsState()
    val isRegistered = registrationStatus[event.title] ?: false
    Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎪", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(event.date, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(event.description, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    NavRoutes.navigateToDetail(
                        navController,
                        event.title,
                        event.description,
                        event.imageUrl,
                        "none",
                        "Event"
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Full Story") }
            KalaActionButton(
                text = if (isRegistered) "Saved ✓" else "Join",
                onClick = { viewModel.register(event) },
                modifier = Modifier.weight(1f),
                enabled = !isRegistered,
                containerColor = if (isRegistered) Color(0xFF2E7D32) else Color(0xFFD4AF37)
            )
        }
    }
}

@Composable
fun WorkshopMapUI(workshop: Workshop, viewModel: WorkshopViewModel, navController: NavController, onClose: () -> Unit) {
    val enrollmentStatus by viewModel.enrollmentStatus.collectAsState()
    val isEnrolled = enrollmentStatus[workshop.id] ?: false
    Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🧑‍🏫", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(workshop.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${workshop.availableSlots} slots left", color = Color(0xFF008080), fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    NavRoutes.navigateToDetail(
                        navController,
                        workshop.title,
                        "Learn from ${workshop.artistName}. Art form: ${workshop.artType}. Fee: ₹${workshop.fee}",
                        workshop.imageUrl,
                        workshop.artistId,
                        "Workshop"
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Details") }
            KalaActionButton(
                text = if (isEnrolled) "Saved ✓" else "Enroll",
                onClick = { viewModel.enroll(workshop) },
                modifier = Modifier.weight(1f),
                enabled = !isEnrolled && workshop.availableSlots > 0,
                containerColor = if (isEnrolled) Color(0xFF2E7D32) else Color(0xFF008080)
            )
        }
    }
}
