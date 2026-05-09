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

    private val _artistList = MutableStateFlow<List<MapItem>>(emptyList())
    private val _eventList = MutableStateFlow<List<MapItem>>(emptyList())
    private val _workshopList = MutableStateFlow<List<MapItem>>(emptyList())
    
    private val _nearestItem = MutableStateFlow<MapItem?>(null)
    val nearestItem: StateFlow<MapItem?> = _nearestItem

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    val mapItems: StateFlow<List<MapItem>> = combine(
        _artistList, _eventList, _workshopList, _selectedFilter
    ) { artists, events, workshops, filter ->
        val all = artists + events + workshops
        if (filter == "All") all else all.filter { it.type.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Parallelize Firestore calls
                val artistsDeferred = async { artistRepository.getArtists() }
                val eventsDeferred = async { eventRepository.getEvents() }
                val workshopsDeferred = async { workshopRepository.getWorkshops() }

                val artistsResult = artistsDeferred.await()
                val eventsResult = eventsDeferred.await()
                val workshopsResult = workshopsDeferred.await()

                // Batch the state updates to avoid multiple rapid emissions of mapItems
                withContext(Dispatchers.Default) {
                    val aList: List<MapItem> = artistsResult.getOrDefault(emptyList<Artist>()).map { artist: Artist -> MapItem(artist, "Artists", artist.name) }
                    val eList: List<MapItem> = eventsResult.getOrDefault(emptyList<Event>()).map { event: Event -> MapItem(event, "Events", event.title) }
                    val wList: List<MapItem> = workshopsResult.getOrDefault(emptyList<Workshop>()).map { workshop: Workshop -> MapItem(workshop, "Workshops", workshop.title) }
                    
                    val combined: List<MapItem> = aList + eList + wList

                    withContext(Dispatchers.Main) {
                        _artistList.value = aList
                        _eventList.value = eList
                        _workshopList.value = wList
                        calculateNearest(combined)
                    }
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
                    calculateNearest(_artistList.value + _eventList.value + _workshopList.value, it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Location unavailable, using default", e)
                calculateNearest(_artistList.value + _eventList.value + _workshopList.value)
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
