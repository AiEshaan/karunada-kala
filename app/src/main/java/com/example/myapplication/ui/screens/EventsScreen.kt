package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.myapplication.ui.components.ReceiptDialog
import com.example.myapplication.ui.components.UiStateHandler
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.viewmodel.EventViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(navController: NavController, viewModel: EventViewModel = viewModel()) {

    val uiState by viewModel.uiState.collectAsState()
    val registrationStatus by viewModel.registrationStatus.collectAsState()
    val isRegistering by viewModel.isRegistering.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showReceiptFor by remember { mutableStateOf<Event?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchEvents()
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GrainOverlay()

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                LargeTopAppBar(
                    title = { 
                        Column {
                            Text(
                                "EVENTS",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "${filteredEvents.size} cultural gatherings discovered",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { padding ->

            PullToRefreshBox(
                isRefreshing = uiState is UiState.Loading,
                onRefresh = { viewModel.fetchEvents() },
                modifier = Modifier.padding(padding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("All", "This Week", "This Month", "Dance", "Craft", "Music")
                        filters.forEach { filter ->
                            KalaFilterChip(
                                selected = selectedFilter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = filter
                            )
                        }
                    }

                    UiStateHandler(
                        uiState = uiState,
                        onRetry = { viewModel.fetchEvents() },
                        loadingContent = {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(5) { EventCardShimmer() }
                            }
                        },
                        emptyContent = {
                            DefaultEmptyState(
                                icon = "🎭",
                                title = "No events yet",
                                description = "Check back soon for cultural festivals!"
                            )
                        }
                    ) { _ ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            itemsIndexed(filteredEvents, key = { _, event -> event.id }) { index, event ->
                                val shouldAnimate = index < 10
                                var visible by remember { mutableStateOf(!shouldAnimate) }

                                LaunchedEffect(event.id) {
                                    if (shouldAnimate) {
                                        delay(index * 80L)
                                        visible = true
                                    }
                                }

                                AnimatedVisibility(
                                    visible = visible,
                                    enter = slideInVertically { it / 2 } + fadeIn()
                                ) {
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
                                            showReceiptFor = event
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
        if (showReceiptFor != null) {
            ReceiptDialog(
                title = showReceiptFor!!.title,
                date = showReceiptFor!!.date,
                onDismiss = { showReceiptFor = null }
            )
        }
    }
}
