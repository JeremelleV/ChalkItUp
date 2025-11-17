package com.example.chalkitup.domain.repository

import com.example.chalkitup.domain.model.UserInfo
import com.example.chalkitup.ui.components.TutorSubject
import java.time.LocalDate
import java.time.LocalTime

interface BookingRepositoryInterface {
    fun getCurrentUserId(): String?
    suspend fun fetchUserInfo(userId: String): Result<UserInfo>
    suspend fun fetchUserActiveStatus(): Result<Boolean>
    suspend fun fetchUserFullName(userId: String): Result<String>
    suspend fun sendEmail(
        tutorID: String,
        fName: String,
        tutorName: String,
        subject: TutorSubject,
        date: String,
        timeSlot: String,
        price: String,
        userEmail: String
    ): Result<Unit>
    suspend fun updateSessionCount(
        tutorId: String,
        yearMonth: String,
        weekNumber: Int
    ): Result<Unit>
    suspend fun markTimesAsBooked(
        tutorId: String,
        yearMonth: String,
        day: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Result<Unit>
    suspend fun addSession(
        tutorId: String,
        comments: String,
        sessionType: String,
        day: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        subject: TutorSubject,
        studentId: String,
        tutorFullName: String,
        studentFullName: String
    ): Result<Unit>
    suspend fun fetchTutors(
        selectedSubject: TutorSubject,
        priceRange: ClosedFloatingPointRange<Float>
    ): Result<List<String>>
    suspend fun fetchAvailabilityForTutors(
        tutorIds: List<String>,
        isCurrentMonth: Boolean,
        mode: String
    ): Result<Pair<Map<LocalDate, List<LocalTime>>, Map<String, Map<LocalDate, List<LocalTime>>>>>
    suspend fun fetchTutorPriceForSubject(
        tutorId: String,
        selectedSubject: TutorSubject
    ): Result<String>
    suspend fun getSessionCountForWeek(
        tutorId: String,
        yearMonth: String,
        weekNumber: Int
    ): Result<Int>
}
