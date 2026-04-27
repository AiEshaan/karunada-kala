package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.myapplication.ui.components.EventCardShimmer
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.JourneyViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyJourneyScreen(
    navController: NavController,
    viewModel: JourneyViewModel = viewModel()
) {
    val registrations by viewModel.registrations.collectAsState()
    val enrollments by viewModel.enrollments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchJourney()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { Text("My Journey", style = MaterialTheme.typography.headlineMedium) }
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = isLoading,
            label = "journeyTransition",
            modifier = Modifier.padding(padding),
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            }
        ) { loading ->
            if (loading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Box(Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))) }
                    items(3) { EventCardShimmer() }
                }
            } else if (registrations.isEmpty() && enrollments.isEmpty()) {
                EmptyJourneyState(onExploreClick = { navController.navigate(NavRoutes.Explore.route) })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stats Card
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            StatsCard(registrations.size, enrollments.size)
                        }
                    }

                    // Registered Events Section
                    if (registrations.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader("🎭 Registered Events", Modifier.padding(horizontal = 16.dp))
                        }
                        itemsIndexed(registrations) { index, reg ->
                            StaggeredJourneyItem(index) {
                                JourneyItemCard(
                                    title = reg["eventTitle"] as? String ?: "Event",
                                    subtitle = reg["artType"] as? String ?: "",
                                    timestamp = reg["timestamp"] as? Timestamp,
                                    status = reg["status"] as? String ?: "Interested",
                                    accentColor = Color(0xFFD4AF37)
                                )
                            }
                        }
                    }

                    // Enrolled Workshops Section
                    if (enrollments.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader("🛠 Enrolled Workshops", Modifier.padding(horizontal = 16.dp))
                        }
                        itemsIndexed(enrollments) { index, enrollment ->
                            StaggeredJourneyItem(index + registrations.size) {
                                JourneyItemCard(
                                    title = enrollment["workshopTitle"] as? String ?: "Workshop",
                                    subtitle = "Confirmed Enrollment",
                                    timestamp = enrollment["timestamp"] as? Timestamp,
                                    status = "Enrolled",
                                    accentColor = MaterialTheme.colorScheme.primary
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
fun StaggeredJourneyItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { 50 } + fadeIn(),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        content()
    }
}

@Composable
fun StatsCard(eventCount: Int, workshopCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(count = eventCount, label = "Events")
            Box(modifier = Modifier.height(40.dp).width(1.dp).background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)))
            StatItem(count = workshopCount, label = "Workshops")
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun JourneyItemCard(title: String, subtitle: String, timestamp: Timestamp?, status: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = relativeTime(timestamp), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

fun relativeTime(ts: Timestamp?): String {
    ts ?: return ""
    val diff = System.currentTimeMillis() - ts.toDate().time
    val mins = diff / 60000
    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "$mins min ago"
        mins < 1440 -> "${mins/60} hr ago"
        else -> "${mins/1440} d ago"
    }
}

@Composable
fun EmptyJourneyState(onExploreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎭", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Your cultural journey starts here",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Explore an art form and begin learning to see your journey unfold!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onExploreClick,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text("Explore Arts", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
