package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

abstract class BaseRepository(context: Context? = null) {
    protected val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    protected val cache = context?.let { CacheRepository(it) }
    protected abstract val tag: String

    /**
     * Observes a Firestore collection and provides a Flow of its items.
     * Implements a "Cache-First, Then-Network" strategy.
     */
    fun <T : Any> observeCollection(collectionName: String, clazz: Class<T>): Flow<List<T>> = flow {
        // 1. Emit cached data immediately if available
        cache?.let {
            val typeToken = TypeToken.getParameterized(List::class.java, clazz) as TypeToken<List<T>>
            val cached = it.getCollection(collectionName, typeToken)
            if (cached.isNotEmpty()) {
                Log.d(tag, "Emitting cached data for $collectionName")
                emit(cached)
            }
        }

        // 2. Setup Firestore listener
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

        // 3. Collect from Firestore, emit, and update cache
        firestoreFlow.collect { objects ->
            emit(objects)
            cache?.saveCollection(collectionName, objects)
        }
    }
}
