package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Workshop
import com.example.myapplication.data.repository.ArtRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkshopViewModel : ViewModel() {

    private val repository = ArtRepository()

    private val _workshops = MutableStateFlow<List<Workshop>>(emptyList())
    val workshops: StateFlow<List<Workshop>> = _workshops

    private val _enrollmentStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val enrollmentStatus: StateFlow<Map<String, Boolean>> = _enrollmentStatus

    private val _isEnrolling = MutableStateFlow(false)
    val isEnrolling: StateFlow<Boolean> = _isEnrolling

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uiEvent = MutableStateFlow<String?>(null)
    val uiEvent: StateFlow<String?> = _uiEvent

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"

    fun fetchWorkshops() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = repository.getWorkshops()
            _workshops.value = list
            
            // Check enrollment status for each workshop
            list.forEach { workshop ->
                checkEnrollment(workshop.id)
            }
            _isLoading.value = false
        }
    }

    fun enroll(workshop: Workshop) {
        viewModelScope.launch {
            _isEnrolling.value = true
            val result = repository.enrollInWorkshop(workshop.id, workshop.title, userId)
            if (result.isSuccess) {
                _enrollmentStatus.value = _enrollmentStatus.value + (workshop.id to true)
                _uiEvent.value = "Successfully enrolled in ${workshop.title}!"
                // Refresh list to update available slots
                val updatedList = repository.getWorkshops()
                _workshops.value = updatedList
            } else {
                _uiEvent.value = result.exceptionOrNull()?.message ?: "Enrollment failed"
            }
            _isEnrolling.value = false
        }
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
