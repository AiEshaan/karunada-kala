package com.example.myapplication

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.viewmodel.AuthViewModel
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
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
    val authViewModel: AuthViewModel = viewModel()
    val user by authViewModel.currentUser.collectAsState()

    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val isOnboardingDone = remember { prefs.getBoolean("onboarding_done", false) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Shared ViewModels
    val artViewModel: ArtViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    val showBottomBar = currentDestination?.route != NavRoutes.Onboarding.route &&
            currentDestination?.route != NavRoutes.Login.route &&
            currentDestination?.route != NavRoutes.SignUp.route

    val startDestination = when {
        !isOnboardingDone -> NavRoutes.Onboarding.route
        user == null -> NavRoutes.Login.route
        else -> NavRoutes.Explore.route
    }

    LaunchedEffect(currentDestination) {
        val route = currentDestination?.route ?: ""
        val chatContext = when {
            route.contains("explore") -> "Exploring the main heritage gallery."
            route.contains("map") -> "Viewing the cultural network map of Karnataka."
            route.contains("events") -> "Looking at upcoming cultural festivals."
            route.contains("community") -> "Browsing community chronicles and stories."
            route.contains("journey") -> "Reviewing personal cultural progress and journey."
            route.contains("detail") -> "Viewing deep details of a specific art form."
            else -> "General browsing of Karunada Kala app."
        }
        chatViewModel.setContext(chatContext)
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    tonalElevation = 0.dp // Flat editorial look
                ) {
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Explore.route } == true,
                        onClick = { navController.navigate(NavRoutes.Explore.route) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Explore") },
                        label = { Text("Explore", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Map.route } == true,
                        onClick = { navController.navigate(NavRoutes.Map.route) },
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
                        label = { Text("Map", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Events.route } == true,
                        onClick = { navController.navigate(NavRoutes.Events.route) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Events") },
                        label = { Text("Events", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Community.route } == true,
                        onClick = { navController.navigate(NavRoutes.Community.route) },
                        icon = { Icon(Icons.Default.Face, contentDescription = "Chronicles") },
                        label = { Text("Chronicles", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Journey.route } == true,
                        onClick = { navController.navigate(NavRoutes.Journey.route) },
                        icon = { Icon(Icons.Default.AccountBox, contentDescription = "Journey") },
                        label = { Text("Journey", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(if (showBottomBar) paddingValues else androidx.compose.foundation.layout.PaddingValues(0.dp))
        ) {
            composable(NavRoutes.Onboarding.route) {
                OnboardingScreen(onFinish = {
                    prefs.edit().putBoolean("onboarding_done", true).apply()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.Explore.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(NavRoutes.SignUp.route)
                    }
                )
            }
            composable(NavRoutes.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(NavRoutes.Explore.route) {
                            popUpTo(NavRoutes.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(NavRoutes.Login.route)
                    }
                )
            }
            composable(NavRoutes.Explore.route) {
                ExploreScreen(navController, viewModel = artViewModel, chatViewModel = chatViewModel)
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

                DetailScreen(name, description, imageUrl, artistId, category, navController, viewModel = artViewModel)
            }
            composable(NavRoutes.ArtistDetail.route) { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                ArtistDetailScreen(artistId)
            }
        }
    }
}
