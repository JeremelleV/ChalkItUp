package com.example.chalkitup.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chalkitup.domain.repository.AuthRepositoryInterface
import com.example.chalkitup.domain.repository.EmailValidator
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: AuthRepositoryInterface
    private lateinit var emailValidator: EmailValidator

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        emailValidator = mock()
        viewModel = AuthViewModel(authRepository, emailValidator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loginWithEmail triggers onSuccess when all conditions met`() = runTest {
        val email = "test@example.com"
        val password = "password"
        val firebaseUser = mock<FirebaseUser> {
            on { isEmailVerified } doReturn true
            on { uid } doReturn "123"
        }

        whenever(authRepository.loginWithEmailAndPassword(email, password))
            .thenReturn(Result.success(firebaseUser))
        whenever(authRepository.hasAgreedToTerms("123"))
            .thenReturn(true)
        whenever(authRepository.isAdminUser("123"))
            .thenReturn(Result.success(false))
        whenever(authRepository.isAdminApproved("123"))
            .thenReturn(Result.success(true))

        var called = false

        viewModel.loginWithEmail(
            email = email,
            password = password,
            onSuccess = { called = true },
            onEmailError = {},
            onTermsError = {},
            awaitingApproval = {},
            isAdmin = {},
            onError = {}
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assert(called)
    }

    @Test
    fun `loginWithGoogle calls onSuccess when successful`() = runTest {
        val account = mock<GoogleSignInAccount> {
            on { idToken } doReturn "mockToken"
        }

        val credential = mock<AuthCredential>()
        mockStatic(GoogleAuthProvider::class.java).use {
            whenever(GoogleAuthProvider.getCredential("mockToken", null))
                .thenReturn(credential)

            whenever(authRepository.loginWithGoogleCredential(credential))
                .thenReturn(Result.success(mock()))

            var called = false
            viewModel.loginWithGoogle(account, onSuccess = { called = true }, onError = {})

            testDispatcher.scheduler.advanceUntilIdle()
            assert(called)
        }
    }

    @Test
    fun `signupWithEmail triggers onUserReady when successful`() = runTest {
        val email = "email@test.com"
        val password = "123456"
        val firstName = "First"
        val lastName = "Last"
        val userType = "Tutor"
        val user = mock<FirebaseUser> {
            on { uid } doReturn "user123"
        }

        whenever(emailValidator.isValid(email)).thenReturn(true)
        whenever(authRepository.signupWithEmail(email, password, firstName, lastName, userType, emptyList()))
            .thenReturn(Result.success(user))

        var called = false
        viewModel.signupWithEmail(
            email, password, firstName, lastName, userType,
            onUserReady = { called = true },
            onError = {},
            onEmailError = {}
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assert(called)
    }

    @Test
    fun `resetPassword triggers onSuccess`() = runTest {
        val email = "reset@test.com"
        whenever(authRepository.resetPassword(email)).thenReturn(Result.success(Unit))

        var message = ""
        viewModel.resetPassword(
            email,
            onSuccess = { message = it },
            onError = {}
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assert(message == "Reset email sent")
    }

    @Test
    fun `resendVerificationEmail triggers onSuccess`() = runTest {
        whenever(authRepository.resendVerificationEmail()).thenReturn(Result.success("Email sent"))

        var message = ""
        viewModel.resendVerificationEmail(
            onSuccess = { message = it },
            onError = {}
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assert(message == "Email sent")
    }
}
