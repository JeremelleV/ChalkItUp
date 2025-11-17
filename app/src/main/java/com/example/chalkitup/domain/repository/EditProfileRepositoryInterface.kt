package com.example.chalkitup.domain.repository

import android.net.Uri
import com.example.chalkitup.domain.model.UserProfile

interface EditProfileRepositoryInterface {
    fun getCurrentUserId(): String?
    suspend fun loadUserProfile(otherUser: String = ""): Result<UserProfile?>
    suspend fun loadProfilePicture(otherUser: String = ""): Result<String?>
    suspend fun updateProfile(userId: String, updateData: Map<String, Any>): Result<Unit>
    suspend fun uploadProfilePictureTemporarily(userId: String, imageUri: Uri): Result<String>
    suspend fun commitTemporaryProfilePicture(userId: String): Result<String>
    suspend fun deleteTemporaryProfilePicture(userId: String): Result<Unit>
}