package com.example.chalkitup.domain.repository

interface SettingsRepositoryInterface {
    fun getEmail(): String?
    suspend fun deleteAccount(): Result<Unit>
}
