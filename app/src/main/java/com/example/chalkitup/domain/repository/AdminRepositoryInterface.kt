package com.example.chalkitup.domain.repository

import com.example.chalkitup.ui.viewmodel.admin.Report
import com.example.chalkitup.ui.viewmodel.admin.User
import kotlinx.coroutines.flow.Flow

interface AdminRepositoryInterface {
    fun getUnapprovedTutors(): Flow<List<User>>
    fun getApprovedTutors(): Flow<List<User>>
    fun getReports(): Flow<List<Report>>
    fun getUsersWithReports(): Flow<List<User>>
    suspend fun approveTutor(tutorId: String)
    suspend fun denyTutor(tutor: User, reason: String, type: String)
    suspend fun resolveReport(reportId: String)
    fun signOut()
    fun getProfilePictures(userIds: List<String>): Flow<Map<String, String?>>
}