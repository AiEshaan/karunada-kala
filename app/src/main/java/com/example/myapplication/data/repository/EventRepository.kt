package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Registration
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EventRepository(context: Context? = null) {
    private val db = FirebaseFirestore.getInstance()
    private val tag = "EventRepository"
    private val cache = context?.let { CacheRepository(it) }

    suspend fun getEvents(): Result<List<Event>> {
        return try {
            val snapshot = db.collection("events").get().await()
            val objects = snapshot.toObjects(Event::class.java)
            cache?.saveCollection("events", objects)
            Result.success(objects)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching events, trying cache...", e)
            cache?.let {
                val typeToken = object : TypeToken<List<Event>>() {}
                val cached = it.getCollection("events", typeToken)
                if (cached.isNotEmpty()) return Result.success(cached)
            }
            Result.failure(e)
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
}
