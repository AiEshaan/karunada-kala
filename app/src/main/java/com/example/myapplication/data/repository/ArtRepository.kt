package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.local.ArtDao
import com.example.myapplication.data.local.toDomain
import com.example.myapplication.data.local.toEntity
import com.example.myapplication.data.model.ArtForm
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ArtRepository(
    context: Context? = null,
    private val artDao: ArtDao? = null
) : BaseRepository(context) {
    override val tag = "ArtRepository"

    fun getLocalArtForms(): Flow<List<ArtForm>> {
        return artDao?.getAllArtForms()?.map { entities ->
            entities.map { it.toDomain() }
        } ?: callbackFlow { trySend(emptyList()) }
    }

    suspend fun refreshArtForms() {
        try {
            val snapshot = db.collection("arts").get().await()
            val objects = snapshot.toObjects(ArtForm::class.java)
            artDao?.insertArtForms(objects.map { it.toEntity() })
        } catch (e: Exception) {
            Log.e(tag, "Error refreshing art forms from Firebase", e)
        }
    }

    suspend fun getArtForms(): Result<List<ArtForm>> {
        return try {
            val snapshot = db.collection("arts").get().await()
            val objects = snapshot.toObjects(ArtForm::class.java)
            artDao?.insertArtForms(objects.map { it.toEntity() })
            Result.success(objects)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching art forms", e)
            Result.failure(e)
        }
    }

    suspend fun getArtFormsPaginated(limit: Long, lastDocument: DocumentSnapshot?): Result<Pair<List<ArtForm>, DocumentSnapshot?>> {
        return try {
            var query = db.collection("arts")
                .orderBy("viewCount", Query.Direction.DESCENDING)
                .limit(limit)
            
            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }
            
            val snapshot = query.get().await()
            val items = snapshot.toObjects(ArtForm::class.java)
            val lastDoc = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
            Result.success(Pair(items, lastDoc))
        } catch (e: Exception) {
            Log.e(tag, "Error fetching paginated art forms", e)
            Result.failure(e)
        }
    }

    suspend fun incrementViewCount(artName: String) {
        try {
            val result = db.collection("arts")
                .whereEqualTo("name", artName)
                .get()
                .await()
            for (document in result) {
                db.collection("arts")
                    .document(document.id)
                    .update("viewCount", FieldValue.increment(1))
                    .await()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error incrementing view count for: $artName", e)
        }
    }
}
