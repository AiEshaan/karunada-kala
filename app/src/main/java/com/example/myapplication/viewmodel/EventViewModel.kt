package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Registration
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.data.repository.ArtRepository
import com.example.myapplication.data.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EventRepository(application)
    private val artRepository = ArtRepository(application)
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

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val filteredEvents: StateFlow<List<Event>> = combine(_events, _selectedFilter) { events, filter ->
        val calendar = java.util.Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        val currentYear = calendar.get(java.util.Calendar.YEAR)

        when (filter) {
            "All" -> events
            "This Week" -> {
                val sevenDaysLater = java.util.Calendar.getInstance()
                sevenDaysLater.add(java.util.Calendar.DAY_OF_YEAR, 7)
                events.filter { event ->
                    try {
                        val datePart = event.date.split("-")[0].trim()
                        val eventDate = sdf.parse(datePart)
                        eventDate?.let {
                            val cal = java.util.Calendar.getInstance()
                            cal.time = it
                            cal.set(java.util.Calendar.YEAR, currentYear)
                            cal.timeInMillis >= System.currentTimeMillis() && cal.timeInMillis <= sevenDaysLater.timeInMillis
                        } ?: false
                    } catch (e: Exception) { false }
                }
            }
            "This Month" -> {
                val thirtyDaysLater = java.util.Calendar.getInstance()
                thirtyDaysLater.add(java.util.Calendar.DAY_OF_YEAR, 30)
                events.filter { event ->
                    try {
                        val datePart = event.date.split("-")[0].trim()
                        val eventDate = sdf.parse(datePart)
                        eventDate?.let {
                            val cal = java.util.Calendar.getInstance()
                            cal.time = it
                            cal.set(java.util.Calendar.YEAR, currentYear)
                            cal.timeInMillis >= System.currentTimeMillis() && cal.timeInMillis <= thirtyDaysLater.timeInMillis
                        } ?: false
                    } catch (e: Exception) { false }
                }
            }
            else -> events.filter { it.artType.equals(filter, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun fetchEvents() {
        artRepository.observeCollection("events", Event::class.java)
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
