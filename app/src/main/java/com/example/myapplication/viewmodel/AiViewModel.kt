package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {

    private val repository = GeminiRepository()

    private val _response = MutableStateFlow<String?>(null)
    val response: StateFlow<String?> = _response

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun askGemini(prompt: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _response.value = repository.generateText(prompt)
            _isLoading.value = false
        }
    }

    fun clearResponse() {
        _response.value = null
    }
}
