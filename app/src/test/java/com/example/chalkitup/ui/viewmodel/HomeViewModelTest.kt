package com.example.chalkitup.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chalkitup.domain.model.Appointment
import com.example.chalkitup.domain.repository.HomeRepositoryInterface
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repo: HomeRepositoryInterface
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()

        // Mock all functions called in init block
        coEvery { repo.getUserNameAndType() } returns Result.success("Test" to "Student")
        coEvery { repo.fetchBookedDates() } returns Result.success(emptyList())
        coEvery { repo.fetchAppointments() } returns Result.success(emptyList())

        viewModel = HomeViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUserName updates userName and userType correctly`() = runTest {
        advanceUntilIdle()
        assertEquals("Test", viewModel.userName.value)
        assertEquals("Student", viewModel.userType.value)
    }

    @Test
    fun `loadProfilePicture updates profilePic`() = runTest {
        val uid = "uid123"
        coEvery { repo.loadProfilePicture(uid) } returns Result.success("http://url.com/image.jpg")

        viewModel.loadProfilePicture(uid)
        advanceUntilIdle()

        assertEquals("http://url.com/image.jpg", viewModel.profilePic.value)
    }

    @Test
    fun `loadBookedDates updates bookedDates`() = runTest {
        val expectedDates = listOf("2025-04-10", "2025-04-15")

        // Set up mocks BEFORE instantiating the ViewModel
        coEvery { repo.getUserNameAndType() } returns Result.success("Test" to "Student")
        coEvery { repo.fetchBookedDates() } returns Result.success(expectedDates)
        coEvery { repo.fetchAppointments() } returns Result.success(emptyList())

        viewModel = HomeViewModel(repo)

        advanceUntilIdle()

        assertEquals(expectedDates, viewModel.bookedDates.value)
    }


    @Test
    fun `loadAppointments updates appointments`() = runTest {
        val appt = Appointment(
            appointmentID = "id",
            tutorID = "t1",
            studentID = "s1",
            date = "2099-12-31",
            time = "11:30 AM - 12:30 PM",
            subjectObject = mapOf("subject" to "Math", "grade" to "10", "specialization" to "", "price" to "20"),
            tutorName = "Tutor",
            studentName = "Student",
            mode = "Online",
            comments = "See you"
        )
        coEvery { repo.fetchAppointments() } returns Result.success(listOf(appt))

        viewModel.loadAppointments()
        advanceUntilIdle()

        assertEquals(1, viewModel.appointments.value.size)
        assertEquals("Math", viewModel.appointments.value[0].subjectObject["subject"])
    }

    @Test
    fun `cancelAppointment triggers reloads`() = runTest {
        val appt = Appointment(
            appointmentID = "id",
            tutorID = "t1",
            studentID = "s1",
            date = "2099-12-31",
            time = "11:30 AM - 12:30 PM",
            subjectObject = mapOf("subject" to "Math", "grade" to "10", "specialization" to "", "price" to "20"),
            tutorName = "Tutor",
            studentName = "Student",
            mode = "Online",
            comments = "See you"
        )
        coEvery { repo.cancelAppointment(appt) } returns Result.success(Unit)
        coEvery { repo.fetchAppointments() } returns Result.success(emptyList())
        coEvery { repo.fetchBookedDates() } returns Result.success(emptyList())

        var complete = false
        viewModel.cancelAppointment(appt) { complete = true }
        advanceUntilIdle()

        assertTrue(complete)
        assertEquals(emptyList<Appointment>(), viewModel.appointments.value)
    }
}
