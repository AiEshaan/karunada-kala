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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import com.example.myapplication.data.repository.GeminiRepository

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostRepository(application)
    private val geminiRepository = GeminiRepository()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    
    private val _sortOrder = MutableStateFlow("Recent")
    val sortOrder: StateFlow<String> = _sortOrder

    private val _uiState = MutableStateFlow<UiState<List<Post>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Post>>> = _uiState

    private val _createPostState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createPostState: StateFlow<UiState<Unit>> = _createPostState

    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    private val _hiddenPostIds = MutableStateFlow<Set<String>>(emptySet())

    private val _aiCaptionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val aiCaptionState: StateFlow<UiState<String>> = _aiCaptionState

    val filteredPosts: StateFlow<List<Post>> = combine(_posts, _blockedUserIds, _hiddenPostIds, _sortOrder) { posts, blocked, hidden, sort ->
        val baseList = posts.filter { !blocked.contains(it.userId) && !hidden.contains(it.id) }
        when (sort) {
            "Trending" -> baseList.sortedByDescending { it.likes }
            else -> baseList.sortedByDescending { it.timestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observePosts()
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
    }

    private fun observePosts() {
        repository.observePosts()
            .onStart { _uiState.value = UiState.Loading }
            .onEach { postList ->
                _posts.value = postList
                _uiState.value = UiState.Success(postList)
            }
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Failed to load archives")
            }
            .launchIn(viewModelScope)
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
        val userId = auth.currentUser?.uid ?: "guest_user"
        viewModelScope.launch {
            repository.toggleLike(postId, userId)
        }
    }

    fun fetchComments(postId: String) {
        repository.observeComments(postId)
            .onEach { postComments ->
                _comments.update { it + (postId to postComments) }
            }
            .launchIn(viewModelScope)
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
                timestamp = Timestamp.now(),
            )
            val result = repository.addComment(postId, comment)
            if (result.isSuccess) {
                fetchComments(postId)
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
            hidePost(postId)
        }
    }

    fun blockUser(userId: String) {
        _blockedUserIds.update { it + userId }
    }

    fun hidePost(postId: String) {
        _hiddenPostIds.update { it + postId }
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
                timestamp = Timestamp.now(),
            )

            val result = repository.addPost(post)
            if (result.isSuccess) {
                _createPostState.value = UiState.Success(Unit)
            } else {
                _createPostState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to chronicle moment")
            }
        }
    }

    fun resetCreatePostState() {
        _createPostState.value = UiState.Idle
    }

    fun generateAiCaption(bitmap: Bitmap) {
        viewModelScope.launch {
            _aiCaptionState.value = UiState.Loading
            geminiRepository.suggestCaptionForImage(bitmap).onSuccess { caption ->
                _aiCaptionState.value = UiState.Success(caption)
            }.onFailure {
                _aiCaptionState.value = UiState.Error("A moment steeped in Karnataka’s timeless heritage.")
            }
        }
    }

    fun resetAiCaption() {
        _aiCaptionState.value = UiState.Idle
    }
}
