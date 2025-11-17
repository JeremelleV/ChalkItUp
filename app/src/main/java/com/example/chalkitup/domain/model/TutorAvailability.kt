package com.example.chalkitup.domain.model

/**
 * Wrapper class for storing a list of TutorAvailability objects in Firestore.
 *
 * @property availability A list of TutorAvailability entries.
 */
data class TutorAvailabilityWrapper(val availability: List<TutorAvailability> = emptyList())

// Data model for storing tutor availability
data class TutorAvailability(
    val day: String = "", // Selected day
    val timeSlots: List<TimeSlot> = emptyList() // List of available time slots for that day
)

data class TimeSlot (
    val time: String = "",
    val online: Boolean = false,
    val inPerson: Boolean = false,
    var booked: Boolean = false
)