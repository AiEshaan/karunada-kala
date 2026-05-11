package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.model.Event
import com.example.myapplication.ui.components.DefaultEmptyState
import com.example.myapplication.ui.components.EventCard
import com.example.myapplication.ui.components.EventCardShimmer
import com.example.myapplication.ui.components.KalaFilterChip
import com.example.myapplication.ui.components.UiStateHandler
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.viewmodel.EventViewModel
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.myapplication.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(navController: NavController, viewModel: EventViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val registrationStatus by viewModel.registrationStatus.collectAsState()
    val isRegistering by viewModel.isRegistering.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchEvents()
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    AppBackgroundContainer {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                LargeTopAppBar(
                    title = { 
                        Column {
                            Text(
                                "FESTIVALS & GATHERINGS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KarnatakaRed,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Experience Karnataka in motion",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TempleGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
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
            Box(modifier = Modifier.fillMaxSize()) {
                // Festival Mood Layer (Subtle floating glow)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.04f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(HeritageGold, Color.Transparent),
                                center = Offset(200f, 400f),
                                radius = 600f
                            )
                        )
                )

                Box(modifier = Modifier.padding(padding)) {
                    UiStateHandler(
                        uiState = uiState,
                        onRetry = { viewModel.fetchEvents() },
                        loadingContent = {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(5) { 
                                    com.example.myapplication.ui.components.CulturalShimmer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp)
                                            .clip(RoundedCornerShape(36.dp))
                                    )
                                }
                            }
                        },
                        emptyContent = {
                            DefaultEmptyState(
                                icon = "🎭",
                                title = "No events yet",
                                description = "Check back soon for cultural festivals!"
                            )
                        }
                    ) { events ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                                FadeInItem(delayMillis = index * 100) {
                                    EventCard(
                                        title = event.title,
                                        date = event.date,
                                        location = event.location,
                                        artType = event.artType,
                                        imageUrl = event.imageUrl,
                                        isRegistered = registrationStatus[event.title] ?: false,
                                        isRegistering = isRegistering,
                                        onRegister = {
                                            viewModel.register(event)
                                        },
                                        onViewOnMap = {
                                            navController.navigate(NavRoutes.map(event.lat, event.lng))
                                        },
                                        onClick = {
                                            NavRoutes.navigateToEventDetail(navController, event)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
