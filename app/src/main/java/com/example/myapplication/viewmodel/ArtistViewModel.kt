package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.repository.ArtRepository
import com.example.myapplication.ui.state.UiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArtRepository(application)

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _mentorshipRequestState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val mentorshipRequestState: StateFlow<UiState<Unit>> = _mentorshipRequestState

    fun loadArtist(name: String) {
        viewModelScope.launch {
            _selectedArtist.value = null // Reset state before loading new artist
            _isLoading.value = true
            _error.value = null
            repository.getArtistByName(name).onSuccess { artist ->
                _selectedArtist.value = artist
                if (artist == null) {
                    _error.value = "Artist not found"
                }
            }.onFailure { e ->
                _error.value = "Failed to load artist story: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun loadArtistById(artistId: String) {
        viewModelScope.launch {
            _selectedArtist.value = null
            _isLoading.value = true
            _error.value = null
            repository.getArtistById(artistId).onSuccess { artist ->
                _selectedArtist.value = artist
                if (artist == null) {
                    _error.value = "Artist not found"
                }
            }.onFailure { e ->
                _error.value = "Failed to load artist story: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun fetchArtists() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getArtists().onSuccess { result ->
                _artists.value = result
            }.onFailure { e ->
                _error.value = "Failed to load cultural map: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun requestMentorship(artistId: String, artForm: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _mentorshipRequestState.value = UiState.Loading
            repository.requestMentorship(userId, artistId, artForm).onSuccess {
                _mentorshipRequestState.value = UiState.Success(Unit)
            }.onFailure {
                _mentorshipRequestState.value = UiState.Error("Failed to send request. Please try again.")
            }
        }
    }

    fun resetMentorshipRequest() {
        _mentorshipRequestState.value = UiState.Idle
    }
}
