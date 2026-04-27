package com.example.myapplication.data.model

data class ArtForm(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val viewCount: Int = 0 // Added to match Firestore field
)
