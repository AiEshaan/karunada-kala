package com.example.myapplication.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.myapplication.ui.components.MorphingButton
import com.example.myapplication.ui.components.ButtonState
import com.example.myapplication.ui.components.KalaActionButton
import com.example.myapplication.ui.components.KalaFilterChip
import com.example.myapplication.ui.model.MapItem
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.theme.KarnatakaRed
import com.example.myapplication.ui.theme.KarnatakaYellow
import com.example.myapplication.ui.theme.TempleGreen
import com.example.myapplication.viewmodel.EventViewModel
import com.example.myapplication.viewmodel.MapViewModel
import com.example.myapplication.ui.components.AppBackgroundContainer
import com.example.myapplication.ui.theme.HeritageCream
import com.example.myapplication.viewmodel.WorkshopViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.Clustering

@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
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
    
    // 🏺 Ancient Parchment Map Style (Refined)
    val parchmentMapStyle = remember {
        try {
            context.resources.openRawResource(com.example.myapplication.R.raw.map_style).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            // Fallback to minimal parchment if resource loading fails
            """
            [
              { "featureType": "all", "elementType": "labels.text.fill", "stylers": [ { "color": "#8b4513" } ] },
              { "featureType": "landscape", "elementType": "geometry", "stylers": [ { "color": "#f7e7c0" } ] },
              { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#9dbbb1" } ] }
            ]
            """.trimIndent()
        }
    }
    val mapItems by mapViewModel.mapItems.collectAsState()
    val filterType by mapViewModel.selectedFilter.collectAsState()
    val isLoading by mapViewModel.isLoading.collectAsState()
    val nearestItem by mapViewModel.nearestItem.collectAsState()
    val userLocation by mapViewModel.userLocation.collectAsState()
    
    var selectedItem by remember { mutableStateOf<MapItem?>(null) }
    var hasDeepLinked by remember { mutableStateOf(false) }
    
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

    val mapProperties = remember(parchmentMapStyle) {
        MapProperties(
            isMyLocationEnabled = false, 
            mapType = MapType.NORMAL,
            mapStyleOptions = MapStyleOptions(parchmentMapStyle)
        )
    }

    // Handle deep link or initial position safely
    LaunchedEffect(initialLat, initialLng, mapItems) {
        if (!hasDeepLinked && initialLat != null && initialLng != null && initialLat.isFinite() && initialLng.isFinite()) {
            val target = LatLng(initialLat, initialLng)
            try {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(target, 14f),
                    durationMs = 1000
                )
            } catch (e: Exception) {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(target, 14f))
            }
            // Auto-select the deep-linked item if it exists in the list
            val item = mapItems.find { 
                it.lat == initialLat && it.lng == initialLng 
            }
            if (item != null) {
                selectedItem = item
                hasDeepLinked = true
            }
        }
    }
    
    // 🎥 Cinematic 3D Camera Sweep
    LaunchedEffect(selectedItem) {
        selectedItem?.let { item ->
            val targetPosition = CameraPosition.builder()
                .target(item.position)
                .zoom(15f)
                .tilt(45f) // 3D Tilt
                .bearing(30f) // Slight rotation
                .build()
            
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(targetPosition),
                durationMs = 1500
            )
        }
    }
    
    LaunchedEffect(Unit) {
        mapViewModel.fetchData()
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFineLocation) {
            mapViewModel.requestUserLocation()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    
    AppBackgroundContainer(
        textureAlpha = 0f,
        showMotion = false,
        overlayBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFdfdfd))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = { 
                        Column {
                            Text("CULTURAL NETWORK", style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp, color = KarnatakaRed)
                            Text("Artisan Map", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TempleGreen)
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = HeritageCream.copy(alpha = 0.95f),
                        titleContentColor = KarnatakaRed
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            onMapClick = { selectedItem = null },
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false, // We use our custom one
                compassEnabled = true
            )
        ) {
            Clustering(
                items = mapItems,
                onClusterItemClick = { item ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedItem = item
                    true
                },
                clusterItemContent = { item ->
                    val isDeepLinked = initialLat != null && item.lat == initialLat && item.lng == initialLng
                    val markerColor = when (item.type) {
                        "Artists" -> Color(0xFF8B4513)   // Warm brown
                        "Events" -> KarnatakaRed         // National Pride Red
                        "Workshops" -> KarnatakaYellow   // National Pride Yellow
                        else -> Color(0xFF6200EE)
                    }
                    val emoji = when (item.type) {
                        "Artists" -> "🏺"
                        "Events" -> "🎭"
                        "Workshops" -> "🎓"
                        else -> "📍"
                    }

                    Surface(
                        modifier = Modifier.size(if (isDeepLinked) 48.dp else 36.dp),
                        shape = CircleShape,
                        color = markerColor,
                        tonalElevation = if (isDeepLinked) 12.dp else 4.dp,
                        border = BorderStroke(
                            if (isDeepLinked) 3.dp else 2.dp,
                            Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = if (isDeepLinked) 20.sp else 14.sp)
                        }
                    }
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
            val filters = listOf("All", "Artists", "Performances", "Workshops")
            filters.forEach { filter ->
                val displayFilter = if (filter == "Performances") "Events" else filter // Map internal state
                val isSelected = (filter == "Performances" && filterType == "Events") || (filterType == filter)
                
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                    label = "chipScale"
                )
                Box(modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }) {
                    KalaFilterChip(
                        selected = isSelected,
                        onClick = { 
                            if (filter == "Performances") mapViewModel.setFilter("Events")
                            else mapViewModel.setFilter(filter)
                        },
                        label = filter
                    )
                }
            }
        }

        // 🧭 Professional Recenter/Compass FAB
        val scope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        // 1. Get current location or wait for it
                        val location = mapViewModel.getUserLocation()
                        
                        // 2. Animate Camera to user or fallback to Karnataka center
                        val target = location?.let { LatLng(it.latitude, it.longitude) } ?: karnatakaCenter
                        val zoom = if (location != null) 15f else 7f
                        
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newCameraPosition(
                                CameraPosition.builder()
                                    .target(target)
                                    .zoom(zoom)
                                    .tilt(0f)
                                    .bearing(0f)
                                    .build()
                            ),
                            durationMs = 1200
                        )
                    }
                },
                containerColor = HeritageCream,
                contentColor = KarnatakaRed,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter to My Location",
                    modifier = Modifier.size(28.dp)
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
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
            ) + fadeOut(),
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
            MorphingButton(
                state = if (isRegistered) ButtonState.SUCCESS else ButtonState.IDLE,
                idleText = "Join",
                successText = "Saved ✓",
                onClick = { viewModel.register(event) },
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFD4AF37),
                successColor = Color(0xFF1D9E75)
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
                text = "Sign up",
                onClick = { navController.navigate(NavRoutes.workshopRegistration(workshop.id, workshop.title)) },
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFF008080)
            )
        }
    }
}
