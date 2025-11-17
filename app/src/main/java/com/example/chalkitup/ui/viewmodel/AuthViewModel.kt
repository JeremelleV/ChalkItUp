package com.example.chalkitup.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.repository.AuthRepositoryInterface
import com.example.chalkitup.domain.repository.EmailValidator
import com.example.chalkitup.ui.components.TutorSubject
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepositoryInterface,
    private val emailValidator: EmailValidator
) : ViewModel() {

    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn

    private val _isGoogleUserLoggedIn = MutableLiveData<Boolean>()
    val isGoogleUserLoggedIn: LiveData<Boolean> = _isGoogleUserLoggedIn

    init {
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        val currentUser = authRepository.getCurrentUser()
        _isUserLoggedIn.value = currentUser != null && currentUser.isEmailVerified
        _isGoogleUserLoggedIn.value = currentUser != null
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onEmailError: () -> Unit,
        onTermsError: () -> Unit,
        awaitingApproval: () -> Unit,
        isAdmin: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val loginResult = authRepository.loginWithEmailAndPassword(email, password)
            val hasAgreed    = loginResult.getOrNull()?.uid?.let { authRepository.hasAgreedToTerms(it) }
            val isAdminUser  = loginResult.getOrNull()?.uid?.let { authRepository.isAdminUser(it).getOrNull() }
            val isApproved   = loginResult.getOrNull()?.uid?.let { authRepository.isAdminApproved(it).getOrNull() }
            val errMsg       = loginResult.exceptionOrNull()?.message

            when (val code = decideLoginOutcome(loginResult, hasAgreed, isAdminUser, isApproved, errMsg)) {
                "onSuccess"             -> onSuccess()
                "onEmailError"          -> onEmailError()
                "onTermsError"          -> onTermsError()
                "awaitingApproval"      -> awaitingApproval()
                "isAdmin"               -> isAdmin()
                else -> if (code.startsWith("onError:")) {
                    onError(code.removePrefix("onError:"))
                }
            }
        }
    }

    fun decideLoginOutcome(
        loginResult: Result<FirebaseUser?>,
        hasAgreed: Boolean?,
        isAdminUser: Boolean?,
        isApproved: Boolean?,
        loginErrorMsg: String?
    ): String {
        if (loginResult.isFailure) {
            return "onError:${loginErrorMsg ?: "Login failed"}"
        }
        val user = loginResult.getOrNull() ?: return "onError:User is null"
        if (hasAgreed == false) {
            return "onTermsError"
        }
        if (!user.isEmailVerified) {
            return "onEmailError"
        }
        if (isAdminUser == true) {
            return "isAdmin"
        }
        return when (isApproved) {
            true  -> "onSuccess"
            false -> "awaitingApproval"
            null  -> "onError:Approval status unknown"
        }
    }

    fun loginWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val idToken = account.idToken
        if (idToken.isNullOrEmpty()) {
            onError("No ID token found. Please try again.")
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            val result = authRepository.loginWithGoogleCredential(credential)
            if (result.isSuccess) {
                _isGoogleUserLoggedIn.value = true
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Google login failed")
            }
        }
    }

    fun signupWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        userType: String,
        subjects: List<TutorSubject> = emptyList(),
        onUserReady: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onEmailError: () -> Unit
    ) {
        if (!emailValidator.isValid(email)) {
            onEmailError()
            return
        }
        viewModelScope.launch {
            val result = authRepository.signupWithEmail(email, password, firstName, lastName, userType, subjects)
            if (result.isSuccess) {
                val user = result.getOrNull()
                if (user != null) {
                    addNotification(
                        notifUserID = user.uid,
                        notifUserName = "$firstName $lastName",
                        notifTime = LocalTime.now().toString(),
                        notifDate = LocalDate.now().toString()
                    )
                    onUserReady(user)
                } else {
                    onError("Signup succeeded, but user is null")
                }
            } else {
                onError(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun resendVerificationEmail(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.resendVerificationEmail()
            if (result.isSuccess) {
                onSuccess(result.getOrNull() ?: "Email sent")
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to resend email")
            }
        }
    }

    fun signout() {
        authRepository.signOut()
        _isUserLoggedIn.value = false
    }

    fun resetPassword(
        email: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            if (result.isSuccess) {
                onSuccess("Reset email sent")
            } else {
                onError(result.exceptionOrNull()?.message ?: "Reset failed")
            }
        }
    }

    fun agreeToTerms() {
        viewModelScope.launch {
            authRepository.agreeToTerms()
        }
    }

    fun isAdminApproved(
        onResult: (Boolean?) -> Unit,
        isAdmin: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                onResult(null)
                isAdmin(false)
                return@launch
            }

            authRepository.isAdminApproved(
                onResult = { approved ->
                    // approved: true (ok), false (denied), null (pending or error)
                    onResult(approved)
                },
                isAdmin = { adminFlag ->
                    // adminFlag: true if admin, false otherwise
                    if (adminFlag != null) {
                        isAdmin(adminFlag)
                    }
                }
            )
        }
    }


    fun checkEmailVerified(): Boolean {
        return authRepository.isEmailVerified()
    }

    private fun addNotification(
        notifUserID: String,
        notifUserName: String,
        notifTime: String,
        notifDate: String
    ) {
        viewModelScope.launch {
            authRepository.addNotification(notifUserID, notifUserName, notifTime, notifDate)
        }
    }
}





object OfflineDataManager {
    private lateinit var userFile: File

    fun init(fileDirectory: File) {
        userFile = File(fileDirectory, "user_data.json")
    }

    fun logUser(username: String, password: String, status: String, userType: String) {
        val userData = JSONObject().apply {
            put("username", username)
            put("password", password)
            put("status", status)
            put("type", userType)
        }
        writeToFile(userData.toString())
    }

    fun changeStatus(newStatus: String) {
        val userData = readFromFile() ?: return
        val json = JSONObject(userData)
        json.put("status", newStatus)
        writeToFile(json.toString())
    }

    fun checkOfflineLogin(username: String, password: String): String? {
        val userData = readFromFile() ?: return null
        val json = JSONObject(userData)
        return if (json.getString("username") == username && json.getString("password") == password) {
            json.getString("status")
        } else {
            null
        }
    }

    fun checkUserType(username: String, password: String): String? {
        val userData = readFromFile() ?: return null
        val json = JSONObject(userData)
        return if (json.getString("username") == username && json.getString("password") == password) {
            json.optString("type", "user")
        } else {
            null
        }
    }

    fun offlineLoginWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onEmailError: () -> Unit,
        onTermsError: () -> Unit,
        onError: (String) -> Unit,
        awaitingApproval: () -> Unit,
        isAdmin: () -> Unit
    ) {
        val status = checkOfflineLogin(email, password)
        when (status) {
            "true" -> onSuccess()
            "need_email" -> onEmailError()
            "need_approval" -> awaitingApproval()
            else -> onError("Invalid credentials or no offline data available")
        }

        val userType = checkUserType(email, password)
        if (userType == "admin") isAdmin()
    }

    fun removeUser(email: String): Boolean {
        val userData = readFromFile() ?: return false
        val json = JSONObject(userData)
        if (json.getString("username") == email) {
            json.remove("username")
            json.remove("password")
            json.remove("status")
            json.remove("type")
            writeToFile(json.toString())
            return true
        }
        return false
    }

    private fun writeToFile(data: String) {
        userFile.writeText(data)
    }

    private fun readFromFile(): String? {
        return if (userFile.exists()) userFile.readText() else null
    }
}
