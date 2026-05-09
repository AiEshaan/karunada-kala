package com.example.myapplication

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.ArtViewModel
import com.example.myapplication.viewmodel.ChatViewModel
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.example.myapplication.ui.components.bouncyClickable
import androidx.compose.animation.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth
        FirebaseAuth.getInstance().signInAnonymously()

        // Initialize Firebase Messaging
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("KarunadaKala", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("KarunadaKala", "FCM Token: $token")
        }

        setContent {
            MyApplicationTheme {
                KarunadaKalaApp()
            }
        }
    }
}

@Composable
fun KarunadaKalaApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val isOnboardingDone = remember { prefs.getBoolean("onboarding_done", false) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Shared ViewModels
    val artViewModel: ArtViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    val showBottomBar = currentDestination?.route != NavRoutes.Onboarding.route

    LaunchedEffect(currentDestination) {
        val route = currentDestination?.route ?: ""
        val context = when {
            route.contains("explore") -> "Exploring the main heritage gallery."
            route.contains("map") -> "Viewing the cultural network map of Karnataka."
            route.contains("events") -> "Looking at upcoming cultural festivals."
            route.contains("community") -> "Browsing community chronicles and stories."
            route.contains("journey") -> "Reviewing personal cultural progress and journey."
            route.contains("detail") -> "Viewing deep details of a specific art form."
            else -> "General browsing of Karunada Kala app."
        }
        chatViewModel.setContext(context)
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AnimatedBottomBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { paddingValues ->
        @OptIn(ExperimentalSharedTransitionApi::class)
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = if (isOnboardingDone) NavRoutes.Explore.route else NavRoutes.Onboarding.route,
                modifier = Modifier.padding(if (showBottomBar) paddingValues else androidx.compose.foundation.layout.PaddingValues(0.dp)),
                enterTransition = { fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { 300 }) },
                exitTransition = { fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { -300 }) },
                popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { -300 }) },
                popExitTransition = { fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { 300 }) }
            ) {
                composable(NavRoutes.Onboarding.route) {
                    OnboardingScreen(onFinish = {
                        prefs.edit().putBoolean("onboarding_done", true).apply()
                        navController.navigate(NavRoutes.Explore.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    })
                }
                composable(NavRoutes.Explore.route) {
                    ExploreScreen(
                        navController, 
                        viewModel = artViewModel, 
                        chatViewModel = chatViewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }
                composable(
                    route = NavRoutes.Map.route,
                    arguments = listOf(
                        navArgument("lat") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("lng") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStackEntry ->
                    val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                    val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
                    MapScreen(navController, initialLat = lat, initialLng = lng)
                }
                composable(NavRoutes.Events.route) {
                    EventsScreen(navController = navController)
                }
                composable(NavRoutes.Community.route) {
                    CommunityScreen()
                }
                composable(NavRoutes.Journey.route) {
                    MyJourneyScreen(navController = navController)
                }
                composable(NavRoutes.Detail.route) { backStackEntry ->
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    val description = backStackEntry.arguments?.getString("description") ?: ""
                    val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                    val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                    val category = backStackEntry.arguments?.getString("category") ?: "Art"

                    DetailScreen(
                        name, 
                        description, 
                        imageUrl, 
                        artistId, 
                        category, 
                        navController, 
                        viewModel = artViewModel,
                        chatViewModel = chatViewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }
                composable(NavRoutes.ArtistDetail.route) { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                    ArtistDetailScreen(
                        artistId = artistId,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedBottomBar(
    navController: androidx.navigation.NavHostController,
    currentDestination: androidx.navigation.NavDestination?
) {
    val navItems = listOf(
        Triple(NavRoutes.Explore, Icons.Default.Home, "Explore"),
        Triple(NavRoutes.Map, Icons.Default.LocationOn, "Map"),
        Triple(NavRoutes.Events, Icons.Default.DateRange, "Events"),
        Triple(NavRoutes.Community, Icons.Default.Face, "Chronicles"),
        Triple(NavRoutes.Journey, Icons.Default.AccountBox, "Journey")
    )

    val selectedIndex = navItems.indexOfFirst { (route, _, _) ->
        currentDestination?.hierarchy?.any { it.route == route.route } == true
    }.coerceAtLeast(0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalWidth = maxWidth
            val itemWidth = totalWidth / navItems.size
            
            // Sliding Pill
            val animatedOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                label = "pillOffset"
            )

            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2D5A2D), RoundedCornerShape(50))
                )
            }

            Row(modifier = Modifier.fillMaxSize()) {
                navItems.forEachIndexed { index, (route, icon, label) ->
                    val isSelected = index == selectedIndex
                    
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                        label = "iconScale"
                    )
                    
                    val alpha by animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.4f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                        label = "labelAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bouncyClickable(onClick = {
                                if (!isSelected) {
                                    navController.navigate(route.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.alpha(alpha),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
