package com.example.myapplication.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.myapplication.data.model.Comment
import com.example.myapplication.data.model.Post
import com.example.myapplication.data.model.Report
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class PostRepository(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val postsCollection = db.collection("posts")
    private val storage = FirebaseStorage.getInstance().reference
    private val tag = "PostRepository"

    suspend fun uploadImage(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                val baos = ByteArrayOutputStream()
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                val data = baos.toByteArray()

                val fileName = "posts/${UUID.randomUUID()}.jpg"
                val imageRef = storage.child(fileName)
                imageRef.putBytes(data).await()
                val downloadUrl = imageRef.downloadUrl.await()
                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun toggleLike(postId: String, userId: String): Result<Unit> {
        return try {
            val postRef = postsCollection.document(postId)
            db.runTransaction { transaction ->
                val snapshot = transaction[postRef]
                @Suppress("UNCHECKED_CAST")
                val likedBy = (snapshot.get("likedBy") as? List<String>) ?: emptyList()
                
                if (likedBy.contains(userId)) {
                    transaction.update(postRef, "likedBy", FieldValue.arrayRemove(userId))
                    transaction.update(postRef, "likes", FieldValue.increment(-1))
                } else {
                    transaction.update(postRef, "likedBy", FieldValue.arrayUnion(userId))
                    transaction.update(postRef, "likes", FieldValue.increment(1))
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, comment: Comment): Result<Unit> {
        return try {
            postsCollection.document(postId).collection("comments").add(comment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observePosts(): Flow<List<Post>> = callbackFlow {
        val subscription = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Observe posts failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { subscription.remove() }
    }

    fun observeComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val subscription = postsCollection.document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                trySend(comments)
            }
        awaitClose { subscription.remove() }
    }

    fun observeUserPosts(userId: String): Flow<List<Post>> = callbackFlow {
        Log.d(tag, "Observing user posts for: $userId")
        val subscription = postsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Observe user posts failed", error)
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(Post::class.java)
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(posts)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun reportPost(report: Report): Result<Unit> {
        return try {
            db.collection("reports").add(report).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPost(post: Post): Result<Unit> {
        return try {
            postsCollection.add(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
