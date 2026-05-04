package com.example.myapplication.data.model

import com.google.firebase.Timestamp

import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null
)
