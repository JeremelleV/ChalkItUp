package com.example.chalkitup.ui.viewmodel.admin

import app.cash.turbine.test
import com.example.chalkitup.MainDispatcherRule
import com.example.chalkitup.domain.repository.AdminRepositoryInterface
import com.example.chalkitup.ui.components.TutorSubject
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AdminHomeViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var mockRepo: AdminRepositoryInterface

    private lateinit var viewModel: AdminHomeViewModel

    private val unapprovedTutorsFlow = MutableStateFlow(listOf<User>())
    private val approvedTutorsFlow = MutableStateFlow(listOf<User>())
    private val reportsFlow = MutableStateFlow(listOf<Report>())
    private val usersWithReportsFlow = MutableStateFlow(listOf<User>())

    @Before
    fun setup() {
        `when`(mockRepo.getUnapprovedTutors()).thenReturn(unapprovedTutorsFlow)
        `when`(mockRepo.getApprovedTutors()).thenReturn(approvedTutorsFlow)
        `when`(mockRepo.getReports()).thenReturn(reportsFlow)
        `when`(mockRepo.getUsersWithReports()).thenReturn(usersWithReportsFlow)

        viewModel = AdminHomeViewModel(mockRepo)
    }

    @Test
    fun `approveTutor calls repository`() = runTest {
        val tutorId = "tutor123"
        viewModel.approveTutor(tutorId)
        advanceUntilIdle()
        verify(mockRepo).approveTutor(tutorId)
    }

    @Test
    fun `denyTutor calls repository`() = runTest {
        val user = User(id = "user1", userType = "Tutor", firstName = "Jane", lastName = "Doe", email = "jane@example.com", subjects = emptyList(), adminApproved = false)
        val reason = "Not qualified"
        val type = "qualification"
        viewModel.denyTutor(user, reason, type)
        advanceUntilIdle()
        verify(mockRepo).denyTutor(user, reason, type)
    }

    @Test
    fun `resolveReport calls repository`() = runTest {
        val report = Report(id = "report123", userId = "user1", reportMessage = "Spam", timestamp = Timestamp.now())
        viewModel.resolveReport(report)
        advanceUntilIdle()
        verify(mockRepo).resolveReport(report.id)
    }

    @Test
    fun `signOut calls repository`() {
        viewModel.signout()
        verify(mockRepo).signOut()
    }

    @Test
    fun `loadProfilePictures emits expected map`() = runTest {
        val userIds = listOf("id1", "id2")
        val expectedMap = mapOf("id1" to "url1", "id2" to null)
        `when`(mockRepo.getProfilePictures(userIds)).thenReturn(MutableStateFlow(expectedMap))

        viewModel.loadProfilePictures(userIds)

        viewModel.profilePictureUrls.test {
            assertEquals(expectedMap, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unapprovedTutors returns repo flow`() = runTest {
        val users = listOf(User(id = "u1", userType = "Tutor", firstName = "John", lastName = "Smith", email = "john@example.com", subjects = emptyList(), adminApproved = false))
        unapprovedTutorsFlow.value = users
        viewModel.unapprovedTutors.test {
            assertEquals(users, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `approvedTutors returns repo flow`() = runTest {
        val users = listOf(User(id = "u2", userType = "Tutor", firstName = "Ana", lastName = "Lopez", email = "ana@example.com", subjects = emptyList(), adminApproved = true))
        approvedTutorsFlow.value = users
        viewModel.approvedTutors.test {
            assertEquals(users, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reports returns repo flow`() = runTest {
        val reports = listOf(Report(id = "r1", userId = "u1", reportMessage = "Plagiarism", timestamp = Timestamp.now()))
        reportsFlow.value = reports
        viewModel.reports.test {
            assertEquals(reports, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `usersWithReports returns repo flow`() = runTest {
        val users = listOf(User(id = "u3", userType = "Student", firstName = "Tom", lastName = "Lee", email = "tom@example.com", subjects = emptyList(), adminApproved = true))
        usersWithReportsFlow.value = users
        viewModel.usersWithReports.test {
            assertEquals(users, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
