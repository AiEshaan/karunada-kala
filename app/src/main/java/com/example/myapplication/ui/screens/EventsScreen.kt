package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Event
import com.example.myapplication.ui.components.EventCard
import com.example.myapplication.ui.components.EventCardShimmer
import com.example.myapplication.viewmodel.EventViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(viewModel: EventViewModel = viewModel()) {

    val eventList by viewModel.events.collectAsState()
    val registrationStatus by viewModel.registrationStatus.collectAsState()
    val isRegistering by viewModel.isRegistering.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Cultural Events",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        AnimatedContent(
            targetState = isLoading,
            label = "eventShimmerTransition",
            modifier = Modifier.padding(padding),
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            }
        ) { loading ->
            if (loading) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(5) {
                        EventCardShimmer()
                    }
                }
            } else if (error != null && eventList.isEmpty()) {
                ErrorState(onRetry = { viewModel.fetchEvents() })
            } else if (eventList.isEmpty()) {
                EmptyState(
                    icon = "🎭",
                    title = "No events yet",
                    description = "Check back soon for cultural festivals!"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(eventList, key = { _, event -> event.id }) { index, event ->
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
                                isRegistered = registrationStatus[event.title] ?: false,
                                isRegistering = isRegistering,
                                onRegister = {
                                    viewModel.register(event)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: String, title: String, description: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(icon, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("⚠️", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Failed to load content. Please check your internet connection.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}
