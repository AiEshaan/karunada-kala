package com.example.myapplication.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Registration(
    @com.google.firebase.firestore.DocumentId val id: String = "",
    val userId: String = "",
    val eventTitle: String = "",
    val artType: String = "",
    val status: String = "Interested",
    val date: String = "",
    val timestamp: Timestamp? = null,
    val type: String = "Manual", // Internal source tracking
    
    // Aligned with Firestore extra fields
    val activityId: String = "",
    val activityTitle: String = "",
    val source: String = "App",
    val activityType: String = "Event",
    
    // New fields for Workshop registrations
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val reason: String = ""
)
