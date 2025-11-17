package com.example.chalkitup.domain.repository

import com.example.chalkitup.ui.components.TutorSubject
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface EmailValidator {
    fun isValid(email: String): Boolean
}


interface AuthRepositoryInterface {

    fun getCurrentUser(): FirebaseUser?

    suspend fun hasAgreedToTerms(uid: String): Boolean

    fun isEmailVerified(): Boolean

    suspend fun loginWithGoogleCredential(credential: AuthCredential): Result<Unit>

    suspend fun loginWithEmailAndPassword(email: String, password: String): Result<FirebaseUser>

    suspend fun isAdminApproved(uid: String): Result<Boolean?>

    suspend fun isAdminUser(uid: String): Result<Boolean>

    suspend fun signupWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        userType: String,
        subjects: List<TutorSubject>
    ): Result<FirebaseUser>

    suspend fun resendVerificationEmail(): Result<String>

    fun signOut()

    suspend fun resetPassword(email: String): Result<Unit>

    suspend fun agreeToTerms()

    fun isAdminApproved(
        onResult: (Boolean?) -> Unit,
        isAdmin: (Boolean?) -> Unit
    )

    suspend fun addNotification(
        notifUserID: String,
        notifUserName: String,
        notifTime: String,
        notifDate: String
    ): Result<Unit>
}
