package com.example.myapplication

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.example.myapplication.ui.components.AppBackgroundContainer
import com.example.myapplication.ui.components.KalaBackground
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.ArtViewModel
import com.example.myapplication.viewmodel.ChatViewModel
import android.util.Log
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
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

    val showBottomBar = (currentDestination?.route != NavRoutes.Onboarding.route) &&
            (currentDestination?.route != NavRoutes.Login.route) &&
            (currentDestination?.route != NavRoutes.SignUp.route)

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
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    tonalElevation = 0.dp,
                ) {
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Explore.route } == true,
                        onClick = { navController.navigate(NavRoutes.Explore.route) },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.explore)) },
                        label = { Text(stringResource(R.string.explore), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
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
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.map)) },
                        label = { Text(stringResource(R.string.map), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
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
                        icon = { Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.events)) },
                        label = { Text(stringResource(R.string.events), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
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
                        icon = { Icon(Icons.Default.Face, contentDescription = stringResource(R.string.chronicles)) },
                        label = { Text(stringResource(R.string.chronicles), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
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
                        icon = { Icon(Icons.Default.AccountBox, contentDescription = stringResource(R.string.my_journey)) },
                        label = { Text(stringResource(R.string.my_journey), fontWeight = FontWeight.Bold, fontSize = 10.sp) },
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
        val routeStr = currentDestination?.route ?: ""
        val textureAlpha = when {
            routeStr.contains(NavRoutes.Explore.route) -> 0.05f
            routeStr.contains(NavRoutes.Community.route) -> 0.045f
            routeStr.contains(NavRoutes.Detail.route) -> 0.035f
            routeStr.contains(NavRoutes.Journey.route) -> 0.035f
            routeStr.contains(NavRoutes.Map.route) || routeStr.contains(NavRoutes.Onboarding.route) -> 0.01f
            else -> 0.04f
        }

        AppBackgroundContainer(textureAlpha = textureAlpha) {
            KalaBackground {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(if (showBottomBar) paddingValues else androidx.compose.foundation.layout.PaddingValues(0.dp)),
                    enterTransition = { fadeIn(tween(400)) + slideInHorizontally { it / 2 } },
                    exitTransition = { fadeOut(tween(400)) + slideOutHorizontally { -it / 2 } },
                    popEnterTransition = { fadeIn(tween(400)) + slideInHorizontally { -it / 2 } },
                    popExitTransition = { fadeOut(tween(400)) + slideOutHorizontally { it / 2 } }
                ) {
                composable(NavRoutes.Onboarding.route) {
                    OnboardingScreen(onFinish = {
                        prefs.edit(commit = false) { putBoolean("onboarding_done", true) }
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
                    
                    EventDetailScreen(id, title, description, date, location, imageUrl, artType, lat, lng, navController)
                }
                composable(NavRoutes.Notifications.route) {
                    NotificationScreen(navController)
                }
            }
        }
    }
}
}
