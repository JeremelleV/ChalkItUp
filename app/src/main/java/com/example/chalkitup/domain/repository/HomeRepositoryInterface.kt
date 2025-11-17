package com.example.chalkitup.domain.repository

import com.example.chalkitup.domain.model.Appointment

interface HomeRepositoryInterface {
    suspend fun loadProfilePicture(userId: String): Result<String?>
    suspend fun getUserNameAndType(): Result<Pair<String, String>>
    suspend fun fetchBookedDates(): Result<List<String>>
    suspend fun fetchAppointments(): Result<List<Appointment>>
    suspend fun cancelAppointment(appointment: Appointment): Result<Unit>
}