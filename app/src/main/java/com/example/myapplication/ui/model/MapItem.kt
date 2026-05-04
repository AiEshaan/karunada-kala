package com.example.myapplication.ui.model

import com.google.maps.android.clustering.ClusterItem
import com.google.android.gms.maps.model.LatLng
import com.example.myapplication.data.model.MapEntity

data class MapItem(
    val entity: MapEntity,
    val type: String, // "Artists", "Events", "Workshops"
    val itemTitle: String
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(entity.lat, entity.lng)
    override fun getTitle(): String = itemTitle
    override fun getSnippet(): String = type
    override fun getZIndex(): Float? = null
    
    val id: String get() = entity.id
    val lat: Double get() = entity.lat
    val lng: Double get() = entity.lng
    val data: MapEntity get() = entity
}
