package com.example.chalkitup.domain.repository

import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Conversation
import com.example.chalkitup.domain.model.User
import kotlinx.coroutines.flow.Flow

interface MessageListRepositoryInterface {
    suspend fun getUserIdAndType(): Result<Pair<String, String>>
    suspend fun fetchConversations(): Flow<Response<List<Conversation>>>
    suspend fun fetchUsers(): Flow<Response<List<User>>>
    suspend fun fetchProfilePictures(userIds: List<String>): Map<String, String?>
    suspend fun updateConversationStatus(conversationId: String, userType: String): Response<Unit>
    suspend fun fetchConversationId(selectedUserId: String, currentUserId: String): Response<String?>
}