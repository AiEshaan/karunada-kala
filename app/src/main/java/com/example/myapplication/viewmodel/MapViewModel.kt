package com.example.myapplication.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.model.MapItem
import com.example.myapplication.data.repository.ArtRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ArtRepository()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val TAG = "MapViewModel"

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _artists = MutableStateFlow<List<MapItem>>(emptyList())
    private val _events = MutableStateFlow<List<MapItem>>(emptyList())
    private val _workshops = MutableStateFlow<List<MapItem>>(emptyList())
    
    private val _nearestItem = MutableStateFlow<MapItem?>(null)
    val nearestItem: StateFlow<MapItem?> = _nearestItem

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    val mapItems: StateFlow<List<MapItem>> = combine(
        _artists, _events, _workshops, _selectedFilter
    ) { artists, events, workshops, filter ->
        val all = artists + events + workshops
        if (filter == "All") all else all.filter { it.type.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Parallelize Firestore calls to prevent blocking the UI thread sequentially
                val artistsDeferred = async { repository.getArtists() }
                val eventsDeferred = async { repository.getEvents() }
                val workshopsDeferred = async { repository.getWorkshops() }

                val artistsResult = artistsDeferred.await()
                val eventsResult = eventsDeferred.await()
                val workshopsResult = workshopsDeferred.await()

                // Perform transformations and distance calculations off the main thread
                withContext(Dispatchers.Default) {
                    artistsResult.onSuccess { artistList ->
                        _artists.value = artistList.map { MapItem(it, "Artists", it.name) }
                    }
                    eventsResult.onSuccess { eventList ->
                        _events.value = eventList.map { MapItem(it, "Events", it.title) }
                    }
                    workshopsResult.onSuccess { workshopList ->
                        _workshops.value = workshopList.map { MapItem(it, "Workshops", it.title) }
                    }

                    calculateNearest(_artists.value + _events.value + _workshops.value)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error fetching map data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestUserLocation() {
        viewModelScope.launch {
            try {
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                _userLocation.value = location
                location?.let { 
                    calculateNearest(_artists.value + _events.value + _workshops.value, it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Location unavailable, using default", e)
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
        // Fallback to Bangalore if user location not yet fetched
        calculateNearest(items, 12.9716, 77.5946)
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }
}
