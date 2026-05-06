package com.example.myapplication.data.model

data class Badge(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val currentProgress: Int = 0,
    val totalRequired: Int = 1
)
