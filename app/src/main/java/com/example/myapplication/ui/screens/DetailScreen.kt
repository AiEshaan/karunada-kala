package com.example.myapplication.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.ArtViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    name: String,
    description: String,
    imageUrl: String,
    artistId: String,
    category: String,
    navController: NavController,
    viewModel: ArtViewModel
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val firestore = FirebaseFirestore.getInstance()

    val aiDescriptions by viewModel.aiDescriptions.collectAsState()
    var entered by remember { mutableStateOf(false) }

    val flipIn by animateFloatAsState(
        targetValue = if (entered) 0f else -90f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "flipIn"
    )

    LaunchedEffect(Unit) {
        entered = true
        viewModel.addViewedArt(name)
        viewModel.generateAiDescriptionIfNeeded(name, description, category)
        firestore.collection("arts")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    firestore.collection("arts")
                        .document(document.id)
                        .update("viewCount", FieldValue.increment(1))
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer {
                rotationY = flipIn
                cameraDistance = 12 * density
            }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { Text(name, style = MaterialTheme.typography.headlineMedium) },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {

                // 🔥 IMMERSIVE HERO
                Box(modifier = Modifier.height(300.dp)) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.7f))
                                )
                            )
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 📄 DESCRIPTION AREA
                val displayDescription = aiDescriptions[name] ?: description
                val isAiEnhanced = aiDescriptions.containsKey(name)

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (isAiEnhanced) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("✨ AI Enhanced", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = displayDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 👁️ INSIGHTS CARD
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👁️", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "This is a highly trending heritage art form in Karnataka.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 🎓 CALL TO ACTION
                Button(
                    onClick = {
                        navController.navigate(NavRoutes.artistDetail(artistId))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("🎓 Learn from a Master Artist", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
