package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ArtRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JourneyViewModel : ViewModel() {

    private val repository = ArtRepository()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"

    private val _registrations = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val registrations: StateFlow<List<Map<String, Any>>> = _registrations

    private val _enrollments = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val enrollments: StateFlow<List<Map<String, Any>>> = _enrollments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchJourney() {
        viewModelScope.launch {
            _isLoading.value = true
            _registrations.value = repository.getUserRegistrations(userId)
            _enrollments.value = repository.getUserEnrollments(userId)
            _isLoading.value = false
        }
    }
}
