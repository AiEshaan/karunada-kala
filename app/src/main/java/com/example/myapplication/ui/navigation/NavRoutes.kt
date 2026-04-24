package com.example.myapplication.ui.navigation

sealed class NavRoutes(val route: String) {
    object Explore : NavRoutes("explore")
    object Map : NavRoutes("map")
    object Events : NavRoutes("events")
    object Workshops : NavRoutes("workshops")
    object Detail : NavRoutes("detail/{name}/{description}/{imageUrl}")
}