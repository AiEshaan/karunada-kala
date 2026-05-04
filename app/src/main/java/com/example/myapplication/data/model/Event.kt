package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId override val id: String = "",
    val title: String = "",
    val artType: String = "",
    val date: String = "",
    val location: String = "",
    val description: String = "",
    val imageUrl: String = "",
    override val lat: Double = 0.0,
    override val lng: Double = 0.0
) : MapEntity
