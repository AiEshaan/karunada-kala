package com.example.myapplication.ui.navigation

import android.net.Uri
import androidx.navigation.NavController
import com.example.myapplication.data.model.ArtForm

sealed class NavRoutes(val route: String) {
    // Core Navigation (The 5-Tab System)
    object Explore : NavRoutes("explore")
    object Map : NavRoutes("map?lat={lat}&lng={lng}")
    object Events : NavRoutes("events")
    object Community : NavRoutes("community")
    object Journey : NavRoutes("journey")

    // Utility & Secondary Screens
    object Onboarding : NavRoutes("onboarding")
    object Login : NavRoutes("login")
    object SignUp : NavRoutes("signup")
    object Detail : NavRoutes("detail/{name}/{description}/{imageUrl}/{artistId}/{category}")
    object ArtistDetail : NavRoutes("artist_detail/{artistId}")

    companion object {
        fun detail(name: String, description: String, imageUrl: String, artistId: String, category: String): String {
            return "detail/$name/$description/$imageUrl/$artistId/$category"
        }
        fun artistDetail(artistId: String): String {
            return "artist_detail/$artistId"
        }
        fun map(lat: Double? = null, lng: Double? = null): String {
            return if (lat != null && lng != null) "map?lat=$lat&lng=$lng" else "map"
        }

        fun navigateToDetail(navController: NavController, art: ArtForm) {
            val encodedDescription = Uri.encode(art.description)
            val encodedImageUrl = Uri.encode(art.imageUrl)
            val encodedArtistId = Uri.encode(art.artistId)
            val encodedCategory = Uri.encode(art.category)
            navController.navigate(
                detail(
                    art.name,
                    encodedDescription,
                    encodedImageUrl,
                    encodedArtistId,
                    encodedCategory
                )
            )
        }

        fun navigateToDetail(
            navController: NavController,
            name: String,
            description: String,
            imageUrl: String,
            artistId: String,
            category: String
        ) {
            val encodedName = Uri.encode(name)
            val encodedDesc = Uri.encode(description)
            val encodedImg = Uri.encode(imageUrl)
            val finalArtistId = if (artistId.isNotBlank()) artistId else "none"
            navController.navigate("detail/$encodedName/$encodedDesc/$encodedImg/$finalArtistId/$category")
        }
    }
}
