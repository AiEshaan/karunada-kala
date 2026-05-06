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
import com.example.myapplication.data.model.Badge
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArtRepository(application)
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

    val badges: StateFlow<List<Badge>> = combine(_registrations, _enrollments, _myChronicles) { registrations, enrollments, chronicles ->
        listOf(
            Badge(
                id = "explorer",
                name = "Heritage Explorer",
                icon = "🏛",
                description = "View 1+ art forms",
                isUnlocked = true, // We assume they viewed at least one to be here or we could track views
                currentProgress = 1,
                totalRequired = 1
            ),
            Badge(
                id = "patron",
                name = "Cultural Patron",
                icon = "🎭",
                description = "Register for 3+ events",
                isUnlocked = registrations.size >= 3,
                currentProgress = registrations.size,
                totalRequired = 3
            ),
            Badge(
                id = "apprentice",
                name = "Eager Apprentice",
                icon = "🧑‍🏫",
                description = "Enroll in 1+ workshop",
                isUnlocked = enrollments.isNotEmpty(),
                currentProgress = enrollments.size,
                totalRequired = 1
            ),
            Badge(
                id = "chronicler",
                name = "Story Chronicler",
                icon = "📜",
                description = "Post 3+ chronicles",
                isUnlocked = chronicles.size >= 3,
                currentProgress = chronicles.size,
                totalRequired = 3
            ),
            Badge(
                id = "guardian",
                name = "Cultural Guardian",
                icon = "🏆",
                description = "Earn all other badges",
                isUnlocked = registrations.size >= 3 && enrollments.isNotEmpty() && chronicles.size >= 3,
                currentProgress = (if (registrations.size >= 3) 1 else 0) + (if (enrollments.isNotEmpty()) 1 else 0) + (if (chronicles.size >= 3) 1 else 0),
                totalRequired = 3
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun updateProfile(name: String, photoUrl: String) {
        val user = auth.currentUser ?: return
        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
            displayName = name
            if (photoUrl.isNotBlank()) {
                photoUri = android.net.Uri.parse(photoUrl)
            }
        }

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fetchJourney()
            }
        }
    }
}
