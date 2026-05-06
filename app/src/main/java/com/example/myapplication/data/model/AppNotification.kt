package com.example.myapplication.data.model

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "general"
)
