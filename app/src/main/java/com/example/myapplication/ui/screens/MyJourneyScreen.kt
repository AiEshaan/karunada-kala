package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Post
import com.example.myapplication.core.utils.TimeUtils
import com.example.myapplication.ui.components.EventCardShimmer
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.JourneyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyJourneyScreen(
    navController: NavController,
    viewModel: JourneyViewModel = viewModel()
) {
    val registrations by viewModel.registrations.collectAsState()
    val enrollments by viewModel.enrollments.collectAsState()
    val myChronicles by viewModel.myChronicles.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchJourney()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "MY JOURNEY", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    ) 
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            } else if (registrations.isEmpty() && enrollments.isEmpty() && myChronicles.isEmpty()) {
                EmptyJourneyState(onExploreClick = { navController.navigate(NavRoutes.Explore.route) })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 🧍 Profile Header
                    item {
                        ProfileHeader(name = userName, avatarUrl = userAvatar)
                    }

                    // Stats Card
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            StatsCard(registrations.size, enrollments.size, myChronicles.size)
                        }
                    }

                    // 📜 My Chronicles Section
                    if (myChronicles.isNotEmpty()) {
                        item {
                            SectionHeader("📜 My Chronicles", Modifier.padding(horizontal = 16.dp))
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                myChronicles.forEach { post ->
                                    MyPostCard(post)
                                }
                            }
                        }
                    }

                    // 🎭 Registered Events Section
                    if (registrations.isNotEmpty()) {
                        item {
                            SectionHeader("🏛 Cultural Engagements", Modifier.padding(horizontal = 16.dp))
                        }
                        itemsIndexed(registrations) { index, reg ->
                            StaggeredJourneyItem(index) {
                                TimelineJourneyItem(
                                    title = reg.eventTitle,
                                    subtitle = reg.artType,
                                    timestamp = reg.timestamp,
                                    status = reg.status,
                                    accentColor = Color(0xFFD4AF37),
                                    isLast = index == registrations.size - 1 && enrollments.isEmpty()
                                )
                            }
                        }
                    }

                    // 🎨 Enrolled Workshops Section
                    if (enrollments.isNotEmpty()) {
                        itemsIndexed(enrollments) { index, enrollment ->
                            StaggeredJourneyItem(index + registrations.size) {
                                TimelineJourneyItem(
                                    title = enrollment.workshopTitle,
                                    subtitle = "Confirmed Enrollment",
                                    timestamp = enrollment.timestamp,
                                    status = "Enrolled",
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    isLast = index == enrollments.size - 1
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
fun ProfileHeader(name: String, avatarUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = if (avatarUrl.isNotEmpty()) avatarUrl else "https://ui-avatars.com/api/?name=$name&background=D4AF37&color=fff",
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Surface(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("CULTURAL EXPLORER", style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 2.sp)
    }
}

@Composable
fun MyPostCard(post: Post) {
    Card(
        modifier = Modifier.size(160.dp, 200.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            AsyncImage(model = post.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f)))))
            Text(
                post.caption,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 2,
                fontWeight = FontWeight.Bold
            )
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
fun StatsCard(eventCount: Int, workshopCount: Int, chronicleCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            )
        )) {
            Row(
                modifier = Modifier
                    .padding(vertical = 32.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(count = eventCount, label = "Events", icon = "🏛")
                StatItem(count = workshopCount, label = "Workshops", icon = "🎨")
                StatItem(count = chronicleCount, label = "Posts", icon = "📜")
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text(text = count.toString(), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun TimelineJourneyItem(title: String, subtitle: String, timestamp: com.google.firebase.Timestamp?, status: String, accentColor: Color, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(modifier = Modifier.width(32.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            Canvas(modifier = Modifier.fillMaxHeight()) {
                if (!isLast) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 30.dp.toPx()),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }
            Surface(
                modifier = Modifier.padding(top = 12.dp).size(16.dp),
                color = accentColor,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.background)
            ) { }
        }

        Spacer(modifier = Modifier.width(8.dp))
        JourneyItemCard(title, subtitle, timestamp, status, accentColor)
    }
}

@Composable
fun JourneyItemCard(title: String, subtitle: String, timestamp: com.google.firebase.Timestamp?, status: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = accentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (status == "Enrolled") "🎨" else "🏛", fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (status == "Enrolled") Color(0xFF2E7D32).copy(alpha = 0.1f) else accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (status == "Enrolled") Color(0xFF2E7D32) else accentColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TimeUtils.relativeTime(timestamp?.toDate()),
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
            }
        }
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
        Text("🏺", fontSize = 80.sp)
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
