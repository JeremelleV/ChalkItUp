package com.example.chalkitup.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.model.UserProfile
import com.example.chalkitup.domain.repository.ProfileRepositoryInterface
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Handles logic for ProfileScreen
// - fetches user information from firebase and loads it

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepositoryInterface
) : ViewModel() {

    // LiveData to hold and observe the user's profile data
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: MutableLiveData<UserProfile?> get() = _userProfile

    // LiveData to indicate if the user is a tutor
    private val _isTutor = MutableLiveData<Boolean>()
    val isTutor: LiveData<Boolean> get() = _isTutor

    // LiveData to hold and observe the user's profile picture URL
    private val _profilePictureUrl = MutableLiveData<String?>()
    val profilePictureUrl: LiveData<String?> get() = _profilePictureUrl

    private val _totalSessions = MutableStateFlow(0)
    val totalSessions: StateFlow<Int> = _totalSessions

    private val _totalHours = MutableStateFlow(0.0)
    val totalHours: StateFlow<Double> = _totalHours

    private var listener: ListenerRegistration? = null

    fun loadUserProfile(targetedUser: String? = "") {
        val uid = if (targetedUser.isNullOrEmpty()) repo.getCurrentUserId()!! else targetedUser
        viewModelScope.launch {
            repo.fetchUserProfile(uid).fold(
                onSuccess = { p ->
                    _userProfile.value = p
                    p?.let {
                        loadProfilePicture(uid)
                        _isTutor.value = it.userType == "Tutor"
                    }
                }, onFailure = {}
            )
        }
    }

    fun loadProfilePicture(userId: String) {
        viewModelScope.launch {
            repo.loadProfilePicture(userId).fold(
                onSuccess = { _profilePictureUrl.value = it },
                onFailure = { _profilePictureUrl.value = null }
            )
        }
    }

    fun startListeningForPastSessions(userIdInput: String? = "") {
        val uid = if (userIdInput.isNullOrEmpty()) repo.getCurrentUserId()!! else userIdInput
        listener = repo.startListeningForPastSessions(uid) { count, hours ->
            _totalSessions.value = count
            _totalHours.value = hours
            viewModelScope.launch { repo.updateTutorStats(uid, count, hours) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }

    fun reportUser(userId: String, reportMessage: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.reportUser(userId, reportMessage).fold(
                onSuccess = { _: Unit ->
                    onSuccess()
                },
                onFailure = { e ->
                }
            )
        }
    }

}
