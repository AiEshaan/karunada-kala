package com.example.myapplication.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Comment
import com.example.myapplication.data.model.Post
import com.example.myapplication.data.model.Report
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.data.repository.PostRepository
import com.example.myapplication.core.utils.SoundManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostRepository(application)
    private val auth = FirebaseAuth.getInstance()
    private val soundManager = SoundManager(application)

    val currentUserId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _uiState = MutableStateFlow<UiState<List<Post>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Post>>> = _uiState

    private val _createPostState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createPostState: StateFlow<UiState<Unit>> = _createPostState

    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    private val _hiddenPostIds = MutableStateFlow<Set<String>>(emptySet())

    val filteredPosts: StateFlow<List<Post>> = combine(_posts, _blockedUserIds, _hiddenPostIds) { posts, blocked, hidden ->
        posts.filter { !blocked.contains(it.userId) && !hidden.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = repository.fetchPosts()
            if (result.isSuccess) {
                val postList = result.getOrDefault(emptyList())
                _posts.value = postList
                _uiState.value = UiState.Success(postList)
            } else {
                _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load archives")
            }
        }
    }

    fun createPostWithImage(imageUri: Uri, caption: String, location: String?) {
        viewModelScope.launch {
            _createPostState.value = UiState.Loading
            val uploadResult = repository.uploadImage(imageUri)
            
            if (uploadResult.isSuccess) {
                val imageUrl = uploadResult.getOrNull() ?: ""
                createPost(imageUrl, caption, location)
            } else {
                _createPostState.value = UiState.Error("Image upload failed: ${uploadResult.exceptionOrNull()?.message}")
            }
        }
    }

    fun toggleLike(postId: String) {
        val userId = currentUserId
        
        // Optimistic Update: Update the local state immediately
        val currentPosts = _posts.value
        val updatedPosts = currentPosts.map { post ->
            if (post.id == postId) {
                val isCurrentlyLiked = post.likedBy.contains(userId)
                val newLikedBy = if (isCurrentlyLiked) {
                    post.likedBy - userId
                } else {
                    post.likedBy + userId
                }
                val newLikes = if (isCurrentlyLiked) post.likes - 1 else post.likes + 1
                post.copy(likedBy = newLikedBy, likes = newLikes.coerceAtLeast(0))
            } else post
        }
        _posts.value = updatedPosts

        viewModelScope.launch {
            val result = repository.toggleLike(postId, userId)
            if (result.isFailure) {
                // Rollback if network call fails
                _posts.value = currentPosts
            }
        }
    }

    fun fetchComments(postId: String) {
        viewModelScope.launch {
            val result = repository.fetchComments(postId)
            if (result.isSuccess) {
                val postComments = result.getOrDefault(emptyList())
                _comments.value = _comments.value + (postId to postComments)
            }
        }
    }

    fun addComment(postId: String, text: String) {
        val user = auth.currentUser
        val userId = user?.uid ?: "guest_user"
        val userName = if (user?.isAnonymous == false) user.displayName ?: "Cultural Guardian" else "Guest Explorer"
        val userAvatar = user?.photoUrl?.toString() ?: ""

        viewModelScope.launch {
            val comment = Comment(
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                text = text,
                timestamp = Timestamp.now()
            )
            val result = repository.addComment(postId, comment)
            if (result.isSuccess) {
                fetchComments(postId)
            }
        }
    }

    fun fetchUserPosts() {
        val userId = auth.currentUser?.uid ?: "guest_user"
        viewModelScope.launch {
            val result = repository.fetchUserPosts(userId)
            if (result.isSuccess) {
                _userPosts.value = result.getOrDefault(emptyList())
            }
        }
    }

    fun reportPost(postId: String, reason: String) {
        val userId = auth.currentUser?.uid ?: "guest_user"
        viewModelScope.launch {
            val report = Report(
                postId = postId,
                userId = userId,
                reason = reason,
                timestamp = Timestamp.now()
            )
            repository.reportPost(report)
            hidePost(postId) // Automatically hide reported posts for the user
        }
    }

    fun blockUser(userId: String) {
        _blockedUserIds.value = _blockedUserIds.value + userId
    }

    fun hidePost(postId: String) {
        _hiddenPostIds.value = _hiddenPostIds.value + postId
    }

    fun createPost(imageUrl: String, caption: String, location: String?) {
        val user = auth.currentUser
        val userId = user?.uid ?: "guest_user"
        val userName = if (user?.isAnonymous == false) user.displayName ?: "Cultural Guardian" else "Guest Explorer"
        val userAvatar = user?.photoUrl?.toString() ?: ""

        viewModelScope.launch {
            _createPostState.value = UiState.Loading
            val post = Post(
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                imageUrl = imageUrl,
                caption = caption,
                location = location,
                timestamp = Timestamp.now()
            )

            val result = repository.addPost(post)
            if (result.isSuccess) {
                _createPostState.value = UiState.Success(Unit)
                soundManager.playSound("SHUTTER")
                loadPosts() // Refresh feed
            } else {
                _createPostState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to chronicle moment")
            }
        }
    }

    fun resetCreatePostState() {
        _createPostState.value = UiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
