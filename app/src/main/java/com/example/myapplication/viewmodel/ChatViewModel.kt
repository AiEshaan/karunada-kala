package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = GeminiRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1. Add user message to list
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(userText, true))
        _messages.value = currentMessages

        viewModelScope.launch {
            _isLoading.value = true
            
            // 2. Get AI reply
            val reply = repository.askKala(userText)

            // 3. Add AI reply to list
            val updatedMessages = _messages.value.toMutableList()
            updatedMessages.add(ChatMessage(reply, false))
            _messages.value = updatedMessages
            
            _isLoading.value = false
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }
}
