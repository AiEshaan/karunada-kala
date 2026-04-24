package com.example.myapplication.data.repository

import com.example.myapplication.data.model.ArtForm
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ArtRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getArtForms(): List<ArtForm> {
        return try {
            val snapshot = db.collection("arts").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ArtForm::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getArtists(): List<Artist> {
        return try {
            val snapshot = db.collection("artists").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Artist::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getEvents(): List<Event> {
        return try {
            val snapshot = db.collection("events").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Event::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}