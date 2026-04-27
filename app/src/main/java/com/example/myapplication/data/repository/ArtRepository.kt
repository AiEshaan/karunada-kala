package com.example.myapplication.data.repository

import com.example.myapplication.data.model.ArtForm
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Workshop
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

    suspend fun getWorkshops(): List<Workshop> {
        return try {
            val snapshot = db.collection("workshops").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Workshop::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun enrollInWorkshop(workshopId: String, workshopTitle: String, userId: String): Result<Unit> {
        return try {
            val workshopRef = db.collection("workshops").document(workshopId)
            val enrollmentRef = db.collection("enrollments").document("${userId}_${workshopId}")

            db.runTransaction { transaction ->
                val workshopSnapshot = transaction.get(workshopRef)
                val availableSlots = workshopSnapshot.getLong("availableSlots") ?: 0

                if (availableSlots <= 0) {
                    throw Exception("No slots available")
                }

                val enrollmentSnapshot = transaction.get(enrollmentRef)
                if (enrollmentSnapshot.exists()) {
                    throw Exception("Already enrolled")
                }

                // 1. Decrement slots
                transaction.update(workshopRef, "availableSlots", availableSlots - 1)

                // 2. Create enrollment record
                val enrollmentData = hashMapOf(
                    "userId" to userId,
                    "workshopId" to workshopId,
                    "workshopTitle" to workshopTitle,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                transaction.set(enrollmentRef, enrollmentData)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isEnrolled(workshopId: String, userId: String): Boolean {
        return try {
            val enrollmentRef = db.collection("enrollments").document("${userId}_${workshopId}")
            val snapshot = enrollmentRef.get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isRegisteredForEvent(eventTitle: String, userId: String): Boolean {
        return try {
            val snapshot = db.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("eventTitle", eventTitle)
                .get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registerForEvent(registration: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("registrations").add(registration).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRegistrations(userId: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("registrations")
                .whereEqualTo("userId", userId)
                .get().await()
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserEnrollments(userId: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("enrollments")
                .whereEqualTo("userId", userId)
                .get().await()
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}