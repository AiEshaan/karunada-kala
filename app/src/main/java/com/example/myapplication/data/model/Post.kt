package com.example.myapplication.data.model

import java.util.Date

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Post(
    @DocumentId val id: String = "",
    @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String = "",
    @get:PropertyName("user_name") @set:PropertyName("user_name") var userName: String = "",
    @get:PropertyName("user_avatar") @set:PropertyName("user_avatar") var userAvatar: String = "",
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl: String = "",
    val caption: String = "",
    val location: String? = null,
    val timestamp: com.google.firebase.Timestamp? = null,
    val likes: Int = 0,
    @get:PropertyName("liked_by") @set:PropertyName("liked_by") var likedBy: List<String> = emptyList(),
    @PropertyName("media_type") var mediaType: String = "image"
)
