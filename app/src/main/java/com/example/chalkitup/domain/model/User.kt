package com.example.chalkitup.domain.model

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val userType: String = "",
    var userProfilePictureUrl: String? = null
)

data class UserInfo(
    val userType: String,
    val firstName: String,
    val email: String
)