package com.example.myapplication.data.model

data class Workshop(
    val id: String = "",
    val title: String = "",
    val artistName: String = "",
    val artType: String = "",
    val date: String = "",
    val fee: String = "",
    val availableSlots: Int = 0,
    val imageUrl: String = ""
)