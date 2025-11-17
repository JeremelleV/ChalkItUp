package com.example.chalkitup.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chalkitup.domain.model.UserProfile
import com.example.chalkitup.domain.repository.ProfileRepositoryInterface
import com.google.firebase.firestore.ListenerRegistration
import io.mockk.coEvery
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repo: ProfileRepositoryInterface
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        viewModel = ProfileViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUserProfile sets userProfile and isTutor`() = testScope.runTest {
        val uid = "uid123"
        val profile = UserProfile(
            userType = "Tutor",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        )

        every { repo.getCurrentUserId() } returns uid
        coEvery { repo.fetchUserProfile(uid) } returns Result.success(profile)
        coEvery { repo.loadProfilePicture(uid) } returns Result.success("http://image")

        viewModel.loadUserProfile()

        advanceUntilIdle()

        assertEquals(profile, viewModel.userProfile.value)
        assertEquals(true, viewModel.isTutor.value)
        assertEquals("http://image", viewModel.profilePictureUrl.value)
    }

    @Test
    fun `loadProfilePicture sets profilePictureUrl`() = testScope.runTest {
        val userId = "uid123"
        val mockUrl = "http://image"

        coEvery { repo.loadProfilePicture(userId) } returns Result.success(mockUrl)

        viewModel.loadProfilePicture(userId)

        advanceUntilIdle()

        assertEquals(mockUrl, viewModel.profilePictureUrl.value)
    }

    @Test
    fun `startListeningForPastSessions sets stats and updates Firestore`() = runTest {
        val userId = "t1"
        val listener: ListenerRegistration = mockk(relaxed = true)

        every { repo.getCurrentUserId() } returns userId
        every {
            repo.startListeningForPastSessions(eq(userId), captureLambda())
        } answers {
            lambda<(Int, Double) -> Unit>().invoke(5, 3.0)
            listener
        }
        coEvery { repo.updateTutorStats(userId, 5, 3.0) } returns Result.success(Unit)

        viewModel.startListeningForPastSessions()

        advanceUntilIdle()
        assertEquals(5, viewModel.totalSessions.value)
        assertEquals(3.0, viewModel.totalHours.value)
    }

    @Test
    fun `reportUser calls repo and triggers onSuccess`() = runTest {
        val userId = "t2"
        val message = "Spam"
        coEvery { repo.reportUser(userId, message) } returns Result.success(Unit)

        var successCalled = false
        viewModel.reportUser(userId, message) { successCalled = true }

        advanceUntilIdle()
        assertTrue(successCalled)
    }
}
