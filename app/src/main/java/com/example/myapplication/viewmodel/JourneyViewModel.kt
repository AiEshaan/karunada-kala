package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Post
import com.example.myapplication.data.model.Registration
import com.example.myapplication.data.model.Enrollment
import com.example.myapplication.data.repository.ArtRepository
import com.example.myapplication.data.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArtRepository()
    private val postRepository = PostRepository(application)
    private val auth = FirebaseAuth.getInstance()
    
    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    private val _registrations = MutableStateFlow<List<Registration>>(emptyList())
    val registrations: StateFlow<List<Registration>> = _registrations

    private val _enrollments = MutableStateFlow<List<Enrollment>>(emptyList())
    val enrollments: StateFlow<List<Enrollment>> = _enrollments

    private val _myChronicles = MutableStateFlow<List<Post>>(emptyList())
    val myChronicles: StateFlow<List<Post>> = _myChronicles

    private val _userName = MutableStateFlow("Guest Explorer")
    val userName: StateFlow<String> = _userName

    private val _userAvatar = MutableStateFlow("")
    val userAvatar: StateFlow<String> = _userAvatar

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchJourney() {
        _isLoading.value = true
        
        // Profile Info
        val user = auth.currentUser
        _userName.value = if (user?.isAnonymous == false) user.displayName ?: "Cultural Guardian" else "Guest Explorer"
        _userAvatar.value = user?.photoUrl?.toString() ?: ""

        // Observe Registrations
        repository.observeUserRegistrations(userId)
            .onEach { _registrations.value = it }
            .launchIn(viewModelScope)

        // Observe Enrollments
        repository.observeUserEnrollments(userId)
            .onEach { _enrollments.value = it }
            .launchIn(viewModelScope)
        
        // Observe Chronicles
        postRepository.observeUserPosts(userId)
            .onEach { 
                _myChronicles.value = it
                _isLoading.value = false // Stop loading after first major data emission
            }
            .launchIn(viewModelScope)
    }

    fun addManualEntry(title: String, subtitle: String) {
        viewModelScope.launch {
            val entry = Registration(
                userId = userId,
                eventTitle = title,
                artType = subtitle,
                status = "Guided",
                timestamp = com.google.firebase.Timestamp.now(),
                type = "AI_Suggestion"
            )
            repository.registerForEvent(entry).onSuccess {
                fetchJourney()
            }
        }
    }
}
