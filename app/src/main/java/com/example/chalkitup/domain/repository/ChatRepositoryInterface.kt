package com.example.chalkitup.domain.repository

import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Message
import com.example.chalkitup.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ChatRepositoryInterface {
    suspend fun fetchCurrentUserId(): String?
    suspend fun fetchUser(userId: String): Response<User>
    suspend fun fetchMessages(conversationId: String): Flow<Response<List<Message>>>
    suspend fun createConversation(
        currentUser: User,
        selectedUser: User
    ): Response<String>
    suspend fun createMessage(
        conversationId: String,
        message: Message,
        userType: String
    ): Response<Unit>
}