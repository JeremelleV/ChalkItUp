package com.example.chalkitup.domain.repository

import com.example.chalkitup.domain.model.TutorAvailabilityWrapper
import com.google.firebase.firestore.ListenerRegistration

interface TutorAvailabilityRepositoryInterface {
    fun getCurrentUserId(): String?
    suspend fun initializeSessionCount(tutorId: String, monthYear: String): Result<Unit>
    fun observeAvailability(
        tutorId: String,
        monthYear: String,
        onAvailabilityUpdate: (TutorAvailabilityWrapper?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration
    suspend fun saveAvailability(
        tutorId: String,
        monthYear: String,
        availabilityWrapper: TutorAvailabilityWrapper
    ): Result<Unit>
}