package com.example.chalkitup.domain.repository

import com.example.chalkitup.domain.model.UserProfile
import com.google.firebase.firestore.ListenerRegistration

interface ProfileRepositoryInterface {
    fun getCurrentUserId(): String?
    suspend fun fetchUserProfile(userId: String): Result<UserProfile?>
    suspend fun loadProfilePicture(userId: String): Result<String?>
    suspend fun reportUser(userId: String, reportMessage: String): Result<Unit>
    fun startListeningForPastSessions(
        userId: String,
        onStatsUpdate: (sessionCount: Int, totalHours: Double) -> Unit
    ): ListenerRegistration
    suspend fun updateTutorStats(userId: String, sessionCount: Int, totalHours: Double): Result<Unit>
}