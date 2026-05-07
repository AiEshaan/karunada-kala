package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.components.ReceiptDialog
import com.example.myapplication.ui.components.UiStateHandler
import com.example.myapplication.ui.components.WorkshopCard
import com.example.myapplication.ui.components.WorkshopCardShimmer
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.viewmodel.WorkshopViewModel
import com.example.myapplication.ui.components.AppBackgroundContainer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopsScreen(navController: NavController, viewModel: WorkshopViewModel = viewModel()) {

    val uiState by viewModel.uiState.collectAsState()
    val enrollmentStatus by viewModel.enrollmentStatus.collectAsState()
    val isEnrolling by viewModel.isEnrolling.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()
    val lastEnrolled by viewModel.lastEnrolledWorkshop.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchWorkshops()
    }

    LaunchedEffect(uiEvent) {
        uiEvent?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiEvent()
        }
    }

    AppBackgroundContainer(textureAlpha = 0.04f) {
        Scaffold(
            containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Learning Sessions", 
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

        Box(modifier = Modifier.padding(padding)) {
            UiStateHandler(
                uiState = uiState,
                onRetry = { viewModel.fetchWorkshops() },
                loadingContent = {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(5) { WorkshopCardShimmer() }
                    }
                },
                emptyContent = {
                    com.example.myapplication.ui.components.DefaultEmptyState(
                        icon = "🎨",
                        title = "No workshops available",
                        description = "Check back soon for new learning sessions!"
                    )
                }
            ) { workshops ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(workshops, key = { _, workshop -> workshop.id }) { index, workshop ->
                        val shouldAnimate = index < 10
                        var visible by remember { mutableStateOf(!shouldAnimate) }
                        
                        LaunchedEffect(workshop.id) {
                            if (shouldAnimate) {
                                delay(index * 80L)
                                visible = true
                            }
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInVertically { it / 2 } + fadeIn()
                        ) {
                            WorkshopCard(
                                workshop = workshop,
                                isEnrolled = enrollmentStatus[workshop.id] ?: false,
                                isEnrolling = isEnrolling,
                                onEnroll = { 
                                    viewModel.enroll(workshop)
                                }
                            )
                        }
                    }
                }
            }
        }

        lastEnrolled?.let { workshop ->
            ReceiptDialog(
                title = workshop.title,
                date = workshop.date,
                onDismiss = { viewModel.clearEnrollmentState() }
            )
        }
        }
    }
}
