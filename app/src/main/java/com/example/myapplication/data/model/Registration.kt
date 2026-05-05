package com.example.myapplication.data.model

import com.google.firebase.Timestamp

data class Registration(
    @com.google.firebase.firestore.DocumentId val id: String = "",
    val userId: String = "",
    val eventTitle: String = "",
    val artType: String = "",
    val status: String = "Interested",
    val date: String = "",
    val timestamp: Timestamp? = null,
    val type: String = "Manual" // Internal source tracking
)
