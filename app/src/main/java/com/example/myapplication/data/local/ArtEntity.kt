package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.ArtForm

@Entity(tableName = "art_forms")
data class ArtEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val artistId: String,
    val artistName: String,
    val viewCount: Int,
    val audioUrl: String,
    val videoUrl: String,
    val isLiked: Boolean
)

fun ArtForm.toEntity(): ArtEntity = ArtEntity(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    category = category,
    artistId = artistId,
    artistName = artistName,
    viewCount = viewCount,
    audioUrl = audioUrl,
    videoUrl = videoUrl,
    isLiked = isLiked
)

fun ArtEntity.toDomain(): ArtForm = ArtForm(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    category = category,
    artistId = artistId,
    artistName = artistName,
    viewCount = viewCount,
    audioUrl = audioUrl,
    videoUrl = videoUrl,
    isLiked = isLiked
)
