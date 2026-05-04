package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId

data class ArtForm(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val viewCount: Int = 0,
    val isLiked: Boolean = false
)
