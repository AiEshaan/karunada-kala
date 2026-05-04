package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Artist(
    @DocumentId override val id: String = "",
    val name: String = "",
    @get:PropertyName("art_type") @set:PropertyName("art_type") var artType: String = "",
    override val lat: Double = 0.0,
    override val lng: Double = 0.0,
    val phone: String = "",
    val bio: String = "",
    @get:PropertyName("photo_url") @set:PropertyName("photo_url") var photoUrl: String = "",
    @get:PropertyName("experience_years") @set:PropertyName("experience_years") var experienceYears: Int = 10,
    val city: String = "Karnataka",
    @get:PropertyName("guru_name") @set:PropertyName("guru_name") var guruName: String = "Traditional Master",
    @get:PropertyName("students_description") @set:PropertyName("students_description") var studentsDescription: String = "Next Generation Apprentices"
) : MapEntity
