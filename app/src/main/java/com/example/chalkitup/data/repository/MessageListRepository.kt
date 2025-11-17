package com.example.chalkitup.data.repository

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Conversation
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.domain.repository.MessageListRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageListRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : MessageListRepositoryInterface {

    private var cachedUsers: List<User>? = null

    override suspend fun getUserIdAndType(): Result<Pair<String, String>> = runCatching {
        val userId = auth.currentUser?.uid ?: throw Exception("No user")
        val doc = firestore.collection("users").document(userId).get().await()
        val type = doc.getString("userType") ?: "Unknown"
        userId to type
    }

    override suspend fun fetchConversations(): Flow<Response<List<Conversation>>> = flow {
        emit(Response.Loading)
        val currentUserId = auth.currentUser?.uid ?: throw Exception("No user")
        val doc = firestore.collection("users").document(currentUserId).get().await()
        val userType = doc.getString("userType") ?: "Unknown"

        try {
            val snapshot = when (userType) {
                "Student" -> firestore.collection("conversations")
                    .whereEqualTo("studentId", currentUserId)
                    .get().await()
                "Tutor" -> firestore.collection("conversations")
                    .whereEqualTo("tutorId", currentUserId)
                    .get().await()
                else -> throw IllegalStateException("Invalid user type: $userType")
            }

            val convos = snapshot.documents.mapNotNull { document ->
                Conversation(
                    id = document.id,
                    studentId = document.getString("studentId") ?: "",
                    tutorId = document.getString("tutorId") ?: "",
                    studentName = document.getString("studentName") ?: "",
                    tutorName = document.getString("tutorName") ?: "",
                    lastMessage = document.getString("lastMessage") ?: "",
                    timestamp = document.getLong("timestamp") ?: 0,
                    lastMessageReadByStudent = document.getBoolean("lastMessageReadByStudent") ?: true,
                    lastMessageReadByTutor = document.getBoolean("lastMessageReadByTutor") ?: true
                )
            }
            emit(Response.Success(convos))

        } catch (e: Exception) {
            emit(Response.Error("Failed to fetch conversations: ${e.message}"))
        }
    }

    override suspend fun fetchUsers(): Flow<Response<List<User>>> = flow {
        cachedUsers?.let {
            emit(Response.Success(it))
            return@flow
        }

        emit(Response.Loading)
        val currentUserId = auth.currentUser?.uid ?: throw Exception("No user")
        val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
        val currentType = currentUserDoc.getString("userType") ?: ""
        val oppositeType = if (currentType == "Student") "Tutor" else "Student"

        try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("userType", oppositeType)
                .get()
                .await()

            val usersList = querySnapshot.map { document ->
                User(
                    id = document.id,
                    firstName = document.getString("firstName") ?: "",
                    lastName = document.getString("lastName") ?: "",
                    userType = document.getString("userType") ?: "",
                    userProfilePictureUrl = null
                )
            }
            val userIds = usersList.map { it.id }
            val profileUrls = fetchProfilePictures(userIds)

            val updatedUsersList = usersList.map { user ->
                user.copy(userProfilePictureUrl = profileUrls[user.id])
            }

            cachedUsers = updatedUsersList
            emit(Response.Success(updatedUsersList))

        } catch (e: Exception) {
            emit(Response.Error("Failed to fetch conversations: ${e.message}"))

        }
    }

    override suspend fun fetchProfilePictures(userIds: List<String>): Map<String, String?> {
        val results = mutableMapOf<String, String?>()

        userIds.forEach { userId ->
            val storageRef = storage.reference.child("$userId/profilePicture.jpg")
            try {
                storageRef.metadata.await()
                val url = storageRef.downloadUrl.await().toString()
                results[userId] = url

            } catch (e: StorageException) {
                when (e.errorCode) {
                    StorageException.ERROR_OBJECT_NOT_FOUND -> {
                        // No profile picture exists for this user
                        results[userId] = null
                    }
                    else -> {
                        // Other storage errors
                        results[userId] = null
                        Log.w("Profile Pictures", "Error fetching profile for $userId: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                // Other unexpected errors
                results[userId] = null
                Log.e("Profile Pictures", "Unexpected error for $userId: ${e.message}")
            }
        }
        return results
    }

    override suspend fun updateConversationStatus(
        conversationId: String,
        userType: String
    ): Response<Unit> {
        return try {
            val field = when (userType) {
                "Student" -> "lastMessageReadByStudent"
                "Tutor" -> "lastMessageReadByTutor"
                else -> return Response.Error("Invalid user type")
            }
            firestore.collection("conversations")
                .document(conversationId)
                .update(field, true)
                .await()
            Response.Success(Unit)

        } catch (e: Exception) {
            Response.Error("Failed to update message status: ${e.message}")
        }
    }

    override suspend fun fetchConversationId(
        selectedUserId: String,
        currentUserId: String
    ): Response<String?> = try {
        val query1 = firestore.collection("conversations")
            .whereEqualTo("studentId", currentUserId)
            .whereEqualTo("tutorId", selectedUserId)
            .get().await()
        val query2 = firestore.collection("conversations")
            .whereEqualTo("tutorId", currentUserId)
            .whereEqualTo("studentId", selectedUserId)
            .get().await()

        val allResults = query1.documents + query2.documents
        Response.Success(allResults.firstOrNull()?.id)

    } catch (e: Exception) {
        Response.Error("Failed to fetch conversation: ${e.message}")
    }

    @VisibleForTesting
    fun clearCache() {
        cachedUsers = null
    }
}
