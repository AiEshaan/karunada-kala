package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId

data class Workshop(
    @DocumentId override val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val artType: String = "",
    val date: String = "",
    val fee: String = "",
    val availableSlots: Int = 0,
    val imageUrl: String = "",
    override val lat: Double = 0.0,
    override val lng: Double = 0.0
) : MapEntity
