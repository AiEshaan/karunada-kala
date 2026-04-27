package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.repository.ArtRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ArtistViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val repository = ArtRepository()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadArtist(name: String) {
        viewModelScope.launch {
            _selectedArtist.value = null // Reset state before loading new artist
            _isLoading.value = true
            try {
                val result = firestore.collection("artists")
                    .whereEqualTo("name", name)
                    .get()
                    .await()
                if (!result.isEmpty) {
                    _selectedArtist.value = result.documents[0].toObject(Artist::class.java)
                }
            } catch (e: Exception) {
                // Handle error
            }
            _isLoading.value = false
        }
    }

    fun loadArtistById(artistId: String) {
        viewModelScope.launch {
            _selectedArtist.value = null
            _isLoading.value = true
            try {
                val doc = firestore.collection("artists")
                    .document(artistId)
                    .get()
                    .await()
                if (doc.exists()) {
                    _selectedArtist.value = doc.toObject(Artist::class.java)
                }
            } catch (e: Exception) {
                // Handle error
            }
            _isLoading.value = false
        }
    }

    fun fetchArtists() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getArtists()
            _artists.value = result
            _isLoading.value = false
        }
    }
}
