package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.repository.ArtistRepository
import com.example.myapplication.ui.state.UiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArtistRepository(application)

    private val _artistsState = MutableStateFlow<UiState<List<Artist>>>(UiState.Loading)
    val artistsState: StateFlow<UiState<List<Artist>>> = _artistsState

    private val _selectedArtistState = MutableStateFlow<UiState<Artist?>>(UiState.Idle)
    val selectedArtistState: StateFlow<UiState<Artist?>> = _selectedArtistState

    private val _mentorshipRequestState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val mentorshipRequestState: StateFlow<UiState<Unit>> = _mentorshipRequestState

    fun loadArtist(name: String) {
        viewModelScope.launch {
            _selectedArtistState.value = UiState.Loading
            repository.getArtistByName(name).onSuccess { artist ->
                _selectedArtistState.value = if (artist != null) {
                    UiState.Success(artist)
                } else {
                    UiState.Error("Artist not found")
                }
            }.onFailure { e ->
                _selectedArtistState.value = UiState.Error("Failed to load artist story: ${e.message}")
            }
        }
    }

    fun loadArtistById(artistId: String) {
        viewModelScope.launch {
            _selectedArtistState.value = UiState.Loading
            repository.getArtistById(artistId).onSuccess { artist ->
                _selectedArtistState.value = if (artist != null) {
                    UiState.Success(artist)
                } else {
                    UiState.Error("Artist not found")
                }
            }.onFailure { e ->
                _selectedArtistState.value = UiState.Error("Failed to load artist story: ${e.message}")
            }
        }
    }

    fun fetchArtists() {
        viewModelScope.launch {
            _artistsState.value = UiState.Loading
            repository.getArtists().onSuccess { result ->
                _artistsState.value = UiState.Success(result)
            }.onFailure { e ->
                _artistsState.value = UiState.Error("Failed to load cultural map: ${e.message}")
            }
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
