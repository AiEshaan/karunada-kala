package com.example.myapplication.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.model.MapItem
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Workshop
import com.example.myapplication.data.repository.ArtistRepository
import com.example.myapplication.data.repository.EventRepository
import com.example.myapplication.data.repository.WorkshopRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val artistRepository = ArtistRepository(application)
    private val eventRepository = EventRepository(application)
    private val workshopRepository = WorkshopRepository(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val TAG = "MapViewModel"

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _allMapItems = MutableStateFlow<List<MapItem>>(emptyList())
    
    private val _nearestItem = MutableStateFlow<MapItem?>(null)
    val nearestItem: StateFlow<MapItem?> = _nearestItem

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    val mapItems: StateFlow<List<MapItem>> = combine(
        _allMapItems, _selectedFilter
    ) { items, filter ->
        if (filter == "All") items else items.filter { it.type.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchData() {
        if (_allMapItems.value.isNotEmpty() && !_isLoading.value) return // Avoid redundant fetches if already loaded
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Parallelize Firestore calls
                val artistsDeferred = async { artistRepository.getArtists() }
                val eventsDeferred = async { eventRepository.getEvents() }
                val workshopsDeferred = async { workshopRepository.getWorkshops() }

                val (artistsResult, eventsResult, workshopsResult) = awaitAll(
                    artistsDeferred, eventsDeferred, workshopsDeferred
                )

                // Batch the state updates to avoid multiple rapid emissions
                val combined = withContext(Dispatchers.Default) {
                    val aList = artistsResult.getOrDefault(emptyList()).map { MapItem(it, "Artists", (it as Artist).name) }
                    val eList = eventsResult.getOrDefault(emptyList()).map { MapItem(it, "Events", (it as Event).title) }
                    val wList = workshopsResult.getOrDefault(emptyList()).map { MapItem(it, "Workshops", (it as Workshop).title) }
                    aList + eList + wList
                }

                _allMapItems.value = combined
                
                // Re-calculate nearest after fetching data
                _userLocation.value?.let {
                    calculateNearest(combined, it.latitude, it.longitude)
                } ?: calculateNearest(combined)

            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error fetching map data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getUserLocation(): Location? {
        return try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
            _userLocation.value = location
            location?.let { 
                calculateNearest(_allMapItems.value, it.latitude, it.longitude)
            }
            location
        } catch (e: Exception) {
            Log.w(TAG, "Location unavailable", e)
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun requestUserLocation() {
        viewModelScope.launch {
            getUserLocation()
        }
    }

    private fun calculateNearest(items: List<MapItem>, userLat: Double, userLng: Double) {
        if (items.isEmpty()) return
        
        viewModelScope.launch(Dispatchers.Default) {
            val nearest = items.minByOrNull { item ->
                val results = FloatArray(1)
                Location.distanceBetween(userLat, userLng, item.lat, item.lng, results)
                results[0]
            }
            _nearestItem.value = nearest
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
