package com.teka.weatheragent.presentation.chat_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class WeatherChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class WeatherChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherChatUiState())
    val uiState: StateFlow<WeatherChatUiState> = _uiState.asStateFlow()

    private val baseUrl = "https://aricha.app.n8n.cloud/webhook/92efd652-d13c-4b2e-b5a1-890fd8c4e476"
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun updateCurrentMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }

    fun sendMessage() {
        val currentMessage = _uiState.value.currentMessage.trim()
        if (currentMessage.isBlank() || _uiState.value.isLoading) return

        // Add user message to chat
        val userMessage = ChatMessage(
            content = currentMessage,
            isFromUser = true
        )
        
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            currentMessage = "",
            isLoading = true,
            error = null
        )

        // Send to weather agent
        viewModelScope.launch {
            try {
                val response = sendToWeatherAgent(currentMessage)
                val agentMessage = ChatMessage(
                    content = response,
                    isFromUser = false
                )
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + agentMessage,
                    isLoading = false
                )
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "Sorry, I'm having trouble connecting right now. Please try again later.",
                    isFromUser = false
                )
                
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private suspend fun sendToWeatherAgent(message: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
                val url = "$baseUrl?future=$encodedMessage"
                
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = httpClient.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    throw Exception("Server returned error: ${response.code}")
                }

                val responseBody = response.body?.string() 
                    ?: throw Exception("Empty response from server")

                // Parse JSON response
                val jsonObject = JSONObject(responseBody)
                val weatherResponse = jsonObject.getString("myField")
                
                // Clean up the response (remove \n characters)
                weatherResponse.replace("\\n", "").trim()
                
            } catch (e: Exception) {
                throw Exception("Failed to get weather response: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearChat() {
        _uiState.value = WeatherChatUiState()
    }
}