package com.example.chalkitup.data.repository

import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Message
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.domain.repository.ChatRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : ChatRepositoryInterface {

    override suspend fun fetchCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun fetchUser(userId: String): Response<User> {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val storageRef = storage.reference.child("$userId/profilePicture.jpg")
            val profileUrl = try {
                storageRef.metadata.await()
                storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                ""
            }

            val user = User(
                id = userDoc.id,
                firstName = userDoc.getString("firstName") ?: "",
                lastName = userDoc.getString("lastName") ?: "",
                userType = userDoc.getString("userType") ?: "",
                userProfilePictureUrl = profileUrl
            )
            Response.Success(user)

        } catch (e: Exception) {
            Response.Error("Failed to fetch user: ${e.message}")
        }
    }

    override suspend fun fetchMessages(conversationId: String): Flow<Response<List<Message>>> = callbackFlow {
        val messageListener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error("Error fetching messages: ${error.message}")).isSuccess
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(Message::class.java)
                } ?: emptyList()

                trySend(Response.Success(messages)).isSuccess
            }
        awaitClose{ messageListener.remove() }
    }

    override suspend fun createConversation(
        currentUser: User,
        selectedUser: User
    ): Response<String> {
        return try {
            val (studentId, studentName, tutorId, tutorName) = when (currentUser.userType) {
                "Student" -> listOf(
                    currentUser.id,
                    "${currentUser.firstName} ${currentUser.lastName}",
                    selectedUser.id,
                    "${selectedUser.firstName} ${selectedUser.lastName}"
                )
                "Tutor" -> listOf(
                    selectedUser.id,
                    "${selectedUser.firstName} ${selectedUser.lastName}",
                    currentUser.id,
                    "${currentUser.firstName} ${currentUser.lastName}"
                )
                else -> return Response.Error("Invalid user type")
            }
            val docRef = firestore.collection("conversations").add(
                mapOf(
                    "studentId" to studentId,
                    "tutorId" to tutorId,
                    "studentName" to studentName,
                    "tutorName" to tutorName,
                    "lastMessage" to "",
                    "timestamp" to System.currentTimeMillis(),
                    "lastMessageReadByStudent" to false,
                    "lastMessageReadByTutor" to false
                )
            ).await()
            Response.Success(docRef.id)

        } catch (e: Exception) {
            Response.Error("Failed to create conversation: ${e.message}")
        }
    }

    override suspend fun createMessage(
        conversationId: String,
        message: Message,
        userType: String
    ): Response<Unit> {
        return try {
            firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .add(message)
                .await()

            val updates = mutableMapOf<String, Any>(
                "lastMessage" to message.text,
                "timestamp" to message.timestamp
            )
            when (userType) {
                "Student" -> {
                    updates["lastMessageReadByStudent"] = true
                    updates["lastMessageReadByTutor"] = false
                }
                "Tutor" -> {
                    updates["lastMessageReadByTutor"] = true
                    updates["lastMessageReadByStudent"] = false
                }
            }
            firestore.collection("conversations")
                .document(conversationId)
                .update(updates)
                .await()

            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error("Failed to send message: ${e.message}")
        }
    }


}

