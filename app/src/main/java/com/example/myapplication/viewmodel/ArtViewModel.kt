package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ArtForm
import com.example.myapplication.data.repository.ArtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtViewModel : ViewModel() {

    private val repository = ArtRepository()

    private val _artForms = MutableStateFlow<List<ArtForm>>(emptyList())
    val artForms: StateFlow<List<ArtForm>> = _artForms

    fun fetchArtForms() {
        viewModelScope.launch {
            val result = repository.getArtForms()
            _artForms.value = result
        }
    }
}