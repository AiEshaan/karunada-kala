package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId

data class Artist(
    @DocumentId override val id: String = "",
    val name: String = "",
    val artType: String = "",
    override val lat: Double = 0.0,
    override val lng: Double = 0.0,
    val phone: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val experienceYears: Int = 10,
    val city: String = "Karnataka",
    val guruName: String = "Traditional Master",
    val studentsDescription: String = "Next Generation Apprentices"
) : MapEntity
