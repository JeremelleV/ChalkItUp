package com.example.chalkitup.ui.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.domain.model.Message
import com.example.chalkitup.domain.repository.ChatRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepositoryInterface,
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    private val _conversationId = MutableStateFlow<String?>(null)
    val conversationId: StateFlow<String?> = _conversationId.asStateFlow()

    private val _messages = MutableStateFlow<Response<List<Message>>>(Response.Loading)
    val messages: StateFlow<Response<List<Message>>> = _messages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var messageListener: Job? = null

    init {
        viewModelScope.launch {
            val currentUserId = chatRepository.fetchCurrentUserId()
            if (currentUserId != null) {
                loadCurrentUser(currentUserId)
            }
        }
    }

    fun loadCurrentUser(userId: String) {
        viewModelScope.launch {
            when (val response  = chatRepository.fetchUser(userId)) {
                is Response.Success -> _currentUser.value = response.data
                is Response.Error -> _error.value = response.message
                else -> {}
            }
        }
    }

    fun loadSelectedUserProfile(userId: String) {
        viewModelScope.launch {
            when (val result = chatRepository.fetchUser(userId)) {
                is Response.Success -> _selectedUser.value = result.data
                is Response.Error -> _error.value = result.message
                else -> {}
            }
        }
    }

    suspend fun sendMessage(
        text: String,
        conversationId: String?
    ): Response<String?> {
        val currentUser = _currentUser.value ?: return Response.Error("Current user not loaded")
        val selectedUser = _selectedUser.value ?: return Response.Error("No recipient selected")
        if (text.isBlank()) return Response.Error("Message cannot be empty")

        return when {
            conversationId == null -> {
                // Create new conversation if doesn't exist
                when (val convoResult = chatRepository.createConversation(currentUser, selectedUser)) {
                    is Response.Success -> {
                        val newConversationId = convoResult.data
                        setupMessageListener(newConversationId)

                        val message = Message(
                            senderId = currentUser.id,
                            text = text,
                            timestamp = System.currentTimeMillis()
                        )
                        when (val messageResult = chatRepository.createMessage(
                            newConversationId,
                            message,
                            currentUser.userType
                        )) {
                            is Response.Success -> { Response.Success(newConversationId) }
                            is Response.Error -> Response.Error(messageResult.message)
                            Response.Loading -> Response.Error("Unexpected loading state")
                        }
                    }
                    is Response.Error -> Response.Error(convoResult.message)
                    Response.Loading -> Response.Error("Unexpected loading state")
                }
            }
            else -> {
                // For existing conversation
                val message = Message(
                    senderId = currentUser.id,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                when (val result = chatRepository.createMessage(
                    conversationId,
                    message,
                    currentUser.userType
                )) {
                    is Response.Success -> Response.Success(null)
                    is Response.Error -> Response.Error(result.message)
                    Response.Loading -> Response.Error("Unexpected loading state")
                }
            }
        }
    }

    fun setupMessageListener(conversationId: String?) {
        messageListener?.cancel()
        _conversationId.value = conversationId

        if (conversationId != null && conversationId.isNotEmpty()) {
            if (_currentUser.value == null) {
                // If current user isn't loaded
                messageListener = viewModelScope.launch {
                    _currentUser

                    _messages.value = Response.Loading
                    chatRepository.fetchMessages(conversationId).collect { response ->
                        _messages.value = response
                    }
                }
            } else {
                // Current user is loaded
                _messages.value = Response.Loading
                messageListener = viewModelScope.launch {
                    chatRepository.fetchMessages(conversationId).collect { response ->
                        _messages.value = response
                    }
                }
            }
        } else {
            _messages.value = Response.Success(emptyList())
        }
    }

}


