package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Event(
    @DocumentId override val id: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("artType") var artType: String = "",
    @PropertyName("date") val date: String = "",
    @PropertyName("location") val location: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("imageUrl") var imageUrl: String = "",
    override val lat: Double = 0.0,
    override val lng: Double = 0.0
) : MapEntity
