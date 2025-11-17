package com.example.chalkitup.domain.repository

import android.net.Uri
import com.example.chalkitup.domain.model.Certification

interface CertificationRepositoryInterface {
    suspend fun getCurrentUserId(): String?
    suspend fun fetchCertifications(otherUser: String = ""): Result<Pair<List<Certification>, List<Uri>>>
    suspend fun uploadFiles(userId: String, files: List<Uri>): Result<Unit>
    suspend fun updateCertifications(userId: String, files: List<Uri>): Result<Unit>
    suspend fun downloadFileToCache(userId: String, fileName: String): Result<Uri>
}