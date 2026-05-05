package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class ArtForm(
    @DocumentId val id: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("imageUrl") val imageUrl: String = "",
    @PropertyName("category") val category: String = "",
    @PropertyName("artistId") val artistId: String = "",
    @PropertyName("artistName") val artistName: String = "",
    @PropertyName("viewCount") val viewCount: Int = 0,
    @PropertyName("audioUrl") val audioUrl: String = "",
    @PropertyName("videoUrl") val videoUrl: String = "",
    @get:PropertyName("isLiked") @set:PropertyName("isLiked") var isLiked: Boolean = false
)
