package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.JourneyViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Post
import com.example.myapplication.data.model.Registration
import com.example.myapplication.data.model.Enrollment
import com.example.myapplication.core.utils.TimeUtils
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.components.AppBackgroundContainer
import com.example.myapplication.ui.components.EventCardShimmer
import com.example.myapplication.ui.components.PatronCertificateDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyJourneyScreen(
    navController: NavController,
    journeyViewModel: JourneyViewModel = viewModel()
) {
    val registrations by journeyViewModel.registrations.collectAsState()
    val myEnrollments by journeyViewModel.enrollments.collectAsState()
    val myChronicles by journeyViewModel.myChronicles.collectAsState()
    val userName by journeyViewModel.userName.collectAsState()
    val userAvatar by journeyViewModel.userAvatar.collectAsState()
    val badges by journeyViewModel.badges.collectAsState()
    val isLoading by journeyViewModel.isLoading.collectAsState()

    var showCertificateFor by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        journeyViewModel.fetchJourney()
    }

    AppBackgroundContainer(
        textureAlpha = 0.01f, // Phase 4.6: Cleaner background
        showMotion = false, // Dashboard feel
        overlayBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFdfdfd).copy(alpha = 0.5f))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = { 
                        Column {
                            Text(
                                "MY JOURNEY", 
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Black,
                                color = KarnatakaRed
                            )
                            Text(
                                "Cultural Milestone",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TempleGreen,
                                fontWeight = FontWeight.Bold
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
            AnimatedContent<Boolean>(
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
                } else if (registrations.isEmpty() && myEnrollments.isEmpty() && myChronicles.isEmpty()) {
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
                                StatsCard(registrations.count(), myEnrollments.count(), myChronicles.count())
                            }
                        }

                        // 🏆 Badges Gallery
                        if (badges.isNotEmpty()) {
                            item {
                                SectionHeader("🏆 Cultural Milestones", Modifier.padding(horizontal = 16.dp))
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    badges.forEachIndexed { index, badge ->
                                        BadgeItem(badge, index) {
                                            if (badge.isUnlocked) {
                                                showCertificateFor = badge.name
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 📜 My Chronicles Section
                        val chroniclesList: List<Post> = myChronicles
                        if (chroniclesList.isNotEmpty()) {
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
                                    for (post in chroniclesList) {
                                        MyPostCard(post)
                                    }
                                }
                            }
                        }

                        // 🎭 Registered Events Section
                        val regList: List<Registration> = registrations
                        if (regList.isNotEmpty()) {
                            item {
                                SectionHeader("🏛 Cultural Engagements", Modifier.padding(horizontal = 16.dp))
                            }
                            itemsIndexed(regList) { index, reg ->
                                StaggeredJourneyItem(index) {
                                    TimelineJourneyItem(
                                        title = reg.eventTitle,
                                        subtitle = reg.artType,
                                        timestamp = reg.timestamp,
                                        status = reg.status,
                                        accentColor = Color(0xFFD4AF37),
                                        isLast = index == regList.count() - 1 && myEnrollments.isEmpty()
                                    )
                                }
                            }
                        }

                        // 🎨 Enrolled Workshops Section
                        val enrList: List<Enrollment> = myEnrollments
                        if (enrList.isNotEmpty()) {
                            itemsIndexed(enrList) { index, enr ->
                                val enrTimestamp = enr.timestamp
                                StaggeredJourneyItem(index + regList.count()) {
                                    TimelineJourneyItem(
                                        title = enr.workshopTitle,
                                        subtitle = "Confirmed Enrollment",
                                        timestamp = enrTimestamp,
                                        status = "Enrolled",
                                        accentColor = MaterialTheme.colorScheme.primary,
                                        isLast = index == enrList.count() - 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showCertificateFor?.let { badgeName ->
        PatronCertificateDialog(
            userName = userName,
            badgeName = badgeName,
            onDismiss = { showCertificateFor = null }
        )
    }
}

@Composable
fun ProfileHeader(name: String, avatarUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Cultural Aura Ring
            val infinite = rememberInfiniteTransition(label = "aura")
            val auraRotation by infinite.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
                label = "rotation"
            )

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .graphicsLayer { rotationZ = auraRotation }
                    .background(
                        Brush.sweepGradient(
                            listOf(KarnatakaRed, HeritageGold, TempleGreen, KarnatakaRed)
                        ),
                        CircleShape
                    )
                    .alpha(0.3f)
            )

            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .padding(4.dp),
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                AsyncImage(
                    model = if (avatarUrl.isNotEmpty()) avatarUrl else "https://ui-avatars.com/api/?name=$name&background=D4AF37&color=fff",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Achievement Glow
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .offset(x = (-8).dp, y = (-8).dp),
                color = HeritageGold,
                shape = CircleShape,
                shadowElevation = 12.dp,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        Text(
            name.uppercase(), 
            style = MaterialTheme.typography.displaySmall, 
            fontWeight = FontWeight.Black,
            color = KarnatakaRed,
            letterSpacing = 1.sp
        )
        Text(
            "CULTURAL CUSTODIAN", 
            style = MaterialTheme.typography.labelSmall, 
            color = TempleGreen.copy(alpha = 0.7f), 
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )
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
        enter = slideInHorizontally { -50 } + fadeIn(animationSpec = spring(dampingRatio = 0.8f)),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        content()
    }
}

@Composable
fun StatsCard(eventCount: Int, workshopCount: Int, chronicleCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(count = eventCount, label = "Events", icon = "🎭")
            StatItem(count = workshopCount, label = "Workshops", icon = "🎓")
            StatItem(count = chronicleCount, label = "Chronicles", icon = "📜")
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = KarnatakaRed.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        com.example.myapplication.ui.components.CountUpText(
            targetValue = count,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = KarnatakaRed)
        )
        Text(
            text = label.uppercase(), 
            style = MaterialTheme.typography.labelSmall, 
            color = TempleGreen.copy(alpha = 0.6f), 
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
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
            com.example.myapplication.ui.components.PulseAnimation(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(16.dp),
                    color = accentColor,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.background)
                ) { }
            }
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

@Composable
fun BadgeItem(badge: com.example.myapplication.data.model.Badge, index: Int, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var hasBursted by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (badge.isUnlocked) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "badgeScale"
    )

    LaunchedEffect(badge.isUnlocked) {
        if (badge.isUnlocked && !hasBursted) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasBursted = true
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(enabled = badge.isUnlocked, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (badge.isUnlocked) {
                com.example.myapplication.ui.components.PulseAnimation {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape,
                        border = BorderStroke(2.dp, Color(0xFFD4AF37))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(badge.icon, fontSize = 32.sp)
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.size(64.dp),
                    color = Color.LightGray.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🔒", fontSize = 24.sp, modifier = Modifier.alpha(0.5f))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            badge.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
