package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.*
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await

class ArtRepository {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ArtRepository"

    private suspend fun <T> fetchCollection(collectionName: String, clazz: Class<T>): Result<List<T>> {
        return try {
            val snapshot = db.collection(collectionName).get().await()
            val objects = snapshot.toObjects(clazz)
            
            // Debugging image URLs
            objects.forEach { obj ->
                when (obj) {
                    is ArtForm -> Log.d("IMG_DEBUG", "ArtForm: ${obj.name}, URL: ${obj.imageUrl}")
                    is Artist -> Log.d("IMG_DEBUG", "Artist: ${obj.name}, URL: ${obj.photoUrl}")
                    is Event -> Log.d("IMG_DEBUG", "Event: ${obj.title}, URL: ${obj.imageUrl}")
                    is Workshop -> Log.d("IMG_DEBUG", "Workshop: ${obj.title}, URL: ${obj.imageUrl}")
                }
            }
            
            Result.success(objects)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching collection: $collectionName", e)
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
            Log.e(TAG, "Error fetching paginated art forms", e)
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
            Log.e(TAG, "Error incrementing view count for: $artName", e)
        }
    }

    suspend fun getArtForms(): Result<List<ArtForm>> {
        return fetchCollection("arts", ArtForm::class.java)
    }

    suspend fun getArtists(): Result<List<Artist>> {
        return fetchCollection("artists", Artist::class.java)
    }

    suspend fun getEvents(): Result<List<Event>> {
        return fetchCollection("events", Event::class.java)
    }

    suspend fun getWorkshops(): Result<List<Workshop>> {
        return fetchCollection("workshops", Workshop::class.java)
    }

    suspend fun enrollInWorkshop(enrollment: Enrollment): Result<Unit> {
        return try {
            val workshopRef = db.collection("workshops").document(enrollment.workshopId)
            val enrollmentRef = db.collection("enrollments").document("${enrollment.userId}_${enrollment.workshopId}")

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
                transaction.set(enrollmentRef, enrollment)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error enrolling in workshop: ${enrollment.workshopId}", e)
            Result.failure(e)
        }
    }

    suspend fun isEnrolled(workshopId: String, userId: String): Boolean {
        return try {
            val enrollmentRef = db.collection("enrollments").document("${userId}_${workshopId}")
            val snapshot = enrollmentRef.get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking enrollment for workshop: $workshopId", e)
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
            Log.e(TAG, "Error checking registration for event: $eventTitle", e)
            false
        }
    }

    suspend fun registerForEvent(registration: Registration): Result<Unit> {
        return try {
            db.collection("registrations").add(registration).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering for event: ${registration.eventTitle}", e)
            Result.failure(e)
        }
    }

    suspend fun getUserRegistrations(userId: String): Result<List<Registration>> {
        return try {
            val snapshot = db.collection("registrations")
                .whereEqualTo("userId", userId)
                .get().await()
            Result.success(snapshot.toObjects(Registration::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user registrations: $userId", e)
            Result.failure(e)
        }
    }

    suspend fun getUserEnrollments(userId: String): Result<List<Enrollment>> {
        return try {
            val snapshot = db.collection("enrollments")
                .whereEqualTo("userId", userId)
                .get().await()
            Result.success(snapshot.toObjects(Enrollment::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user enrollments: $userId", e)
            Result.failure(e)
        }
    }

    suspend fun getArtistById(artistId: String): Result<Artist?> {
        return try {
            val doc = db.collection("artists")
                .document(artistId)
                .get()
                .await()
            Result.success(if (doc.exists()) doc.toObject(Artist::class.java) else null)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching artist by ID: $artistId", e)
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
            Log.e(TAG, "Error fetching artist by name: $name", e)
            Result.failure(e)
        }
    }
}
