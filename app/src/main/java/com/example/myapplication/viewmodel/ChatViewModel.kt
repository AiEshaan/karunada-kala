package com.example.myapplication.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.data.repository.GeminiRepository
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = GeminiRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentContext = MutableStateFlow("")
    private val chatHistory = mutableListOf<com.google.ai.client.generativeai.type.Content>()
    
    private val _ambientInsight = MutableStateFlow<String?>(null)
    val ambientInsight = _ambientInsight.asStateFlow()
    
    private var lastInsightContext = ""
    
    fun setContext(context: String) {
        if (context != _currentContext.value) {
            _currentContext.value = context
            if (context != lastInsightContext) {
                generateAmbientInsight(context)
            }
        }
    }

    private fun generateAmbientInsight(context: String) {
        lastInsightContext = context
        viewModelScope.launch {
            repository.askKala("Give me a one-sentence, fascinating, and culturally deep insight about this: $context. Keep it poetic and short (max 15 words). Start with ✨.", context, emptyList())
                .onSuccess { insight ->
                    _ambientInsight.value = insight
                }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1. Add user message to list and internal history
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(userText, true))
        _messages.value = currentMessages
        
        chatHistory.add(content("user") { text(userText) })

        viewModelScope.launch {
            _isLoading.value = true
            
            // Add a temporary "Kala is thinking" message if you want, or just rely on isLoading
            
            // Request response from Gemini
            repository.askKala(userText, _currentContext.value, chatHistory).onSuccess { reply ->
                // Update message state with response
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(ChatMessage(reply, false))
                _messages.value = updatedMessages
                
                chatHistory.add(content("model") { text(reply) })
            }.onFailure {
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(ChatMessage("Namaskara! I'm experiencing a small technical issue. Please check your internet and try again.", false))
                _messages.value = updatedMessages
            }
            
            _isLoading.value = false
        }
    }

    fun analyzeImage(bitmap: Bitmap) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage("Checking this out...", true)) // Placeholder or separate type
        _messages.value = currentMessages

        viewModelScope.launch {
            _isLoading.value = true
            repository.analyzeHeritageImage(bitmap).onSuccess { reply ->
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(ChatMessage(reply, false))
                _messages.value = updatedMessages
            }.onFailure {
                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(ChatMessage("Namaskara! I'm having trouble seeing the image clearly right now. Please check your connection.", false))
                _messages.value = updatedMessages
            }
            _isLoading.value = false
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        chatHistory.clear()
    }
}
