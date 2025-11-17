package com.example.chalkitup.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.model.Interest
import com.example.chalkitup.domain.model.ProgressItem
import com.example.chalkitup.domain.model.UserProfile
import com.example.chalkitup.domain.repository.EditProfileRepositoryInterface
import com.example.chalkitup.ui.components.TutorSubject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repo: EditProfileRepositoryInterface
) : ViewModel() {

    // LiveData to hold and observe the user profile data
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> get() = _userProfile

    // LiveData to hold and observe the user's profile picture URL
    private val _profilePictureUrl = MutableLiveData<String?>()
    val profilePictureUrl: LiveData<String?> get() = _profilePictureUrl

    // Temporary variable to store the updated profile picture URL
    private var tempProfilePictureUrl: String? = null // Temporary profile picture
    private var originalProfilePictureUrl: String? = null

    // Initialize and load the user's profile when the ViewModel is created
    init {
        loadUserProfile()
    }

    // Function to load the user's profile data from Firestore
    private fun loadUserProfile(otherUser: String = "") {
        viewModelScope.launch {
            repo.loadUserProfile(otherUser).fold(
                onSuccess = { profile ->
                    _userProfile.value = profile
                    profile?.let {
                        repo.loadProfilePicture(otherUser).fold(
                            onSuccess = { url ->
                                _profilePictureUrl.value = url
                                originalProfilePictureUrl = url
                            }, onFailure = { _profilePictureUrl.value = null }
                        )
                    }
                }, onFailure = { }
            )
        }
    }

    fun uploadProfilePictureTemporarily(uri: Uri) {
        viewModelScope.launch {
            val userId = repo.getCurrentUserId() ?: return@launch
            repo.uploadProfilePictureTemporarily(userId, uri).fold(
                onSuccess = {
                    tempProfilePictureUrl = it
                    _profilePictureUrl.value = it
                }, onFailure = { }
            )
        }
    }

    private fun commitProfilePictureChange() {
        viewModelScope.launch {
            val userId = repo.getCurrentUserId() ?: return@launch
            tempProfilePictureUrl?.let {
                repo.commitTemporaryProfilePicture(userId).fold(
                    onSuccess = {
                        _profilePictureUrl.value = it
                        originalProfilePictureUrl = it
                        tempProfilePictureUrl = null
                    }, onFailure = { }
                )
            }
        }
    }

    fun cancelProfilePictureChange() {
        viewModelScope.launch {
            val userId = repo.getCurrentUserId() ?: return@launch
            tempProfilePictureUrl?.let {
                repo.deleteTemporaryProfilePicture(userId)
            }
            _profilePictureUrl.value = originalProfilePictureUrl
            tempProfilePictureUrl = null
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        subjects: List<TutorSubject>,
        bio: String,
        startingPrice: String,
        experience: String,
        progress: List<ProgressItem>,
        interests: SnapshotStateList<Interest>
    ) {
        viewModelScope.launch {
            val userId = repo.getCurrentUserId() ?: return@launch
            tempProfilePictureUrl?.let { commitProfilePictureChange() }
            val data = mutableMapOf<String, Any>(
                "firstName" to firstName,
                "lastName" to lastName,
                "bio" to bio,
                "startingPrice" to startingPrice,
                "experience" to experience,
                "progress" to progress,
                "interests" to interests
            )
            _userProfile.value?.takeIf { it.userType=="Tutor" }?.let {
                data["subjects"] = subjects
            }
            _profilePictureUrl.value?.let { data["profilePictureUrl"] = it }
            repo.updateProfile(userId, data)
        }
    }

}
