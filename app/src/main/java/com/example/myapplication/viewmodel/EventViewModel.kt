package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.repository.ArtRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val repository = ArtRepository()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _registrationStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val registrationStatus: StateFlow<Map<String, Boolean>> = _registrationStatus

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val list = repository.getEvents()
                if (list.isEmpty()) {
                    // Handled by UI empty state, but could set error if unexpected
                }
                _events.value = list
                list.forEach { event ->
                    checkRegistration(event.title)
                }
            } catch (e: Exception) {
                _error.value = "Failed to load events. Please check your connection."
            }
            _isLoading.value = false
        }
    }

    private fun checkRegistration(eventTitle: String) {
        viewModelScope.launch {
            try {
                val registered = repository.isRegisteredForEvent(eventTitle, userId)
                if (registered) {
                    _registrationStatus.value = _registrationStatus.value + (eventTitle to true)
                }
            } catch (e: Exception) {
                // Silent fail for status check
            }
        }
    }

    fun register(event: Event) {
        if (_isRegistering.value) return // Guard against double taps
        
        viewModelScope.launch {
            _isRegistering.value = true
            val registration = hashMapOf(
                "userId" to userId,
                "eventTitle" to event.title,
                "artType" to event.artType,
                "date" to event.date,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "status" to "Interested"
            )
            val result = repository.registerForEvent(registration)
            if (result.isSuccess) {
                _registrationStatus.value = _registrationStatus.value + (event.title to true)
                _error.value = "Registered for ${event.title}!"
            } else {
                _error.value = "Registration failed. Try again later."
            }
            _isRegistering.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
