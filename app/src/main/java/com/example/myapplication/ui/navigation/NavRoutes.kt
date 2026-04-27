package com.example.myapplication.ui.navigation

sealed class NavRoutes(val route: String) {
    object Explore : NavRoutes("explore")
    object Map : NavRoutes("map")
    object Events : NavRoutes("events")
    object Workshops : NavRoutes("workshops")
    object Journey : NavRoutes("journey")
    object Onboarding : NavRoutes("onboarding")
    object AiGuide : NavRoutes("ai_guide")
    object Detail : NavRoutes("detail/{name}/{description}/{imageUrl}/{artistId}/{category}")
    object ArtistDetail : NavRoutes("artist_detail/{artistId}")

    companion object {
        fun detail(name: String, description: String, imageUrl: String, artistId: String, category: String): String {
            return "detail/$name/$description/$imageUrl/$artistId/$category"
        }
        fun artistDetail(artistId: String): String {
            return "artist_detail/$artistId"
        }
    }
}
