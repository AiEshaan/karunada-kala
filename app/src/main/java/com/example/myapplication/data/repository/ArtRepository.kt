package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.*
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import android.content.Context
import com.google.gson.reflect.TypeToken

import java.util.UUID

class ArtRepository(context: Context? = null) {

    private val db = FirebaseFirestore.getInstance()
    private val tag = "ArtRepository"
    private val cache = context?.let { CacheRepository(it) }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> fetchCollection(collectionName: String, clazz: Class<T>): Result<List<T>> {
        return try {
            val snapshot = db.collection(collectionName).get().await()
            val objects = snapshot.toObjects(clazz)
            
            // Update cache
            cache?.saveCollection(collectionName, objects)
            
            Result.success(objects)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching collection: $collectionName, trying cache...", e)
            // Try cache on failure
            cache?.let {
                val typeToken = TypeToken.getParameterized(List::class.java, clazz) as TypeToken<List<T>>
                val cached = it.getCollection(collectionName, typeToken)
                if (cached.isNotEmpty()) return Result.success(cached)
            }
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

    suspend fun getArtForms(): Result<List<ArtForm>> {
        return fetchCollection("arts", ArtForm::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> observeCollection(collectionName: String, clazz: Class<T>): Flow<List<T>> = flow {
        // Emit cached data first
        cache?.let {
            val typeToken = TypeToken.getParameterized(List::class.java, clazz) as TypeToken<List<T>>
            val cached = it.getCollection(collectionName, typeToken)
            if (cached.isNotEmpty()) emit(cached)
        }

        // Then observe Firestore
        val firestoreFlow = callbackFlow {
            val subscription = db.collection(collectionName)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Error observing collection $collectionName", error)
                        return@addSnapshotListener
                    }
                    val objects = snapshot?.toObjects(clazz) ?: emptyList()
                    trySend(objects)
                }
            awaitClose { subscription.remove() }
        }

        firestoreFlow.collect { objects ->
            emit(objects)
            // Update cache
            cache?.saveCollection(collectionName, objects)
        }
    }

    fun observeUserRegistrations(userId: String): Flow<List<Registration>> = callbackFlow {
        val subscription = db.collection("registrations")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val registrations = snapshot?.toObjects(Registration::class.java) ?: emptyList()
                trySend(registrations)
            }
        awaitClose { subscription.remove() }
    }

    fun observeUserEnrollments(userId: String): Flow<List<Enrollment>> = callbackFlow {
        val subscription = db.collection("enrollments")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val enrollments = snapshot?.toObjects(Enrollment::class.java) ?: emptyList()
                trySend(enrollments)
            }
        awaitClose { subscription.remove() }
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
            Log.e(tag, "Error enrolling in workshop: ${enrollment.workshopId}", e)
            Result.failure(e)
        }
    }

    suspend fun isEnrolled(workshopId: String, userId: String): Boolean {
        return try {
            val enrollmentRef = db.collection("enrollments").document("${userId}_${workshopId}")
            val snapshot = enrollmentRef.get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e(tag, "Error checking enrollment for workshop: $workshopId", e)
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
            Log.e(tag, "Error checking registration for event: $eventTitle", e)
            false
        }
    }

    suspend fun registerForEvent(registration: Registration): Result<String> {
        return try {
            val ticketId = "KALA-${UUID.randomUUID().toString().take(6).uppercase()}"
            val finalRegistration = registration.copy(id = ticketId)
            
            db.collection("registrations").document(ticketId).set(finalRegistration).await()
            Result.success(ticketId)
        } catch (e: Exception) {
            Log.e(tag, "Error registering for event: ${registration.eventTitle}", e)
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
            Log.e(tag, "Error fetching user registrations: $userId", e)
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
            Log.e(tag, "Error fetching user enrollments: $userId", e)
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
