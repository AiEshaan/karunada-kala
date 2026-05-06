package com.example.myapplication.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.model.MapItem
import com.example.myapplication.data.repository.ArtRepository
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Workshop
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

import com.example.myapplication.ui.state.UiState
import com.example.myapplication.core.utils.NetworkUtils

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ArtRepository(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val tag = "MapViewModel"

    private val _uiState = MutableStateFlow<UiState<List<MapItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<MapItem>>> = _uiState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _showOnlyToday = MutableStateFlow(false)
    val showOnlyToday: StateFlow<Boolean> = _showOnlyToday

    private val _artists = MutableStateFlow<List<MapItem>>(emptyList())
    private val _events = MutableStateFlow<List<MapItem>>(emptyList())
    private val _workshops = MutableStateFlow<List<MapItem>>(emptyList())
    
    private val _nearestItem = MutableStateFlow<MapItem?>(null)
    val nearestItem: StateFlow<MapItem?> = _nearestItem

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    private val _discoveryAlert = MutableStateFlow<String?>(null)
    val discoveryAlert: StateFlow<String?> = _discoveryAlert

    val mapItems: StateFlow<List<MapItem>> = combine(
        _artists,
        _events,
        _workshops,
        _selectedFilter,
        _showOnlyToday,
    ) { artists, events, workshops, filter, showToday ->
        var all = artists + events + workshops
        
        if (showToday) {
            val calendar = java.util.Calendar.getInstance()
            val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            
            val validDates = mutableListOf<String>()
            for (i in 0..7) {
                validDates.add(dateFormat.format(calendar.time))
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }

            all = all.filter { item ->
                item.type == "Artists" || 
                ((item.data as? Event)?.date?.let { dateStr -> validDates.any { dateStr.contains(it, ignoreCase = true) } } == true) ||
                ((item.data as? Workshop)?.date?.let { dateStr -> validDates.any { dateStr.contains(it, ignoreCase = true) } } == true)
            }
        }
        
        if (filter == "All") all else all.filter { it.type.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nowActiveCount: StateFlow<Int> = combine(
        _artists,
        _events,
        _workshops
    ) { artists, events, workshops ->
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        
        val validDates = mutableListOf<String>()
        for (i in 0..7) {
            validDates.add(dateFormat.format(calendar.time))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        val activeEvents = events.count { item ->
            (item.data as? Event)?.date?.let { dateStr -> validDates.any { dateStr.contains(it, ignoreCase = true) } } == true
        }
        val activeWorkshops = workshops.count { item ->
            (item.data as? Workshop)?.date?.let { dateStr -> validDates.any { dateStr.contains(it, ignoreCase = true) } } == true
        }
        
        activeEvents + activeWorkshops
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun fetchData() {
        _isLoading.value = true
        _uiState.value = UiState.Loading
        
        // Observe Artists
        repository.observeCollection("artists", Artist::class.java)
            .onEach { artistList ->
                _artists.value = artistList.map { MapItem(it, "Artists", it.name) }
                updateDiscoveryAlert()
                calculateNearestAndStopLoading()
                updateUiState()
            }
            .catch { e ->
                _uiState.value = UiState.Error("Failed to load map data: ${e.message}")
            }
            .launchIn(viewModelScope)

        // Observe Events
        repository.observeCollection("events", com.example.myapplication.data.model.Event::class.java)
            .onEach { eventList ->
                _events.value = eventList.map { MapItem(it, "Events", it.title) }
                updateDiscoveryAlert()
                calculateNearestAndStopLoading()
                updateUiState()
            }
            .catch { e ->
                _uiState.value = UiState.Error("Failed to load map data: ${e.message}")
            }
            .launchIn(viewModelScope)

        // Observe Workshops
        repository.observeCollection("workshops", Workshop::class.java)
            .onEach { workshopList ->
                _workshops.value = workshopList.map { MapItem(it, "Workshops", it.title) }
                updateDiscoveryAlert()
                calculateNearestAndStopLoading()
                updateUiState()
            }
            .catch { e ->
                _uiState.value = UiState.Error("Failed to load map data: ${e.message}")
            }
            .launchIn(viewModelScope)
    }

    private fun updateUiState() {
        val allItems = _artists.value + _events.value + _workshops.value
        if (allItems.isNotEmpty()) {
            _uiState.value = UiState.Success(allItems)
        } else if (!_isLoading.value) {
            _uiState.value = UiState.Error("No items found on the map.")
        }
    }

    private fun updateDiscoveryAlert() {
        val location = _userLocation.value ?: return
        val allItems = _artists.value + _events.value + _workshops.value
        
        val nearby = allItems.find { item ->
            val results = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, item.lat, item.lng, results)
            results[0] < 5000 // 5km radius
        }
        
        _discoveryAlert.value = nearby?.let { 
            "✨ You are near ${it.itemTitle} (${it.type})!"
        }
    }

    private fun calculateNearestAndStopLoading() {
        val allItems = _artists.value + _events.value + _workshops.value
        _userLocation.value?.let {
            calculateNearest(allItems, it.latitude, it.longitude)
        } ?: calculateNearest(allItems)
        _isLoading.value = false
    }

    @SuppressLint("MissingPermission")
    fun requestUserLocation() {
        viewModelScope.launch {
            try {
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                _userLocation.value = location
                location?.let { 
                    updateDiscoveryAlert()
                    calculateNearest(_artists.value + _events.value + _workshops.value, it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                Log.w(tag, "Location unavailable, using default", e)
                calculateNearest(_artists.value + _events.value + _workshops.value)
            }
        }
    }

    private fun calculateNearest(items: List<MapItem>, userLat: Double, userLng: Double) {
        if (items.isEmpty()) return
        
        _nearestItem.value = items.minByOrNull { item ->
            val results = FloatArray(1)
            Location.distanceBetween(userLat, userLng, item.lat, item.lng, results)
            results[0]
        }
    }

    private fun calculateNearest(items: List<MapItem>) {
        calculateNearest(items, 12.9716, 77.5946)
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        if (filter != "Now") _showOnlyToday.value = false
    }

    fun toggleNowFilter() {
        _showOnlyToday.update { !it }
        if (_showOnlyToday.value) _selectedFilter.value = "All"
    }
}
