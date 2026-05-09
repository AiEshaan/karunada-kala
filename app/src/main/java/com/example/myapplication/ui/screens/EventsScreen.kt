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
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.viewmodel.EventViewModel
import com.example.myapplication.ui.components.AppBackgroundContainer
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

    AppBackgroundContainer(
        textureAlpha = 0.04f,
        overlayBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF8F3EA).copy(alpha = 0.5f)) // Phase 4.4: Warmer festival feel
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                LargeTopAppBar(
                    title = { 
                        Text(
                            "EVENTS",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        ) 
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { padding ->

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
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(24.dp))
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
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                            com.example.myapplication.ui.components.StaggeredItem(index = index) {
                                EventCard(
                                    title = event.title,
                                    date = event.date,
                                    location = event.location,
                                    artType = event.artType,
                                    isRegistered = registrationStatus[event.title] ?: false,
                                    isRegistering = isRegistering,
                                    onRegister = {
                                        viewModel.register(event)
                                    },
                                    onViewOnMap = {
                                        navController.navigate(NavRoutes.map(event.lat, event.lng))
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
