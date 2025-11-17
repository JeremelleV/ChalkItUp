package com.example.chalkitup.domain

/**
 * Wrapper class that represents different states of data loading.
 * Used to handle loading, success, and error states in repositories and ViewModels.
 */
sealed class Response<out T> {
    object Loading : Response<Nothing>()

    data class Success<out T>(
        val data: T
    ) : Response<T>()

    data class Error(
        val message: String
    ) : Response<Nothing>()

}