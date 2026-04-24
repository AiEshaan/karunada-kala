package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.*
import com.example.myapplication.ui.navigation.NavRoutes
import com.example.myapplication.ui.screens.DetailScreen
import com.example.myapplication.ui.screens.EventsScreen
import com.example.myapplication.ui.screens.ExploreScreen
import com.example.myapplication.ui.screens.MapScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable Splash Screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                KarunadaKalaApp()
            }
        }
    }
}

@Composable
fun KarunadaKalaApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.Explore.route) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Explore") },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.Map.route) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Map") },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.Events.route) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Events") },
                    label = { Text("Events") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(NavRoutes.Workshops.route) },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Workshops") },
                    label = { Text("Workshops") }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = NavRoutes.Explore.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavRoutes.Explore.route) {
                ExploreScreen(navController)
            }
            composable(NavRoutes.Map.route) {
                MapScreen()
            }
            composable(NavRoutes.Events.route) {
                EventsScreen()
            }
            composable(NavRoutes.Workshops.route) {
                PlaceholderScreen("Workshops Screen")
            }
            composable(NavRoutes.Detail.route) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val description = backStackEntry.arguments?.getString("description") ?: ""
                val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""

                DetailScreen(name, description, imageUrl)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Surface {
        Text(text = title)
    }
}
