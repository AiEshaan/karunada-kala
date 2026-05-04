package com.example.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Report(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val reason: String = "",
    val timestamp: Timestamp? = null
)
