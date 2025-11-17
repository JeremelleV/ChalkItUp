package com.example.chalkitup.data.repository

import com.example.chalkitup.domain.repository.SettingsRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SettingsRepositoryInterface {

    override fun getEmail(): String? = auth.currentUser?.email

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    private suspend fun markAccountAsDeleted(userId: String): Result<Unit> = runCatching {
        val updateData = mapOf(
            "firstName" to "deleted user",
            "lastName" to "",
            "active" to false
        )
        firestore.collection("users").document(userId).update(updateData).await()
    }

    private suspend fun deleteAuthUser(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw Exception("No user signed in")
        user.delete().await()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("No user signed in"))
        return markAccountAsDeleted(userId).fold(
            onSuccess = {
                deleteAuthUser()
            },
            onFailure = { Result.failure(it) }
        )
    }

}
