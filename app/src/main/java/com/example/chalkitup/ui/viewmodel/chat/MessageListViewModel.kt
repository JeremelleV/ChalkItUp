package com.example.chalkitup.ui.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Conversation
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.domain.repository.MessageListRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.fold


@HiltViewModel
class MessageListViewModel @Inject constructor(
    private val messageListRepository: MessageListRepositoryInterface
) : ViewModel() {

    val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserType = MutableStateFlow<String?>(null)
    val currentUserType: StateFlow<String?> = _currentUserType.asStateFlow()

    private val _conversationId = MutableStateFlow<String?>(null)
    val conversationId: StateFlow<String?> = _conversationId.asStateFlow()

    val _users = MutableStateFlow<Response<List<User>>>(Response.Loading)
    val users: StateFlow<Response<List<User>>> = _users.asStateFlow()

    val _conversations = MutableStateFlow<Response<List<Conversation>>>(Response.Loading)
    val conversations: StateFlow<Response<List<Conversation>>> = _conversations.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val dataReady = combine(_conversations, _users) { conversations, users ->
        when {
            conversations is Response.Success && users is Response.Success ->
                Response.Success(Pair(conversations.data, users.data))
            conversations is Response.Error -> conversations
            users is Response.Error -> users
            else -> Response.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, Response.Loading)

    init {
        viewModelScope.launch {
            messageListRepository.getUserIdAndType().fold(
                onSuccess= { (id, t) ->
                    _currentUserId.value = id
                    _currentUserType.value = t
                },
                onFailure={}
            )
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            messageListRepository.fetchConversations().collect { response ->
                when (response) {
                    is Response.Success -> { _conversations.value = response }
                    is Response.Error -> _error.value = response.message
                    Response.Loading -> _conversations.value = Response.Loading
                }
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            messageListRepository.fetchUsers().collect { response ->
                when (response) {
                    is Response.Success -> {
                        _users.value = response
                    }
                    is Response.Error -> _error.value = response.message
                    Response.Loading -> _users.value = Response.Loading
                }
            }
        }
    }

    fun getUserInfo(conversation: Conversation): User? {
        val currentUserId = _currentUserId.value ?: return null
        val otherUserId = if (conversation.studentId == currentUserId)
            conversation.tutorId else conversation.studentId
        return when (val usersResponse = _users.value) {
            is Response.Success -> usersResponse.data.find { it.id == otherUserId }
            else -> null
        }
    }

    suspend fun fetchConversationId(selectedUserId: String): String? {
        val currentUserId = _currentUserId.value ?: return null
        val convoResult = messageListRepository.fetchConversationId(selectedUserId, currentUserId)
        return when (convoResult) {
            is Response.Success -> {
                _conversationId.value = convoResult.data
                convoResult.data
            }
            is Response.Error -> {
                _error.value = convoResult.message
                null
            }
            else -> null
        }
    }


    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            val userType = _currentUserType.value ?: return@launch
            messageListRepository.updateConversationStatus(conversationId, userType)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredUsers(): StateFlow<List<User>> {
        return combine(users, searchQuery) { usersResponse, query ->
            when (usersResponse) {
                is Response.Success -> {
                    usersResponse.data.filter { user ->
                        user.firstName.contains(query.trim(), ignoreCase = true) ||
                                user.lastName.contains(query, ignoreCase = true)
                    }.sortedWith(compareBy(
                        { it.firstName.lowercase() },
                        { it.lastName.lowercase() }
                    ))
                }
                else -> emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun getFilteredConversations(): StateFlow<List<Conversation>> {
        return combine(_conversations, _searchQuery, _currentUserId) { convos, query, currentUser ->
            when (convos) {
                is Response.Success -> {
                    convos.data.filter { conversation ->
                        val matchName = if (conversation.studentId == currentUser) {
                            conversation.tutorName
                        } else {
                            conversation.studentName
                        }
                        matchName.lowercase().contains(query.trim(), ignoreCase = true)
                    }.sortedByDescending { it.timestamp }
                }
                else -> emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun isConversationUnread(conversation: Conversation, currentUserType: String?): Boolean {
        return when (currentUserType) {
            "Student" -> !conversation.lastMessageReadByStudent
            "Tutor" -> !conversation.lastMessageReadByTutor
            else -> false
        }
    }

}

