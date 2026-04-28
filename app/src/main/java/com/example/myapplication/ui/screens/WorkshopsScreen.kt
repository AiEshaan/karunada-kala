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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.WorkshopCard
import com.example.myapplication.ui.components.WorkshopCardShimmer
import com.example.myapplication.viewmodel.WorkshopViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopsScreen(viewModel: WorkshopViewModel = viewModel()) {

    val workshopList by viewModel.workshops.collectAsState()
    val enrollmentStatus by viewModel.enrollmentStatus.collectAsState()
    val isEnrolling by viewModel.isEnrolling.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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

        AnimatedContent(
            targetState = isLoading,
            label = "workshopShimmerTransition",
            modifier = Modifier.padding(padding),
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            }
        ) { loading ->
            if (loading) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(5) {
                        WorkshopCardShimmer()
                    }
                }
            } else if (workshopList.isEmpty()) {
                EmptyState(
                    icon = "🎨",
                    title = "No workshops available",
                    description = "Check back soon for new learning sessions!"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(workshopList, key = { _, workshop -> workshop.id }) { index, workshop ->
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
    }
}
