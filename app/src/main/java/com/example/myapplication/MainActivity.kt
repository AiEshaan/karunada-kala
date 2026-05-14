package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.components.AppBackground
import com.example.myapplication.ui.theme.HeritageCream
import com.example.myapplication.ui.theme.KarnatakaRed
import com.example.myapplication.ui.theme.TempleGreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.ArtViewModel
import com.example.myapplication.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

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
    val isDataSeeded = remember { prefs.getBoolean("data_seeded", false) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Shared ViewModels
    val artViewModel: ArtViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    // 🚀 AUTOMATIC ONE-TIME DATA SEEDING
    LaunchedEffect(isDataSeeded) {
        if (!isDataSeeded) {
            com.example.myapplication.data.SeedData.seedAll()
            prefs.edit { putBoolean("data_seeded", true) }
        }
    }

    val showBottomBar = currentDestination?.route != NavRoutes.Onboarding.route

    LaunchedEffect(currentDestination) {
        val route = currentDestination?.route ?: ""
        val contextStr = when {
            route.contains("explore") -> "Exploring the main heritage gallery."
            route.contains("map") -> "Viewing the cultural network map of Karnataka."
            route.contains("events") -> "Looking at upcoming cultural festivals."
            route.contains("community") -> "Browsing community chronicles and stories."
            route.contains("journey") -> "Reviewing personal cultural progress and journey."
            route.contains("detail") -> "Viewing deep details of a specific art form."
            else -> "General browsing of Karunada Kala app."
        }
        chatViewModel.setContext(contextStr)
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
        Box(modifier = Modifier.fillMaxSize()) {
            AppBackground()
            @OptIn(ExperimentalSharedTransitionApi::class)
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = if (isOnboardingDone) NavRoutes.Explore.route else NavRoutes.Onboarding.route,
                    modifier = Modifier.padding(if (showBottomBar) paddingValues else PaddingValues(0.dp)),
                    enterTransition = { 
                        fadeIn(spring(dampingRatio = 0.8f, stiffness = 400f)) + 
                        slideInVertically(initialOffsetY = { 30 }, animationSpec = spring(dampingRatio = 0.8f)) 
                    },
                    exitTransition = { 
                        fadeOut(spring(dampingRatio = 0.8f, stiffness = 400f)) + 
                        slideOutVertically(targetOffsetY = { -30 }, animationSpec = spring(dampingRatio = 0.8f))
                    },
                    popEnterTransition = { 
                        fadeIn(spring(dampingRatio = 0.8f, stiffness = 400f)) + 
                        slideInVertically(initialOffsetY = { -30 }, animationSpec = spring(dampingRatio = 0.8f))
                    },
                    popExitTransition = { 
                        fadeOut(spring(dampingRatio = 0.8f, stiffness = 400f)) + 
                        slideOutVertically(targetOffsetY = { 30 }, animationSpec = spring(dampingRatio = 0.8f))
                    }
                ) {
                    composable(NavRoutes.Onboarding.route) {
                        OnboardingScreen(onFinish = {
                            prefs.edit { putBoolean("onboarding_done", true) }
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
                            navController = navController,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }
                    composable(NavRoutes.EventDetail.route) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val title = backStackEntry.arguments?.getString("title") ?: ""
                        val description = backStackEntry.arguments?.getString("description") ?: ""
                        val date = backStackEntry.arguments?.getString("date") ?: ""
                        val location = backStackEntry.arguments?.getString("location") ?: ""
                        val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                        val artType = backStackEntry.arguments?.getString("artType") ?: ""
                        val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                        val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0

                        EventDetailScreen(
                            id = id,
                            title = title,
                            description = description,
                            date = date,
                            location = location,
                            imageUrl = imageUrl,
                            artType = artType,
                            lat = lat,
                            lng = lng,
                            navController = navController
                        )
                    }
                    composable(NavRoutes.Notifications.route) {
                        NotificationScreen(navController = navController)
                    }
                    composable(NavRoutes.Login.route) {
                        LoginScreen(
                            navController = navController,
                            onLoginSuccess = { navController.navigate(NavRoutes.Explore.route) { popUpTo(0) } },
                            onNavigateToSignUp = { navController.navigate(NavRoutes.SignUp.route) }
                        )
                    }
                    composable(NavRoutes.SignUp.route) {
                        SignUpScreen(
                            navController = navController,
                            onSignUpSuccess = { navController.navigate(NavRoutes.Explore.route) { popUpTo(0) } },
                            onNavigateToLogin = { navController.navigate(NavRoutes.Login.route) }
                        )
                    }
                    composable(
                        route = NavRoutes.WorkshopRegistration.route,
                        arguments = listOf(
                            navArgument("workshopId") { type = NavType.StringType },
                            navArgument("title") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val workshopId = backStackEntry.arguments?.getString("workshopId") ?: ""
                        val title = backStackEntry.arguments?.getString("title") ?: ""
                        WorkshopRegistrationScreen(navController, workshopId, title)
                    }
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .navigationBarsPadding()
    ) {
        // Organic Fluid Dock Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            color = HeritageCream.copy(alpha = 0.95f),
            shape = RoundedCornerShape(36.dp),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.2f))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val totalWidth = maxWidth
                val itemWidth = totalWidth / navItems.size
                
                // Sliding Fluid Pill
                val animatedOffset by animateDpAsState(
                    targetValue = itemWidth * selectedIndex,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                    label = "pillOffset"
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = animatedOffset.roundToPx(), y = 0) }
                        .width(itemWidth)
                        .fillMaxHeight()
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(KarnatakaRed, Color(0xFF8B0E1E))
                                ),
                                RoundedCornerShape(30.dp)
                            )
                    )
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    navItems.forEachIndexed { index, (route, icon, label) ->
                        val isSelected = index == selectedIndex
                        
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.1f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                            label = "iconScale"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate(route.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) Color.White else TempleGreen.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
