package com.example.chalkitup.ui.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.repository.AdminRepositoryInterface
import com.example.chalkitup.ui.components.TutorSubject
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AdminHomeViewModel @Inject constructor(
    private val repo: AdminRepositoryInterface
) : ViewModel() {

    val unapprovedTutors: StateFlow<List<User>> =
        repo.getUnapprovedTutors()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val approvedTutors: StateFlow<List<User>> =
        repo.getApprovedTutors()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val reports: StateFlow<List<Report>> =
        repo.getReports()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val usersWithReports: StateFlow<List<User>> =
        repo.getUsersWithReports()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun approveTutor(id: String) = viewModelScope.launch {
        repo.approveTutor(id)
    }

    fun denyTutor(user: User, reason: String, type: String) = viewModelScope.launch {
        repo.denyTutor(user, reason, type)
    }

    fun resolveReport(report: Report) = viewModelScope.launch {
        repo.resolveReport(report.id)
    }

    fun signout() {
        repo.signOut()
    }

    fun loadProfilePictures(forUserIds: List<String>) {
        viewModelScope.launch {
            repo.getProfilePictures(forUserIds)
                .collect { urls ->
                    _profilePictureUrls.value = urls
                }
        }
    }

    private val _profilePictureUrls = MutableStateFlow<Map<String, String?>>(emptyMap())
    val profilePictureUrls: StateFlow<Map<String, String?>> = _profilePictureUrls


}

data class Report(
    var id: String = "",
    val userId: String = "",
    var reportMessage: String = "",
    val timestamp: Timestamp? = null
)


@IgnoreExtraProperties
data class User(
    var id: String = "",
    val userType: String = "",  // Type of user ("Tutor" or "Student")
    var firstName: String = "", // First name of the user
    var lastName: String = "",  // Last name of the user
    val email: String = "",     // Email address of the user
    val subjects: List<TutorSubject> = emptyList(), // List of subjects the user is associated with (for tutors)
    val adminApproved: Boolean
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", "", emptyList(), false)
}