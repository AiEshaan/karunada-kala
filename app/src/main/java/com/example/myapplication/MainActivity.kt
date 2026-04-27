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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.ArtViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth
        FirebaseAuth.getInstance().signInAnonymously()

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

    // Shared ArtViewModel to track viewed arts across Explore and Detail
    val artViewModel: ArtViewModel = viewModel()

    val showBottomBar = currentDestination?.route != NavRoutes.Onboarding.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Explore.route } == true,
                        onClick = { navController.navigate(NavRoutes.Explore.route) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Explore") },
                        label = { Text("Explore") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Map.route } == true,
                        onClick = { navController.navigate(NavRoutes.Map.route) },
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
                        label = { Text("Map") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Events.route } == true,
                        onClick = { navController.navigate(NavRoutes.Events.route) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Events") },
                        label = { Text("Events") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Workshops.route } == true,
                        onClick = { navController.navigate(NavRoutes.Workshops.route) },
                        icon = { Icon(Icons.Default.Build, contentDescription = "Workshops") },
                        label = { Text("Workshops") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.AiGuide.route } == true,
                        onClick = { navController.navigate(NavRoutes.AiGuide.route) },
                        icon = { Icon(Icons.Default.Info, contentDescription = "AI Guide") },
                        label = { Text("AI Guide") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Journey.route } == true,
                        onClick = { navController.navigate(NavRoutes.Journey.route) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Journey") },
                        label = { Text("Journey") }
                    )
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = if (isOnboardingDone) NavRoutes.Explore.route else NavRoutes.Onboarding.route,
            modifier = Modifier.padding(if (showBottomBar) paddingValues else androidx.compose.foundation.layout.PaddingValues(0.dp))
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
                ExploreScreen(navController, viewModel = artViewModel)
            }
            composable(NavRoutes.Map.route) {
                MapScreen(navController)
            }
            composable(NavRoutes.Events.route) {
                EventsScreen()
            }
            composable(NavRoutes.Workshops.route) {
                WorkshopsScreen()
            }
            composable(NavRoutes.Journey.route) {
                MyJourneyScreen(navController = navController)
            }
            composable(NavRoutes.AiGuide.route) {
                AiGuideScreen()
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
