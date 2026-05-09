package com.example.myapplication.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.KalaFilterChip
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.EventViewModel
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AppBackgroundContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    id: String,
    title: String,
    description: String,
    date: String,
    location: String,
    imageUrl: String,
    artType: String,
    lat: Double,
    lng: Double,
    navController: NavController,
    viewModel: EventViewModel = viewModel()
) {
    val context = LocalContext.current
    val registrationStatus by viewModel.registrationStatus.collectAsState()
    val isRegistered = registrationStatus[title] ?: false
    val isRegistering by viewModel.isRegistering.collectAsState()

    AppBackgroundContainer(textureAlpha = 0.03f) {
        Scaffold(
            containerColor = Color.Transparent,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Join me for $title, a $artType event in Karnataka! Discover more on Karunada Kala.")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Event"))
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Share")
                    }

                    Button(
                        onClick = {
                            // Use a dummy event object for registration since we only need title and id
                            viewModel.register(com.example.myapplication.data.model.Event(id = id, title = title))
                        },
                        modifier = Modifier.weight(2f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isRegistered && !isRegistering,
                        colors = if (isRegistered) {
                            ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        } else {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        }
                    ) {
                        if (isRegistering) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(if (isRegistered) "Registered ✓" else "Register Interest", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image
            Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(com.example.myapplication.R.drawable.placeholder),
                    error = painterResource(com.example.myapplication.R.drawable.placeholder)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 300f
                            )
                        )
                )
                
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    KalaFilterChip(
                        selected = true,
                        onClick = {},
                        label = artType,
                        icon = "✨"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, title)
                            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.White)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(date, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(location, style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(Modifier.height(32.dp))
                
                Text("About this Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(Modifier.height(32.dp))
                
                Text("Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                // Embedded Map Placeholder (Since we don't have a mini map component yet, we navigate to the main map)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { navController.navigate(NavRoutes.map(lat, lng)) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Get API Key from Manifest metadata
                        val appInfo = context.packageManager.getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
                        val mapsKey = appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
                        
                        AsyncImage(
                            model = "https://maps.googleapis.com/maps/api/staticmap?center=$lat,$lng&zoom=15&size=600x300&markers=color:red%7C$lat,$lng&key=$mapsKey",
                            contentDescription = "Map Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(com.example.myapplication.R.drawable.placeholder),
                            error = painterResource(com.example.myapplication.R.drawable.placeholder)
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                "View on Interactive Map",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
