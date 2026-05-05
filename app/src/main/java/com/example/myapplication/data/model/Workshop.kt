package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Workshop(
    @DocumentId override val id: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("artistId") val artistId: String = "",
    @PropertyName("artistName") val artistName: String = "",
    @PropertyName("artType") val artType: String = "",
    @PropertyName("date") val date: String = "",
    @get:PropertyName("fee") @set:PropertyName("fee") var feeValue: Any? = null,
    @PropertyName("availableSlots") val availableSlots: Int = 0,
    @PropertyName("imageUrl") val imageUrl: String = "",
    @PropertyName("location") val location: String = "",
    @PropertyName("lat") override val lat: Double = 0.0,
    @PropertyName("lng") override val lng: Double = 0.0
) : MapEntity {
    @get:Exclude
    val fee: String
        get() = when (val v = feeValue) {
            is Long -> if (v == 0L) "Free" else "₹$v"
            is String -> when {
                v.isEmpty() || v == "0" -> "Free"
                v.contains("₹") -> v
                v.any { it.isDigit() } -> "₹$v"
                else -> v
            }
            else -> "Free"
        }
}
