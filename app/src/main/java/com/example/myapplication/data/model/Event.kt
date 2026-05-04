package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Event(
    @DocumentId override val id: String = "",
    val title: String = "",
    @get:PropertyName("art_type") @set:PropertyName("art_type") var artType: String = "",
    val date: String = "",
    val location: String = "",
    val description: String = "",
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl: String = "",
    override val lat: Double = 0.0,
    override val lng: Double = 0.0
) : MapEntity
