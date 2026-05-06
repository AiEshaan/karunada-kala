package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.Enrollment
import com.example.myapplication.data.model.Workshop
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkshopRepository(context: Context? = null) {
    private val db = FirebaseFirestore.getInstance()
    private val tag = "WorkshopRepository"
    private val cache = context?.let { CacheRepository(it) }

    suspend fun getWorkshops(): Result<List<Workshop>> {
        return try {
            val snapshot = db.collection("workshops").get().await()
            val objects = snapshot.toObjects(Workshop::class.java)
            cache?.saveCollection("workshops", objects)
            Result.success(objects)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching workshops, trying cache...", e)
            cache?.let {
                val typeToken = object : TypeToken<List<Workshop>>() {}
                val cached = it.getCollection("workshops", typeToken)
                if (cached.isNotEmpty()) return Result.success(cached)
            }
            Result.failure(e)
        }
    }

    suspend fun enrollInWorkshop(enrollment: Enrollment): Result<Unit> {
        return try {
            val workshopRef = db.collection("workshops").document(enrollment.workshopId)
            val enrollmentRef = db.collection("enrollments").document("${enrollment.userId}_${enrollment.workshopId}")

            db.runTransaction { transaction ->
                val workshopSnapshot = transaction.get(workshopRef)
                if (!workshopSnapshot.exists()) throw Exception("Workshop not found")

                val availableSlots = workshopSnapshot.getLong("availableSlots") ?: 0
                if (availableSlots <= 0) throw Exception("No slots available")

                val enrollmentSnapshot = transaction.get(enrollmentRef)
                if (enrollmentSnapshot.exists()) throw Exception("Already enrolled")

                transaction.update(workshopRef, "availableSlots", availableSlots - 1)
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
}
