package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.ArtForm
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ArtRepository(context: Context? = null) {
    private val db = FirebaseFirestore.getInstance()
    private val tag = "ArtRepository"
    private val cache = context?.let { CacheRepository(it) }

    suspend fun getArtForms(): Result<List<ArtForm>> {
        return try {
            val snapshot = db.collection("arts").get().await()
            val objects = snapshot.toObjects(ArtForm::class.java)
            cache?.saveCollection("arts", objects)
            Result.success(objects)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching art forms, trying cache...", e)
            cache?.let {
                val typeToken = object : TypeToken<List<ArtForm>>() {}
                val cached = it.getCollection("arts", typeToken)
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

    fun <T : Any> observeCollection(collectionName: String, clazz: Class<T>): Flow<List<T>> = flow {
        cache?.let {
            val typeToken = TypeToken.getParameterized(List::class.java, clazz) as TypeToken<List<T>>
            val cached = it.getCollection(collectionName, typeToken)
            if (cached.isNotEmpty()) emit(cached)
        }

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
            cache?.saveCollection(collectionName, objects)
        }
    }
}
