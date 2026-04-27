package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ArtForm
import com.example.myapplication.data.model.ArtRecommendation
import com.example.myapplication.data.repository.ArtRepository
import com.example.myapplication.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtViewModel : ViewModel() {

    private val repository = ArtRepository()
    private val geminiRepository = GeminiRepository()

    private val _artForms = MutableStateFlow<List<ArtForm>>(emptyList())
    val artForms: StateFlow<List<ArtForm>> = _artForms

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _viewedArts = MutableStateFlow<List<String>>(emptyList())
    val viewedArts = _viewedArts.asStateFlow()

    private val _recommendations = MutableStateFlow<List<ArtRecommendation>>(emptyList())
    val recommendations = _recommendations.asStateFlow()

    private val _aiDescriptions = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiDescriptions = _aiDescriptions.asStateFlow()

    fun fetchArtForms() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getArtForms()
            _artForms.value = result
            _isLoading.value = false
        }
    }

    fun addViewedArt(name: String) {
        if (!_viewedArts.value.contains(name)) {
            _viewedArts.value = _viewedArts.value + name
        }
    }

    fun loadRecommendations(allArts: List<String>) {
        if (viewedArts.value.isEmpty()) return
        
        viewModelScope.launch {
            val result = geminiRepository.generateRecommendations(
                viewedArts.value,
                allArts
            )
            _recommendations.value = result
        }
    }

    fun generateAiDescriptionIfNeeded(name: String, currentDescription: String, category: String) {
        if (currentDescription.length >= 50 || _aiDescriptions.value.containsKey(name)) return

        viewModelScope.launch {
            val aiDesc = geminiRepository.generateArtDescription(name, category)
            _aiDescriptions.value = _aiDescriptions.value + (name to aiDesc)
        }
    }
}
