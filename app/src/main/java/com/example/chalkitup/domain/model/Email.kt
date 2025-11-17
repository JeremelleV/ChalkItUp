package com.example.chalkitup.domain.model

// Email Class info
data class Email (
    var to: String = "",
    var message: EmailMessage
)

data class EmailMessage(
    var subject: String = "",
    var html: String = "",
    var body: String = "",
)