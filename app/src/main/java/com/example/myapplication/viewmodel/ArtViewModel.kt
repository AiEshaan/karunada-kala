package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ArtForm
import com.example.myapplication.data.repository.ArtRepository
import com.example.myapplication.data.repository.GeminiRepository
import com.example.myapplication.data.repository.SearchPrefsRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.myapplication.ui.state.UiState
import com.example.myapplication.data.local.AppDatabase

class ArtViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ArtRepository(application, database.artDao())
    private val geminiRepository = GeminiRepository()
    private val searchPrefs = SearchPrefsRepository(application)

    private val _artFormsState = MutableStateFlow<UiState<List<ArtForm>>>(UiState.Loading)
    val artFormsState: StateFlow<UiState<List<ArtForm>>> = _artFormsState

    private val _artForms = MutableStateFlow<List<ArtForm>>(emptyList())
    val artForms: StateFlow<List<ArtForm>> = _artForms

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _viewedArts = MutableStateFlow<List<String>>(emptyList())

    private val _aiDescriptions = MutableStateFlow<Map<String, String>>(emptyMap())
    val aiDescriptions = _aiDescriptions.asStateFlow()

    private val _artLegends = MutableStateFlow<Map<String, String>>(emptyMap())
    val artLegends = _artLegends.asStateFlow()

    private val _isGeneratingLegend = MutableStateFlow(false)
    val isGeneratingLegend = _isGeneratingLegend.asStateFlow()

    val artOfTheDay: StateFlow<ArtForm?> = _artForms.map { list ->
        if (list.isEmpty()) return@map null
        val calendar = java.util.Calendar.getInstance()
        val dayOfYear = calendar[java.util.Calendar.DAY_OF_YEAR]
        val year = calendar[java.util.Calendar.YEAR]
        // Deterministic pick based on date
        val index = (dayOfYear + year) % list.size
        list[index]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val legendCache = mutableMapOf<String, String>()

    private var lastArtDocument: DocumentSnapshot? = null
    private val pageSize = 10L
    private var isLastPage = false
    private val trendingItemsList = listOf("Yakshagana", "Channapatna Toys", "Mysore Silk", "Lambani Craft")

    init {
        // 1. Observe Local Cache (Instant Load)
        viewModelScope.launch {
            repository.getLocalArtForms().collect { items ->
                if (items.isNotEmpty()) {
                    _artForms.value = items
                    _artFormsState.value = UiState.Success(items)
                }
            }
        }
        
        // 2. Refresh from Cloud (Zero-Latency Sync)
        viewModelScope.launch {
            _isLoading.value = true
            repository.refreshArtForms()
            _isLoading.value = false
        }

        viewModelScope.launch {
            searchPrefs.recentSearchesFlow.collectLatest { searches ->
                _recentSearches.value = searches
                if (_searchQuery.value.isEmpty()) {
                    _suggestions.value = (searches + trendingItemsList).asSequence().distinct().take(5).toList()
                }
            }
        }
    }

    fun loadMoreArtForms() {
        if (_isLoading.value || isLastPage) return

        viewModelScope.launch {
            _isLoading.value = true
            repository.getArtFormsPaginated(pageSize, lastArtDocument).onSuccess { (items, lastDoc) ->
                if (items.isEmpty() && _artForms.value.isEmpty()) {
                    _artFormsState.value = UiState.Error("No art forms found")
                    isLastPage = true
                } else if (items.isEmpty()) {
                    isLastPage = true
                } else {
                    val newList = _artForms.value + items
                    _artForms.update { newList }
                    _artFormsState.value = UiState.Success(newList)
                    lastArtDocument = lastDoc
                }
            }.onFailure {
                if (_artForms.value.isEmpty()) {
                    _artFormsState.value = UiState.Error("Failed to load art forms")
                }
            }
            _isLoading.value = false
        }
    }

    fun fetchArtForms() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.observeCollection("arts", ArtForm::class.java).collect { forms ->
                _artForms.value = forms
                _artFormsState.value = UiState.Success(forms)
            }
            _isLoading.value = false
        }
    }

    fun generateLegend(artName: String) {
        if (legendCache.containsKey(artName)) {
            _artLegends.update { it + (artName to legendCache[artName]!!) }
            return
        }

        viewModelScope.launch {
            _isGeneratingLegend.value = true
            // Intentional latency to allow the generative UI transition to feel natural
            kotlinx.coroutines.delay(800)
            geminiRepository.generatePersonalizedLegend(artName).onSuccess { legend ->
                legendCache[artName] = legend
                _artLegends.update { it + (artName to legend) }
            }.onFailure {
                val fallback = "This art form represents Karnataka’s rich heritage, passed down through generations as a symbol of our timeless tradition."
                _artLegends.update { it + (artName to fallback) }
            }
            _isGeneratingLegend.value = false
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.length > 2) {
            // Local filter
            val localMatches = _artForms.value
                .filter { it.name.contains(newQuery, ignoreCase = true) }
                .map { it.name }

            if (localMatches.size < 3) {
                // Fetch AI suggestions if few local matches
                viewModelScope.launch {
                    geminiRepository.suggestSearchQueries(newQuery).onSuccess { aiSuggestions ->
                        _suggestions.value = (localMatches + aiSuggestions).asSequence().distinct().take(5).toList()
                    }
                }
            } else {
                _suggestions.value = localMatches.take(5)
            }
        } else if (newQuery.isEmpty()) {
            _suggestions.value = (_recentSearches.value + trendingItemsList).distinct().take(5)
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
        _artForms.update { list ->
            list.map { if (it.id == artId) it.copy(isLiked = !it.isLiked) else it }
        }
    }

    fun addViewedArt(name: String) {
        if (!_viewedArts.value.contains(name)) {
            _viewedArts.update { it + name }
            viewModelScope.launch {
                repository.incrementViewCount(name)
            }
        }
    }

    fun generateAiDescriptionIfNeeded(name: String, currentDescription: String, category: String) {
        if ((currentDescription.length >= 50) || _aiDescriptions.value.containsKey(name)) {
            if (!_artLegends.value.containsKey(name)) {
                viewModelScope.launch {
                    geminiRepository.generatePersonalizedLegend(name).onSuccess { legend ->
                        _artLegends.update { it + (name to legend) }
                    }
                }
            }
            return
        }

        viewModelScope.launch {
            geminiRepository.generateArtDescription(name, category).onSuccess { aiDesc ->
                _aiDescriptions.update { it + (name to aiDesc) }
            }
            
            geminiRepository.generatePersonalizedLegend(name).onSuccess { legend ->
                _artLegends.update { it + (name to legend) }
            }
        }
    }

    fun getRelatedArts(currentArtName: String, category: String): List<ArtForm> {
        return _artForms.value.filter { 
            it.category == category && it.name != currentArtName 
        }.take(5)
    }
}
