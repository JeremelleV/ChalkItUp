package com.example.chalkitup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.model.Appointment
import com.example.chalkitup.domain.repository.HomeRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: HomeRepositoryInterface
): ViewModel() {

    private val _userName = MutableStateFlow<String?>("Unknown")
    val userName: StateFlow<String?> get() = _userName

    private val _userType = MutableStateFlow<String?>("Unknown")
    val userType: StateFlow<String?> get() = _userType

    private val _bookedDates = MutableStateFlow<List<String>>(emptyList())
    val bookedDates: StateFlow<List<String>> get() = _bookedDates

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> get() = _appointments

    private val _profilePic=MutableStateFlow<String?>(null)
    val profilePic:StateFlow<String?> get() =_profilePic

    init{
        loadUserName();loadBookedDates();loadAppointments()
    }

    fun loadProfilePicture(uid:String)=viewModelScope.launch{
        repo.loadProfilePicture(uid).fold(onSuccess={_profilePic.value=it},onFailure={})
    }

    private fun loadUserName()=viewModelScope.launch{
        repo.getUserNameAndType().fold(
            onSuccess={(n,t)->_userName.value=n;_userType.value=t},
            onFailure={}
        )
    }

    private fun loadBookedDates()=viewModelScope.launch{
        repo.fetchBookedDates().fold(onSuccess={_bookedDates.value=it},onFailure={})
    }

    fun loadAppointments()=viewModelScope.launch{
        repo.fetchAppointments().fold(onSuccess={_appointments.value=it},onFailure={})
    }

    fun cancelAppointment(a:Appointment,onComplete:()->Unit)=viewModelScope.launch{
        repo.cancelAppointment(a).fold(onSuccess={
            loadAppointments();loadBookedDates();onComplete()
        },onFailure={})
    }

}
