package com.example.chalkitup.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.model.Appointment
import com.example.chalkitup.domain.repository.BookingRepositoryInterface
import com.example.chalkitup.ui.components.TutorSubject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepositoryInterface
) : ViewModel() {

    // --- UI State & Local Variables ---
    private val _userSubject = MutableStateFlow<TutorSubject?>(null)

    private val _userType = MutableStateFlow<String?>("Unknown")
    val userType: StateFlow<String?> get() = _userType

    private val _fName = MutableStateFlow<String?>("Unknown")
    private val _userEmail = MutableStateFlow<String?>("Unknown")
    private val _tutorName = MutableStateFlow<String?>("Unknown")
    private val _price = MutableStateFlow<String?>("Unknown")
    private val _timeSlot = MutableStateFlow<String?>("Unknown")
    private val _date = MutableStateFlow<String?>("Unknown")

    private val _tutors = MutableStateFlow<List<String>>(emptyList())
    val tutors: StateFlow<List<String>> get() = _tutors

    private val _availability = MutableStateFlow<Map<LocalDate, List<LocalTime>>>(emptyMap())
    val availability: StateFlow<Map<LocalDate, List<LocalTime>>> get() = _availability

    private val _tutorAvailabilityMap = MutableStateFlow<Map<String, Map<LocalDate, List<LocalTime>>>>(emptyMap())

    private val _selectedDay = MutableStateFlow<LocalDate?>(null)
    val selectedDay: StateFlow<LocalDate?> get() = _selectedDay

    private val _selectedStartTime = MutableStateFlow<LocalTime?>(null)
    val selectedStartTime: StateFlow<LocalTime?> get() = _selectedStartTime

    private val _selectedEndTime = MutableStateFlow<LocalTime?>(null)
    val selectedEndTime: StateFlow<LocalTime?> get() = _selectedEndTime

    private val _isCurrentMonth = MutableStateFlow(true)
    val isCurrentMonth: StateFlow<Boolean> get() = _isCurrentMonth

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    init {
        viewModelScope.launch {
            bookingRepository.fetchUserInfo(bookingRepository.getCurrentUserId()!!)
                .fold(onSuccess = {
                    _userType.value = it.userType
                    _fName.value = it.firstName
                    _userEmail.value = it.email
                }, onFailure = { })
        }
    }

    // --- REPOSITORY DELEGATION FUNCTIONS ---

    fun getCurrentUserActiveStatus (onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            bookingRepository.fetchUserActiveStatus().fold(
                onSuccess = { onComplete(it) },
                onFailure = { onComplete(false) }
            )
        }
    }

    init {
        getUserInfoFromUsers()
    }

    private fun getUserInfoFromUsers() {
        viewModelScope.launch {
            bookingRepository.fetchUserInfo(bookingRepository.getCurrentUserId()!!)
                .fold(onSuccess = { userInfo ->
                    _userType.value = userInfo.userType
                    _fName.value = userInfo.firstName
                    _userEmail.value = userInfo.email
                },
                onFailure = { error ->
                    Log.e("BookingViewModel", "Error fetching user info: ${error.message}")
                }
            )
        }
    }

    fun submitBooking(
        tutorId: String,
        comments: String,
        sessionType: String,
        subject: TutorSubject,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val day = _selectedDay.value!!
            val start = _selectedStartTime.value!!
            val end = _selectedEndTime.value!!
            val studentId = bookingRepository.getCurrentUserId()!!
            bookingRepository.updateSessionCount(tutorId, day.format(DateTimeFormatter.ofPattern("yyyy-MM")), getWeekNumber(day))
            bookingRepository.markTimesAsBooked(tutorId, day.format(DateTimeFormatter.ofPattern("yyyy-MM")), day, start, end)
            val tutorFull = bookingRepository.fetchUserFullName(tutorId).getOrThrow()
            val studentFull = bookingRepository.fetchUserFullName(studentId).getOrThrow()
            val priceStr = bookingRepository.fetchTutorPriceForSubject(tutorId, subject).getOrThrow()
            subject.price = priceStr
            val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
            val timeRange = "${start.format(fmt)} - ${end.format(fmt)}"
            _userSubject.value = subject; _price.value = priceStr; _timeSlot.value = timeRange; _date.value = day.toString(); _tutorName.value = tutorFull
            bookingRepository.addSession(tutorId, comments, sessionType, day, start, end, subject, studentId, tutorFull, studentFull)
            bookingRepository.sendEmail(tutorId, _fName.value!!, tutorFull, subject, _date.value!!, _timeSlot.value!!, priceStr, _userEmail.value!!)
            onSuccess()
        }
    }

    fun setSubject(subject: TutorSubject, priceRange: ClosedFloatingPointRange<Float>, mode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            bookingRepository.fetchTutors(subject, priceRange).fold(
                onSuccess = { tutors ->
                    _tutors.value = tutors
                    bookingRepository.fetchAvailabilityForTutors(tutors, _isCurrentMonth.value, mode).fold(
                        onSuccess = { (avail, map) ->
                            _availability.value = avail; _tutorAvailabilityMap.value = map
                        }, onFailure = { _error.value = it.message }
                    )
                }, onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun matchTutorForTimeRange(
        selectedDay: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val map = _tutorAvailabilityMap.value
            val weekNumber = getWeekNumber(selectedDay)
            val yearMonth = selectedDay.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val matches = mutableListOf<Pair<String, Int>>()
            for ((tutorId, availability) in map) {
                val dayAvail = availability[selectedDay] ?: continue
                if (dayAvail.contains(startTime) && dayAvail.contains(endTime.minusMinutes(30))) {
                    var cur = startTime
                    while (cur < endTime) {
                        if (cur !in dayAvail) break
                        cur = cur.plusMinutes(30)
                    }
                    if (cur >= endTime) {
                        val count = bookingRepository.getSessionCountForWeek(tutorId, yearMonth, weekNumber)
                            .getOrDefault(0)
                        matches += tutorId to count
                    }
                }
            }
            onResult(matches.minByOrNull { it.second }?.first)
        }
    }

    // --- UI State & Helper Functions (Remain in the ViewModel) ---

    fun toggleIsCurrentMonth() {
        _isCurrentMonth.value = !_isCurrentMonth.value
    }

    fun resetState() {
        _selectedDay.value = null
        _selectedStartTime.value = null
        _selectedEndTime.value = null
        _availability.value = emptyMap()
        _isLoading.value = false
        _error.value = null
        _isCurrentMonth.value = true

        _userSubject.value = null
        _tutorName.value = "Unknown"
        _userType.value = "Unknown"
        _fName.value = "Unknown"
        _userEmail.value = "Unknown"
        _timeSlot.value = "Unknown"
        _date.value = "Unknown"
    }

    fun resetDay() {
        _selectedDay.value = null
        _selectedStartTime.value = null
        _selectedEndTime.value = null
    }

    fun resetMonth() {
        _isCurrentMonth.value = true
    }

    fun getFirstDayOfMonth(currentDate: LocalDate): LocalDate =
        currentDate.withDayOfMonth(1)

    fun getLastDayOfMonth(currentDate: LocalDate): LocalDate =
        currentDate.withDayOfMonth(currentDate.lengthOfMonth())

    fun selectDay(day: LocalDate) {
        _selectedDay.value = day
        _selectedStartTime.value = null
        _selectedEndTime.value = null
    }

    fun selectStartTime(time: LocalTime) {
        _selectedStartTime.value = time
        _selectedEndTime.value = null
    }

    fun selectEndTime(time: LocalTime) {
        _selectedEndTime.value = time
    }

    fun getValidEndTimes(startTime: LocalTime, availability: List<LocalTime>): List<LocalTime> {
        val validEndTimes = mutableListOf<LocalTime>()
        var currentTime = startTime.plusMinutes(30)
        while (currentTime in availability) {
            validEndTimes.add(currentTime)
            currentTime = currentTime.plusMinutes(30)
        }
        validEndTimes.add(currentTime)
        return validEndTimes
    }

    private fun getWeekNumber(date: LocalDate): Int {
        val firstDayOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = firstDayOfMonth.dayOfWeek.value
        return (date.dayOfMonth + dayOfWeek - 1) / 7 + 1
    }
}




object BookingManager {
    private lateinit var userFile: File

    // Initialize function to set up the bookings file if it doesn't exist
    fun init(fileDirectory: File) {
        userFile = File(fileDirectory, "bookings.json")
        if (!userFile.exists()) {
            userFile.writeText(JSONArray().toString())  // Initialize with an empty array
            println("Booking file initialized.")
        }
    }

    // Function to add a new booking
    fun addBooking(app: Appointment) {
        val jsonArray = JSONArray(userFile.readText())

        val jsonObject = JSONObject().apply {
            put("appointmentID", app.appointmentID)
            put("studentId", app.studentID)
            put("tutorId", app.tutorID)
            put("tutorName", app.tutorName)
            put("studentName", app.studentName)
            put("date", app.date)
            put("time", app.time)
            put("subject", app.subject)
            put("mode", app.mode)
            put("comments", app.comments)
            put("subjectObject", app.subjectObject)  // subjectObject is still a Map
        }

        jsonArray.put(jsonObject)
        userFile.writeText(jsonArray.toString(4)) // Pretty print with indentation
    }

    // Function to remove a booking
    fun removeBooking(appointmentID: String) {
        val jsonArray = JSONArray(userFile.readText())

        val filteredArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("appointmentID") != appointmentID) {
                filteredArray.put(obj)
            }
        }

        userFile.writeText(filteredArray.toString(4))
    }

    // Function to read all bookings from the file
    fun readBookings(): List<Appointment> {
        val jsonArray = JSONArray(userFile.readText())
        val bookings = mutableListOf<Appointment>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            // Safely retrieve values with optString for missing or null keys
            val appointmentID = obj.optString("appointmentID", "")
            val studentID = obj.optString("studentID", "")
            val tutorID = obj.optString("tutorID", "")
            val tutorName = obj.optString("tutorName", "")
            val studentName = obj.optString("studentName", "")
            val date = obj.optString("date", "")
            val time = obj.optString("time", "")
            val subject = obj.optString("subject", "")
            val mode = obj.optString("mode", "")
            val comments = obj.optString("comments", "")

            // Convert subjectObject to a Map<String, Any> using the safe optJSONObject and toMap()
            val subjectObject = obj.optJSONObject("subjectObject")?.toMap() ?: emptyMap()

            // Create the Appointment object
            val booking = Appointment(
                appointmentID = appointmentID,
                studentID = studentID,
                tutorID = tutorID,
                tutorName = tutorName,
                studentName = studentName,
                date = date,
                time = time,
                subject = subject,
                mode = mode,
                comments = comments,
                subjectObject = subjectObject
            )

            bookings.add(booking)
        }

        return bookings
    }

    // Function to clear all bookings (i.e., empty the file)
    fun clearBookings() {
        userFile.writeText(JSONArray().toString()) // Overwrite with an empty JSON array
        println("All bookings cleared.")
    }
}

// Extension function to convert a JSONObject to a Map<String, Any>
fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = this.get(key)  // Gets the value for the key (could be any type)
    }
    return map
}

fun JSONObject.toMapFromJSONObject(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = this.get(key)  // Gets the value for the key (could be any type)
    }
    return map
}



//BookingManager.addBooking(
//appointmentID,
//studentID,
//matchedTutorId,
//tutorName,
//studentName,
//date,
//time,
//subject,
//mode,
//comments,
//subjectObject
//)
