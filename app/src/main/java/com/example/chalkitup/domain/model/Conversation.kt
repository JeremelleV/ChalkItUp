package com.example.chalkitup.domain.model

data class Conversation(
    val id: String = "",
    val studentId: String = "",
    val tutorId: String = "",
    val studentName: String = "",
    val tutorName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0,
    val lastMessageReadByStudent: Boolean = true,
    val lastMessageReadByTutor: Boolean = true
)
