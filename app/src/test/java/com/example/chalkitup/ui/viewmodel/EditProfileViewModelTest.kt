package com.example.chalkitup.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.chalkitup.domain.model.Interest
import com.example.chalkitup.domain.model.UserProfile
import com.example.chalkitup.domain.repository.EditProfileRepositoryInterface
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: EditProfileViewModel
    private val repo: EditProfileRepositoryInterface = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private val fakeProfile = UserProfile(
        userType = "Student",
        firstName = "Alice",
        lastName = "Smith",
        email = "",
        subjects = emptyList(),
        bio = "",
        startingPrice = "",
        experience = "",
        interests = listOf(Interest("Math", false)),
        progress = emptyList()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repo.getCurrentUserId() } returns "user123"
        coEvery { repo.loadUserProfile(any()) } returns Result.success(fakeProfile)
        coEvery { repo.loadProfilePicture(any()) } returns Result.success("https://original-url.com/pic.jpg")

        viewModel = EditProfileViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uploadProfilePictureTemporarily updates LiveData with temp url`() = runTest {
        val testUri = mockk<android.net.Uri>()
        val tempUrl = "https://fake-url.com/temp.jpg"
        coEvery { repo.uploadProfilePictureTemporarily("user123", testUri) } returns Result.success(tempUrl)

        val observer = mockk<Observer<String?>>(relaxed = true)
        viewModel.profilePictureUrl.observeForever(observer)

        viewModel.uploadProfilePictureTemporarily(testUri)
        advanceUntilIdle()

        verify { observer.onChanged(tempUrl) }
    }

    @Test
    fun `cancelProfilePictureChange resets profile picture url`() = runTest {
        val tempUri = mockk<android.net.Uri>()
        val originalUrl = "https://original-url.com/pic.jpg"

        coEvery { repo.uploadProfilePictureTemporarily("user123", tempUri) } returns Result.success("https://fake-url.com/temp.jpg")
        coEvery { repo.deleteTemporaryProfilePicture("user123") } returns Result.success(Unit)

        val observer = mockk<Observer<String?>>(relaxed = true)
        viewModel.profilePictureUrl.observeForever(observer)

        viewModel.uploadProfilePictureTemporarily(tempUri)
        advanceUntilIdle()
        viewModel.cancelProfilePictureChange()
        advanceUntilIdle()

        verify { observer.onChanged(originalUrl) }
    }

    @Test
    fun `cancelProfilePictureChange does nothing if temp picture is null`() = runTest {
        val observer = mockk<Observer<String?>>(relaxed = true)
        viewModel.profilePictureUrl.observeForever(observer)

        viewModel.cancelProfilePictureChange()
        advanceUntilIdle()

        coVerify(exactly = 0) { repo.deleteTemporaryProfilePicture(any()) }
    }

    @Test
    fun `loadUserProfile loads data correctly`() = runTest {
        val userObserver = mockk<Observer<UserProfile?>>(relaxed = true)
        val urlObserver = mockk<Observer<String?>>(relaxed = true)

        viewModel.userProfile.observeForever(userObserver)
        viewModel.profilePictureUrl.observeForever(urlObserver)

        advanceUntilIdle()

        verify { userObserver.onChanged(fakeProfile) }
        verify { urlObserver.onChanged("https://original-url.com/pic.jpg") }
    }

}
