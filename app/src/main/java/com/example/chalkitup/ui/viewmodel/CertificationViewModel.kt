package com.example.chalkitup.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.model.Certification
import com.example.chalkitup.domain.repository.CertificationRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLConnection
import javax.inject.Inject

@HiltViewModel
class CertificationViewModel @Inject constructor(
    private val certificationRepository: CertificationRepositoryInterface
) : ViewModel() {

    // StateFlow to hold the list of certifications fetched from Firebase Storage
    private val _certifications = MutableStateFlow<List<Certification>>(emptyList())
    val certifications: StateFlow<List<Certification>> = _certifications

    // StateFlow to hold the list of selected files for upload
    private val _selectedFiles = MutableStateFlow<List<Uri>>(emptyList()) // For managing selected files
    val selectedFiles: StateFlow<List<Uri>> = _selectedFiles

    // LiveData to hold the URI of a file selected for viewing or download
    private val _fileUri = MutableLiveData<Uri?>()
    val fileUri: LiveData<Uri?> = _fileUri

    // Function to reset the file URI, called after the file is opened or downloaded
    fun resetFileUri() {
        _fileUri.value = null
    }

    fun getCertifications(otherUser: String = "") {
        viewModelScope.launch {
            certificationRepository.fetchCertifications(otherUser).fold(
                onSuccess = { (list, uris) ->
                    _certifications.value = list
                    _selectedFiles.value = uris
                },
                onFailure = { }
            )
        }
    }

    fun uploadFiles(userId: String) {
        viewModelScope.launch {
            certificationRepository.uploadFiles(userId, _selectedFiles.value).fold(
                onSuccess = { getCertifications(); _selectedFiles.value = emptyList() },
                onFailure = { }
            )
        }
    }

    fun updateCertifications() {
        viewModelScope.launch {
            val userId = certificationRepository.getCurrentUserId() ?: return@launch
            certificationRepository.updateCertifications(userId, _selectedFiles.value).fold(
                onSuccess = { getCertifications(); _selectedFiles.value = emptyList() },
                onFailure = { }
            )
        }
    }

    fun addSelectedFiles(uris: List<Uri>) {
        _selectedFiles.value += uris
    }

    fun removeSelectedFile(uri: Uri) {
        _selectedFiles.value -= uri
    }

    fun downloadFileToCache(fileName: String, otherUser: String = "") {
        viewModelScope.launch {
            certificationRepository.downloadFileToCache(
                otherUser.ifEmpty { certificationRepository.getCurrentUserId()!! }, fileName
            ).fold(
                onSuccess = { _fileUri.postValue(it) },
                onFailure = { _fileUri.postValue(null) }
            )
        }
    }

    fun openFile(context: Context, uri: Uri) {
        val mime = URLConnection.guessContentTypeFromName(File(uri.path!!).name) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (intent.resolveActivity(context.packageManager) != null)
            context.startActivity(intent)
        else Toast.makeText(context, "No app to open this file type", Toast.LENGTH_SHORT).show()
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        // Query the content resolver to get the display name of the file
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)  // Get the column index for the display name
            it.moveToFirst()  // Move the cursor to the first row (there should be only one)
            return it.getString(nameIndex)  // Return the file name
        }
        // Fallback in case the URI doesn't contain the expected metadata
        return uri.path?.substringAfterLast("/") ?: "Unknown File"
    }
}
