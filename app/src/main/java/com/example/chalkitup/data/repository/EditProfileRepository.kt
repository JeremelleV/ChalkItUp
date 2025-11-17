package com.example.chalkitup.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.chalkitup.domain.model.UserProfile
import com.example.chalkitup.domain.repository.EditProfileRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : EditProfileRepositoryInterface {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun loadUserProfile(otherUser: String): Result<UserProfile?> = runCatching {
        val userId = if (otherUser.isNotEmpty()) otherUser else auth.currentUser?.uid!!
        firestore.collection("users").document(userId).get().await()
            .toObject(UserProfile::class.java)
    }

    override suspend fun loadProfilePicture(otherUser: String): Result<String?> = runCatching {
        val userId = if (otherUser.isNotEmpty()) otherUser else auth.currentUser?.uid!!
        storage.reference.child("$userId/profilePicture.jpg").downloadUrl.await().toString()
    }

    override suspend fun updateProfile(userId: String, updateData: Map<String, Any>): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).update(updateData).await()
    }

    // --- Temporary Profile Picture Functions ---

    override suspend fun uploadProfilePictureTemporarily(userId: String, imageUri: Uri): Result<String> = runCatching {
        val tempRef = storage.reference.child("$userId/temp_profilePicture.jpg")
        tempRef.putFile(imageUri).continueWithTask { it.result?.storage?.downloadUrl!! }.await().toString()
    }

    override suspend fun commitTemporaryProfilePicture(userId: String): Result<String> = runCatching {
        val tempRef = storage.reference.child("$userId/temp_profilePicture.jpg")
        val permRef = storage.reference.child("$userId/profilePicture.jpg")
        val bytes = tempRef.getBytes(Long.MAX_VALUE).await()
        permRef.putBytes(bytes).await()
        tempRef.delete().await()
        permRef.downloadUrl.await().toString()
    }

    override suspend fun deleteTemporaryProfilePicture(userId: String): Result<Unit> = runCatching {
        storage.reference.child("$userId/temp_profilePicture.jpg").delete().await()
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) return it.getString(idx)
        }
        return uri.path!!.substringAfterLast("/")
    }

}
