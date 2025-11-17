package com.example.chalkitup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.model.TimeSlot
import com.example.chalkitup.domain.model.TutorAvailability
import com.example.chalkitup.domain.model.TutorAvailabilityWrapper
import com.example.chalkitup.domain.repository.TutorAvailabilityRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for managing tutor availability.
 *
 * This ViewModel handles the selection, storage, and retrieval of tutor
 * availability data. It allows tutors to select available time slots for specific
 * days, toggle selections, and save or remove availability data in Firestore.
 * Availability data is automatically fetched from Firestore upon ViewModel
 * initialization.
 */

@HiltViewModel
class TutorAvailabilityViewModel @Inject constructor(
    private val repo: TutorAvailabilityRepositoryInterface
) : ViewModel() {

    // Holds the currently selected day for availability
    private val _selectedDay = MutableStateFlow<String?>(null)
    val selectedDay: StateFlow<String?> = _selectedDay

    // State to manage edit mode
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    // Holds the set of selected time slots for the selected day
    val _selectedTimeSlots = MutableStateFlow<Set<TimeSlot>>(emptySet())
    val selectedTimeSlots: StateFlow<Set<TimeSlot>> = _selectedTimeSlots

    // Holds the list of all availability entries for the tutor
    val _tutorAvailabilityList = MutableStateFlow<List<TutorAvailability>>(emptyList())
    val tutorAvailabilityList: StateFlow<List<TutorAvailability>> = _tutorAvailabilityList

    private val _isCurrentMonth = MutableStateFlow(false)

    // Generates time intervals from 9:00 AM to 9:30 PM in 30-minute increments
    val timeIntervals = generateTimeIntervals()

    private fun generateTimeIntervals(): List<String> {
        val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        return (9..20).flatMap { h ->
            listOf(
                fmt.format(Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,h);set(Calendar.MINUTE,0)}.time),
                fmt.format(Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,h);set(Calendar.MINUTE,30)}.time)
            )
        }
    }

    init {
        fetchAvailabilityFromFirestore() // Automatically fetch on ViewModel creation
    }

    fun fetchAvailabilityFromFirestore(plusMonth: YearMonth? = null) {
        val tutorId = repo.getCurrentUserId() ?: return
        val monthYear = if (plusMonth!=null) {
            _isCurrentMonth.value=false
            SimpleDateFormat("yyyy-MM",Locale.getDefault())
                .format(Calendar.getInstance().apply{add(Calendar.MONTH,1)}.time)
        } else {
            _isCurrentMonth.value=true
            SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(Date())
        }
        viewModelScope.launch { repo.initializeSessionCount(tutorId,monthYear) }
        repo.observeAvailability(tutorId,monthYear,{
            _tutorAvailabilityList.value=it?.availability?:emptyList()
        },{})
    }

    fun saveAvailability() {
        val day=_selectedDay.value?:return
        val list=_tutorAvailabilityList.value.toMutableList()
        if(_selectedTimeSlots.value.isEmpty()){
            list.removeAll{it.day==day}
        } else {
            val idx=list.indexOfFirst{it.day==day}
            if(idx>=0) list[idx]=list[idx].copy(timeSlots=_selectedTimeSlots.value.toList())
            else list+=TutorAvailability(day,_selectedTimeSlots.value.toList())
        }
        _tutorAvailabilityList.value=list
        _isEditing.value=false
        val tutorId=repo.getCurrentUserId()?:return
        val monthYear= if(_isCurrentMonth.value) SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(Date())
        else SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(Calendar.getInstance().apply{add(Calendar.MONTH,1)}.time)
        viewModelScope.launch {
            repo.saveAvailability(tutorId,monthYear,TutorAvailabilityWrapper(list))
        }
    }

    fun selectDay(day:String){
        _selectedDay.value=day
        _selectedTimeSlots.value = _tutorAvailabilityList.value.find{it.day==day}?.timeSlots?.toSet()?:emptySet()
    }

    fun toggleEditMode(){ _isEditing.value=!_isEditing.value }
    fun cancelEdit(){ selectDay(_selectedDay.value!!); _isEditing.value=false }

    fun clearSelectedDay() {
        _selectedDay.value = null
    }

    fun selectAllOnline() {
        _selectedTimeSlots.value = _selectedTimeSlots.value.toMutableSet().apply {
            timeIntervals.forEach { time ->
                if (any { it.time == time && it.booked }) return@forEach
                val existing = find { it.time == time }
                if (existing != null) {
                    remove(existing)
                    add(existing.copy(online = true))
                } else {
                    add(TimeSlot(time = time, online = true))
                }
            }
        }
    }

    fun clearAllOnline() {
        _selectedTimeSlots.value = _selectedTimeSlots.value.toMutableSet().apply {
            timeIntervals.forEach { time ->
                if (any { it.time == time && it.booked }) return@forEach
                val existing = find { it.time == time }
                if (existing != null) {
                    val updated = existing.copy(online = false)
                    remove(existing)
                    if (updated.inPerson) add(updated)
                }
            }
        }
    }

    fun selectAllInPerson() {
        _selectedTimeSlots.value = _selectedTimeSlots.value.toMutableSet().apply {
            timeIntervals.forEach { time ->
                if (any { it.time == time && it.booked }) return@forEach
                val existing = find { it.time == time }
                if (existing != null) {
                    remove(existing)
                    add(existing.copy(inPerson = true))
                } else {
                    add(TimeSlot(time = time, inPerson = true))
                }
            }
        }
    }

    fun clearAllInPerson() {
        _selectedTimeSlots.value = _selectedTimeSlots.value.toMutableSet().apply {
            timeIntervals.forEach { time ->
                if (any { it.time == time && it.booked }) return@forEach
                val existing = find { it.time == time }
                if (existing != null) {
                    val updated = existing.copy(inPerson = false)
                    remove(existing)
                    if (updated.online) add(updated)
                }
            }
        }
    }

    // Toggles the selection of a time slot for the selected day
    fun toggleTimeSlotSelection(timeSlot: String, mode: String) {
        _selectedTimeSlots.value = _selectedTimeSlots.value.toMutableSet().apply {
            val existing = find { it.time == timeSlot }
            if (existing != null) {
                remove(existing)
                val updated = existing.copy(
                    online = if (mode == "online") !existing.online else existing.online,
                    inPerson = if (mode == "inPerson") !existing.inPerson else existing.inPerson
                )
                if (updated.online || updated.inPerson) add(updated)
            } else {
                add(TimeSlot(time = timeSlot, online = mode == "online", inPerson = mode == "inPerson", booked = false))
            }
        }
    }

}
