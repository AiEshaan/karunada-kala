package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.Artist
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await

class ArtistRepository(context: Context? = null) : BaseRepository(context) {
    override val tag = "ArtistRepository"

    suspend fun getArtists(): Result<List<Artist>> {
        return try {
            val snapshot = db.collection("artists").get().await()
            val objects = snapshot.toObjects(Artist::class.java)
            cache?.saveCollection("artists", objects)
            Result.success(objects)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching artists, trying cache...", e)
            cache?.let {
                val typeToken = object : TypeToken<List<Artist>>() {}
                val cached = it.getCollection("artists", typeToken)
                if (cached.isNotEmpty()) return Result.success(cached)
            }
            Result.failure(e)
        }
    }

    suspend fun getArtistById(artistId: String): Result<Artist?> {
        return try {
            val doc = db.collection("artists").document(artistId).get().await()
            Result.success(if (doc.exists()) doc.toObject(Artist::class.java) else null)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching artist by ID: $artistId", e)
            Result.failure(e)
        }
    }

    suspend fun getArtistByName(name: String): Result<Artist?> {
        return try {
            val result = db.collection("artists")
                .whereEqualTo("name", name)
                .get()
                .await()
            Result.success(if (!result.isEmpty) result.documents[0].toObject(Artist::class.java) else null)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching artist by name: $name", e)
            Result.failure(e)
        }
    }

    suspend fun requestMentorship(
        userId: String,
        artistId: String,
        artForm: String
    ): Result<Unit> {
        return try {
            val request = hashMapOf(
                "userId" to userId,
                "artistId" to artistId,
                "artForm" to artForm,
                "status" to "pending",
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            db.collection("mentorship_requests").add(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
