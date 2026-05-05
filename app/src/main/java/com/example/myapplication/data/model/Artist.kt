package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Artist(
    @DocumentId override val id: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("artType") var artType: String = "",
    @PropertyName("lat") override val lat: Double = 0.0,
    @PropertyName("lng") override val lng: Double = 0.0,
    @PropertyName("phone") val phone: String = "",
    @PropertyName("bio") val bio: String = "",
    @PropertyName("photoUrl") var photoUrl: String = "",
    @PropertyName("experienceYears") var experienceYears: Int = 10,
    @PropertyName("city") val city: String = "Karnataka",
    @PropertyName("guruName") var guruName: String = "Traditional Master",
    @PropertyName("studentsDescription") var studentsDescription: String = "Next Generation Apprentices"
) : MapEntity
