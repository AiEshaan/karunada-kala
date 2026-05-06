package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Post
import com.example.myapplication.ui.components.EventCardShimmer
import com.example.myapplication.ui.components.KalaGlassCard
import com.example.myapplication.ui.components.KalaElevation
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.JourneyViewModel
import com.google.firebase.Timestamp

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
    val badges by viewModel.badges.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchJourney()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MY JOURNEY", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 4.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (showSettings) {
            SettingsSheet(onDismiss = { showSettings = false }, navController = navController)
        }

        if (showEditProfile) {
            EditProfileDialog(
                currentName = userName,
                currentAvatar = userAvatar,
                onDismiss = { showEditProfile = false },
                onSave = { name, avatar ->
                    viewModel.updateProfile(name, avatar)
                    showEditProfile = false
                }
            )
        }
        
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchJourney() },
            modifier = Modifier.padding(padding)
        ) {
            AnimatedContent(
                targetState = isLoading,
                label = "journeyTransition",
                modifier = Modifier.fillMaxSize(),
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
                        item {
                            ProfileHeader(
                                name = userName, 
                                avatarUrl = userAvatar, 
                                level = calculateLevel(registrations.size, enrollments.size, myChronicles.size),
                                onEditClick = { showEditProfile = true }
                            )
                        }

                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                StatsCard(registrations.size, enrollments.size, myChronicles.size)
                            }
                        }

                        item {
                            XpProgressBar(registrations.size, enrollments.size, myChronicles.size)
                        }

                        item {
                            AchievementSection(badges)
                        }

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

                        item {
                            SectionHeader("🏺 Cultural Engagements", Modifier.padding(horizontal = 16.dp))
                        }

                        itemsIndexed(registrations) { index, reg ->
                            StaggeredJourneyItem(index) {
                                TimelineJourneyItem(
                                    title = reg.eventTitle,
                                    subtitle = reg.artType,
                                    timestamp = reg.timestamp,
                                    status = reg.status,
                                    accentColor = MaterialTheme.colorScheme.secondary,
                                    isLast = (index == registrations.size - 1) && enrollments.isEmpty()
                                )
                            }
                        }

                        itemsIndexed(enrollments) { index, enrollment ->
                            StaggeredJourneyItem(index + registrations.size) {
                                TimelineJourneyItem(
                                    title = enrollment.workshopTitle,
                                    subtitle = "Heritage Workshop",
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
fun AchievementSection(badges: List<com.example.myapplication.data.model.Badge>) {
    KalaGlassCard(
        modifier = Modifier.padding(16.dp),
        elevation = KalaElevation.Low
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("🏆 Achievements", Modifier.padding(bottom = 12.dp))
            
            val unlockedCount = badges.count { it.isUnlocked }
            val progress = if (badges.isNotEmpty()) unlockedCount.toFloat() / badges.size else 0f
            
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cultural Progress", style = MaterialTheme.typography.labelMedium)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                badges.forEach { badge ->
                    BadgeItem(badge)
                }
            }
        }
    }
}

@Composable
fun BadgeItem(badge: com.example.myapplication.data.model.Badge) {
    KalaGlassCard(
        modifier = Modifier.width(140.dp),
        elevation = KalaElevation.Low,
        alpha = if (badge.isUnlocked) 0.65f else 0.3f
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(badge.icon, fontSize = 24.sp, modifier = Modifier.graphicsLayer { alpha = if (badge.isUnlocked) 1f else 0.5f })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                badge.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Text(
                badge.description,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp,
                color = Color.Gray
            )
            
            if (!badge.isUnlocked && badge.totalRequired > 1) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { badge.currentProgress.toFloat() / badge.totalRequired },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = Color.Gray,
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
                Text(
                    "${badge.currentProgress}/${badge.totalRequired}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var demoMode by remember { mutableStateOf(false) }
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            
            Text("ACCOUNT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            SettingItem(Icons.Default.Person, "Profile Details", "Eshaan P.M")
            SettingItem(Icons.Default.Email, "Email", "eshaan@heritage.in")
            
            Spacer(Modifier.height(16.dp))
            Text("PREFERENCES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            SettingItem(Icons.Default.Notifications, "Notifications", "Enabled")
            SettingItem(Icons.Default.Translate, "Language", "English (Kannada Ready)")
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Build, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Demo Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("Preload presentation data", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Switch(checked = demoMode, onCheckedChange = { demoMode = it })
            }

            Spacer(Modifier.height(16.dp))
            Text("APP INFO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            SettingItem(Icons.Default.Info, "App Version", "1.1.0 (WOW Polish)")
            SettingItem(Icons.AutoMirrored.Filled.HelpCenter, "Cultural Help Center", null)
            
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    onDismiss()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("LOGOUT")
            }

            Spacer(Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun SettingItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
    }
}

@Composable
fun ProfileHeader(name: String, avatarUrl: String, level: String, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = avatarUrl.ifEmpty { "https://ui-avatars.com/api/?name=$name&background=D4AF37&color=fff" },
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(com.example.myapplication.R.drawable.placeholder),
                error = painterResource(com.example.myapplication.R.drawable.placeholder)
            )
            Surface(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                tonalElevation = 4.dp,
                onClick = onEditClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(level, style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 2.sp)
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentAvatar: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var avatar by remember { mutableStateOf(currentAvatar) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = avatar,
                    onValueChange = { avatar = it },
                    label = { Text("Avatar URL (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, avatar) }) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun XpProgressBar(registrations: Int, enrollments: Int, chronicles: Int) {
    val totalXp = registrations * 20 + enrollments * 50 + chronicles * 30
    val currentLevel = totalXp / 100
    val progressInLevel = (totalXp % 100) / 100f

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Level $currentLevel Heritage Keeper", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("${(progressInLevel * 100).toInt()}% to Level ${currentLevel + 1}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progressInLevel },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

fun calculateLevel(registrations: Int, enrollments: Int, chronicles: Int): String {
    val total = registrations + enrollments + chronicles
    return when {
        total > 15 -> "Vishwa Karma (World Architect)"
        total > 10 -> "Raja Guru (Royal Master)"
        total > 5 -> "Kala Acharya (Art Teacher)"
        else -> "Abhyasi (Seeker)"
    }
}

@Composable
fun MyPostCard(post: Post) {
    Card(
        modifier = Modifier.size(140.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(com.example.myapplication.R.drawable.placeholder),
                error = painterResource(com.example.myapplication.R.drawable.placeholder)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f))))
            )
            Text(
                post.caption,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
        }
    }
}

@Composable
fun StaggeredJourneyItem(index: Int, content: @Composable () -> Unit) {
    val animatable = remember { androidx.compose.animation.core.Animatable(50f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 100L)
        animatable.animateTo(0f, spring(stiffness = 300f, dampingRatio = 0.8f))
        alpha.animateTo(1f, tween(500))
    }
    Box(modifier = Modifier.offset { IntOffset(0, animatable.value.dp.roundToPx()) }.graphicsLayer { this.alpha = alpha.value }) { content() }
}

@Composable
fun StatsCard(registrations: Int, enrollments: Int, chronicles: Int) {
    KalaGlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = KalaElevation.Medium,
        alpha = 0.9f
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F3D2E), Color(0xFF1F6F5B))
                    )
                )
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(count = registrations, label = "Events", icon = "🏛", isDark = true)
            StatItem(count = enrollments, label = "Workshops", icon = "🏺", isDark = true)
            StatItem(count = chronicles, label = "Stories", icon = "📜", isDark = true)
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, icon: String, isDark: Boolean = false) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 24.sp)
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall, color = textColor, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = subTextColor)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
fun TimelineJourneyItem(
    title: String,
    subtitle: String,
    timestamp: Timestamp?,
    status: String,
    accentColor: Color,
    isLast: Boolean
) {
    Row(modifier = Modifier.padding(horizontal = 24.dp).height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = accentColor,
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.background)
            ) {}
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(accentColor.copy(alpha = 0.2f), shape = RoundedCornerShape(1.dp))
                )
            }
        }
        
        Spacer(Modifier.width(24.dp))
        
        JourneyItemCard(title, subtitle, timestamp, status, accentColor)
    }
}

@Composable
fun JourneyItemCard(
    title: String,
    subtitle: String,
    timestamp: Timestamp?,
    status: String,
    accentColor: Color
) {
    val locale = LocalConfiguration.current.locales[0]
    KalaGlassCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        elevation = KalaElevation.Low
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (timestamp != null) {
                    val date = java.text.SimpleDateFormat("dd MMM yyyy", locale).format(timestamp.toDate())
                    Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.6f))
                }
            }
            Surface(
                color = accentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyJourneyState(onExploreClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏛", fontSize = 64.sp)
        Spacer(Modifier.height(24.dp))
        Text("Your journey hasn't begun...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Every legend starts with a single step. Discover your first art form or event.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onExploreClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("BEGIN EXPLORING")
        }
    }
}
