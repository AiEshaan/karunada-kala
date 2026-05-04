package com.example.myapplication.data.model

import com.google.firebase.Timestamp

data class Enrollment(
    @com.google.firebase.firestore.DocumentId val id: String = "",
    val userId: String = "",
    val workshopId: String = "",
    val workshopTitle: String = "",
    val timestamp: Timestamp? = null
)
