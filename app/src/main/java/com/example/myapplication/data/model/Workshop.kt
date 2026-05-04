package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Workshop(
    @DocumentId override val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val artType: String = "",
    val date: String = "",
    @get:PropertyName("fee") @set:PropertyName("fee") var feeInternal: Any = 0L,
    val availableSlots: Int = 0,
    val imageUrl: String = "",
    override val lat: Double = 0.0,
    override val lng: Double = 0.0
) : MapEntity {
    // Computed property for the UI to use
    val fee: String get() = feeInternal.toString()
}
