package com.example.chalkitup.data.repository

import com.example.chalkitup.domain.repository.AuthRepositoryInterface
import com.example.chalkitup.domain.repository.EmailValidator
import com.example.chalkitup.ui.components.TutorSubject
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

class AndroidEmailValidator @Inject constructor(): EmailValidator {
    override fun isValid(email: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


@Singleton
class AuthRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AuthRepositoryInterface {

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override suspend fun hasAgreedToTerms(uid: String): Boolean {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.getBoolean("agreeToTerms") ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified ?: false

    override suspend fun loginWithGoogleCredential(credential: AuthCredential): Result<Unit> {
        return try {
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null)
                Result.success(user)
            else
                Result.failure(Exception("User is null after login"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isAdminApproved(uid: String): Result<Boolean?> {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val userType = snapshot.getString("userType")
            if (userType == "Student" || userType == "Admin") {
                Result.success(true)
            } else {
                Result.success(snapshot.getBoolean("adminApproved"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isAdminUser(uid: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val userType = snapshot.getString("userType")
            if (userType == "Admin") {
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signupWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        userType: String,
        subjects: List<TutorSubject>
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("User is null"))
            user.reload().await()
            val userData = hashMapOf(
                "userType" to userType,
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "subjects" to subjects,
                "agreeToTerms" to false,
                "adminApproved" to false,
                "active" to true
            )
            firestore.collection("users").document(user.uid).set(userData).await()
            user.sendEmailVerification().await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resendVerificationEmail(): Result<String> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            user.sendEmailVerification().await()
            Result.success("Email sent")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun agreeToTerms() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).update("agreeToTerms", true).await()
    }

    override fun isAdminApproved(
        onResult: (Boolean?) -> Unit,
        isAdmin: (Boolean?) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onResult(null)
            isAdmin(false)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snap = firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val userType = snap.getString("userType")
                when (userType) {
                    "Student" -> {
                        // Students are always “approved”
                        withContext(Dispatchers.Main) {
                            onResult(true)
                            isAdmin(false)
                        }
                    }
                    "Admin" -> {
                        // This is an admin user
                        withContext(Dispatchers.Main) {
                            isAdmin(true)
                        }
                    }
                    else -> {
                        // Treat as Tutor: check adminApproved flag
                        val approved = snap.getBoolean("adminApproved")
                        withContext(Dispatchers.Main) {
                            onResult(approved) // true, false, or null if missing
                            isAdmin(false)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null)
                    isAdmin(false)
                }
            }
        }
    }


    override suspend fun addNotification(
        notifUserID: String,
        notifUserName: String,
        notifTime: String,
        notifDate: String
    ): Result<Unit> {
        return try {
            val notifData = hashMapOf(
                "notifID" to "",
                "notifType" to "Update",
                "notifUserID" to notifUserID,
                "notifUserName" to notifUserName,
                "notifTime" to notifTime,
                "notifDate" to notifDate,
                "comments" to "Welcome to ChalkItUp Tutors!",
                "sessType" to "",
                "sessDate" to "",
                "sessTime" to "",
                "otherID" to "",
                "otherName" to "",
                "subject" to "",
                "grade" to "",
                "spec" to "",
                "mode" to "",
                "price" to ""
            )
            firestore.collection("notifications").add(notifData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
