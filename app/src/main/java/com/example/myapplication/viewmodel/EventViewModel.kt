package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Registration
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.data.repository.ArtRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchEvents() {
        repository.observeCollection("events", Event::class.java)
            .onStart { _uiState.value = UiState.Loading }
            .onEach { list ->
                _events.value = list
                list.forEach { event ->
                    checkRegistration(event.title)
                }
                _uiState.value = UiState.Success(list)
            }
            .catch {
                val errorMsg = "Failed to load events. Please check your connection."
                _error.value = errorMsg
                _uiState.value = UiState.Error(errorMsg)
            }
            .launchIn(viewModelScope)
    }

    private fun checkRegistration(eventTitle: String) {
        viewModelScope.launch {
            try {
                val registered = repository.isRegisteredForEvent(eventTitle, userId)
                if (registered) {
                    _registrationStatus.update { it + (eventTitle to true) }
                }
            } catch (ignored: Exception) {
            }
        }
    }

    fun register(event: Event) {
        if (_isRegistering.value) return 
        
        viewModelScope.launch {
            _isRegistering.value = true
            val registration = Registration(
                userId = userId,
                eventTitle = event.title,
                artType = event.artType,
                timestamp = com.google.firebase.Timestamp.now(),
                status = "Interested",
            )
            repository.registerForEvent(registration).onSuccess { ticketId ->
                _registrationStatus.update { it + (event.title to true) }
                val successMsg = "Ticket Generated: $ticketId 📜! Added to Journey."
                _error.value = successMsg
            }.onFailure {
                val errorMsg = "Registration failed. Try again later."
                _error.value = errorMsg
            }
            _isRegistering.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
