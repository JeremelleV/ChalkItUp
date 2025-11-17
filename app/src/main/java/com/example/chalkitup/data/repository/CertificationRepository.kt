package com.example.chalkitup.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.example.chalkitup.domain.model.Certification
import com.example.chalkitup.domain.repository.CertificationRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificationRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : CertificationRepositoryInterface {

    override suspend fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun fetchCertifications(otherUser: String): Result<Pair<List<Certification>, List<Uri>>> = runCatching {
        var userId = auth.currentUser?.uid ?: throw Exception("No user signed in")
        if (otherUser.isNotEmpty()) userId = otherUser
        val ref = storage.reference.child("$userId/certifications")
        val listResult = ref.listAll().await()
        if (listResult.items.isEmpty()) return Result.success(emptyList<Certification>() to emptyList<Uri>())
        val certs = mutableListOf<Certification>()
        val uris = mutableListOf<Uri>()
        for (fileRef in listResult.items) {
            val uri = fileRef.downloadUrl.await()
            certs += Certification(fileRef.name, uri.toString())
            uris += uri
        }
        certs to uris
    }


    override suspend fun uploadFiles(userId: String, files: List<Uri>): Result<Unit> = runCatching {
        val ref = storage.reference.child("$userId/certifications")
        for (uri in files) {
            val name = getFileNameFromUri(uri)
            ref.child(name).putFile(uri).await()
        }
    }

    override suspend fun updateCertifications(userId: String, files: List<Uri>): Result<Unit> = runCatching {
        val ref = storage.reference.child("$userId/certifications")
        val listResult = ref.listAll().await()
        val existing = listResult.items.map { it.name }.toSet()
        val selected = files.map { getFileNameFromUri(it) }.toSet()
        (existing - selected).forEach { ref.child(it).delete().await() }
        for (uri in files) {
            val name = getFileNameFromUri(uri)
            if (name !in existing) ref.child(name).putFile(uri).await()
        }
    }

    override suspend fun downloadFileToCache(userId: String, fileName: String): Result<Uri> = runCatching {
        println("Downloading file $fileName for user $userId")
        val ref = storage.reference.child("$userId/certifications/$fileName")
        val local = File(context.cacheDir, fileName)
        ref.getFile(local).await()
        FileProvider.getUriForFile(context, "com.example.chalkitup.fileprovider", local)
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) return it.getString(idx)
        }
        return uri.path?.substringAfterLast("/") ?: "UnknownFile"
    }

}