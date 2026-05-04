package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Workshop
import com.example.myapplication.data.model.Enrollment
import com.example.myapplication.ui.state.UiState
import com.example.myapplication.data.repository.ArtRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkshopViewModel : ViewModel() {

    private val repository = ArtRepository()

    private val _workshops = MutableStateFlow<List<Workshop>>(emptyList())
    val workshops: StateFlow<List<Workshop>> = _workshops

    private val _uiState = MutableStateFlow<UiState<List<Workshop>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Workshop>>> = _uiState

    private val _enrollmentStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val enrollmentStatus: StateFlow<Map<String, Boolean>> = _enrollmentStatus

    private val _isEnrolling = MutableStateFlow(false)
    val isEnrolling: StateFlow<Boolean> = _isEnrolling

    private val _enrollmentUiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val enrollmentUiState: StateFlow<UiState<String>> = _enrollmentUiState

    private val _lastEnrolledWorkshop = MutableStateFlow<Workshop?>(null)
    val lastEnrolledWorkshop: StateFlow<Workshop?> = _lastEnrolledWorkshop

    private val _uiEvent = MutableStateFlow<String?>(null)
    val uiEvent: StateFlow<String?> = _uiEvent

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"

    fun fetchWorkshops() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getWorkshops().onSuccess { list ->
                _workshops.value = list
                
                // Check enrollment status for each workshop
                list.forEach { workshop ->
                    checkEnrollment(workshop.id)
                }
                _uiState.value = UiState.Success(list)
            }.onFailure {
                _uiState.value = UiState.Error("Failed to load workshops. Namaskara!")
            }
        }
    }

    fun enroll(workshop: Workshop) {
        viewModelScope.launch {
            _isEnrolling.value = true
            _enrollmentUiState.value = UiState.Loading
            
            val enrollment = Enrollment(
                userId = userId,
                workshopId = workshop.id,
                workshopTitle = workshop.title,
                timestamp = com.google.firebase.Timestamp.now()
            )
            
            repository.enrollInWorkshop(enrollment).onSuccess {
                _enrollmentStatus.value = _enrollmentStatus.value + (workshop.id to true)
                val successMsg = "Added to your Journey 📜! (${workshop.title})"
                _uiEvent.value = successMsg
                _lastEnrolledWorkshop.value = workshop
                _enrollmentUiState.value = UiState.Success(successMsg)
                // Update local workshop state with current availability
                repository.getWorkshops().onSuccess { updatedList ->
                    _workshops.value = updatedList
                }
            }.onFailure { e ->
                val errorMsg = e.message ?: "Enrollment failed"
                _uiEvent.value = errorMsg
                _enrollmentUiState.value = UiState.Error(errorMsg)
            }
            _isEnrolling.value = false
        }
    }

    fun clearEnrollmentState() {
        _enrollmentUiState.value = UiState.Idle
        _lastEnrolledWorkshop.value = null
    }

    private fun checkEnrollment(workshopId: String) {
        viewModelScope.launch {
            val enrolled = repository.isEnrolled(workshopId, userId)
            if (enrolled) {
                _enrollmentStatus.value = _enrollmentStatus.value + (workshopId to true)
            }
        }
    }

    fun clearUiEvent() {
        _uiEvent.value = null
    }
}
