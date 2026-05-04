package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ArtForm
import com.example.myapplication.data.model.ArtRecommendation
import com.example.myapplication.data.repository.ArtRepository
import com.example.myapplication.data.repository.GeminiRepository
import com.example.myapplication.data.repository.SearchPrefsRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArtRepository()
    private val geminiRepository = GeminiRepository()
    private val searchPrefs = SearchPrefsRepository(application)

    private val _artForms = MutableStateFlow<List<ArtForm>>(emptyList())
    val artForms: StateFlow<List<ArtForm>> = _artForms

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches

    private val _trendingItems = MutableStateFlow<List<String>>(listOf("Yakshagana", "Channapatna Toys", "Mysore Silk", "Lambani Craft"))
    val trendingItems: StateFlow<List<String>> = _trendingItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _viewedArts = MutableStateFlow<List<String>>(emptyList())
    val viewedArts = _viewedArts.asStateFlow()

    private val _recommendations = MutableStateFlow<List<ArtRecommendation>>(emptyList())
    val recommendations = _recommendations.asStateFlow()

    private val _aiDescriptions = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiDescriptions = _aiDescriptions.asStateFlow()

    private val _artLegends = MutableStateFlow<Map<String, String>>(emptyMap())
    val artLegends = _artLegends.asStateFlow()

    private var lastArtDocument: DocumentSnapshot? = null
    private val PAGE_SIZE = 10L
    private var isLastPage = false

    init {
        loadMoreArtForms()
        viewModelScope.launch {
            searchPrefs.recentSearchesFlow.collectLatest {
                _recentSearches.value = it
                if (_searchQuery.value.isEmpty()) {
                    _suggestions.value = (it + _trendingItems.value).distinct().take(5)
                }
            }
        }
    }

    fun loadMoreArtForms() {
        if (_isLoading.value || isLastPage) return

        viewModelScope.launch {
            _isLoading.value = true
            repository.getArtFormsPaginated(PAGE_SIZE, lastArtDocument).onSuccess { (items, lastDoc) ->
                if (items.isEmpty()) {
                    isLastPage = true
                } else {
                    _artForms.value = _artForms.value + items
                    lastArtDocument = lastDoc
                    updateRecommendations(_artForms.value)
                }
            }.onFailure {
                // If an error occurs, don't set isLastPage = true so user can retry
            }
            _isLoading.value = false
        }
    }

    fun fetchArtForms() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getArtForms().onSuccess { forms ->
                _artForms.value = forms
                updateRecommendations(forms)
            }
            _isLoading.value = false
        }
    }

    private fun updateRecommendations(forms: List<ArtForm>) {
        val ranked = forms.sortedByDescending { 
            (if (it.isLiked) 10 else 0) + (it.viewCount / 5) 
        }.take(5).map { 
            ArtRecommendation(it.name, "Discover the beauty of ${it.name}") 
        }
        _recommendations.value = ranked
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.length > 1) {
            _suggestions.value = _artForms.value
                .filter { it.name.contains(newQuery, ignoreCase = true) }
                .map { it.name }
                .take(5)
        } else if (newQuery.isEmpty()) {
            // When query is empty, show trending and recent
            _suggestions.value = (_recentSearches.value + _trendingItems.value).distinct().take(5)
        } else {
            _suggestions.value = emptyList()
        }
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            searchPrefs.saveSearch(query)
        }
    }

    fun toggleLike(artId: String) {
        val updatedList = _artForms.value.map {
            if (it.id == artId) it.copy(isLiked = !it.isLiked) else it
        }
        _artForms.value = updatedList
        updateRecommendations(updatedList)
    }

    fun addViewedArt(name: String) {
        if (!_viewedArts.value.contains(name)) {
            _viewedArts.value = _viewedArts.value + name
            viewModelScope.launch {
                repository.incrementViewCount(name)
            }
        }
    }

    fun loadRecommendations(allArts: List<String>) {
        if (viewedArts.value.isEmpty()) return
        
        viewModelScope.launch {
            geminiRepository.generateRecommendations(
                viewedArts.value,
                allArts
            ).onSuccess { result ->
                _recommendations.value = result
            }
        }
    }

    fun generateAiDescriptionIfNeeded(name: String, currentDescription: String, category: String) {
        if (currentDescription.length >= 50 || _aiDescriptions.value.containsKey(name)) {
            // Even if we don't need a new description, we might still want a legend
            if (!_artLegends.value.containsKey(name)) {
                viewModelScope.launch {
                    geminiRepository.generatePersonalizedLegend(name).onSuccess { legend ->
                        _artLegends.value = _artLegends.value + (name to legend)
                    }
                }
            }
            return
        }

        viewModelScope.launch {
            geminiRepository.generateArtDescription(name, category).onSuccess { aiDesc ->
                _aiDescriptions.value = _aiDescriptions.value + (name to aiDesc)
            }
            
            geminiRepository.generatePersonalizedLegend(name).onSuccess { legend ->
                _artLegends.value = _artLegends.value + (name to legend)
            }
        }
    }
}
