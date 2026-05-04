package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Registration
import com.example.myapplication.ui.state.UiState
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

    private val _uiState = MutableStateFlow<UiState<List<Event>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Event>>> = _uiState

    private val _registrationStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val registrationStatus: StateFlow<Map<String, Boolean>> = _registrationStatus

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering

    private val _registrationUiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val registrationUiState: StateFlow<UiState<String>> = _registrationUiState

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchEvents() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _error.value = null
            repository.getEvents().onSuccess { list ->
                _events.value = list
                list.forEach { event ->
                    checkRegistration(event.title)
                }
                _uiState.value = UiState.Success(list)
            }.onFailure { e ->
                _error.value = "Failed to load events. Please check your connection."
                _uiState.value = UiState.Error(_error.value!!)
            }
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
                // Check registration status; failures are non-critical here
            }
        }
    }

    fun register(event: Event) {
        if (_isRegistering.value) return // Guard against double taps
        
        viewModelScope.launch {
            _isRegistering.value = true
            _registrationUiState.value = UiState.Loading
            val registration = Registration(
                userId = userId,
                eventTitle = event.title,
                artType = event.artType,
                timestamp = com.google.firebase.Timestamp.now(),
                status = "Interested"
            )
            repository.registerForEvent(registration).onSuccess {
                _registrationStatus.value = _registrationStatus.value + (event.title to true)
                val successMsg = "Added to your Journey 📜! (${event.title})"
                _error.value = successMsg
                _registrationUiState.value = UiState.Success(successMsg)
            }.onFailure { e ->
                val errorMsg = "Registration failed. Try again later."
                _error.value = errorMsg
                _registrationUiState.value = UiState.Error(errorMsg)
            }
            _isRegistering.value = false
        }
    }

    fun clearRegistrationState() {
        _registrationUiState.value = UiState.Idle
    }

    fun getEventCoordinates(eventTitle: String): Pair<Double, Double>? {
        val event = _events.value.find { it.title == eventTitle }
        return if (event != null) Pair(event.lat, event.lng) else null
    }

    fun clearError() {
        _error.value = null
    }
}
