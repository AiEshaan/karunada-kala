package com.example.myapplication.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtDao {
    @Query("SELECT * FROM art_forms")
    fun getAllArtForms(): Flow<List<ArtEntity>>

    @Query("SELECT * FROM art_forms WHERE category = :category")
    fun getArtFormsByCategory(category: String): Flow<List<ArtEntity>>

    @Query("SELECT * FROM art_forms WHERE id = :id")
    fun getArtFormById(id: String): Flow<ArtEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtForms(artForms: List<ArtEntity>)

    @Update
    suspend fun updateArtForm(artForm: ArtEntity)

    @Query("DELETE FROM art_forms")
    suspend fun clearAll()
}
