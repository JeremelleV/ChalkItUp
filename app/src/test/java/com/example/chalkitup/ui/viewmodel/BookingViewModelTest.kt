package com.example.chalkitup.ui.viewmodel

import app.cash.turbine.test
import com.example.chalkitup.MainDispatcherRule
import com.example.chalkitup.domain.model.UserInfo
import com.example.chalkitup.domain.repository.BookingRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate
import java.time.LocalTime

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class BookingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var mockRepo: BookingRepositoryInterface

    private lateinit var viewModel: BookingViewModel

    @Before
    fun setup() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())

        // Mock required calls used in BookingViewModel init block
        `when`(mockRepo.getCurrentUserId()).thenReturn("user123")
        `when`(mockRepo.fetchUserInfo("user123")).thenReturn(
            Result.success(
                UserInfo(
                    userType = "student",
                    firstName = "Test",
                    email = "test@example.com"
                )
            )
        )

        viewModel = BookingViewModel(mockRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectDay updates selectedDay`() = runTest {
        val date = LocalDate.of(2025, 4, 1)
        viewModel.selectDay(date)
        viewModel.selectedDay.test {
            Assert.assertEquals(date, awaitItem())
        }
    }

    @Test
    fun `selectStartTime updates selectedStartTime`() = runTest {
        val time = LocalTime.of(10, 0)
        viewModel.selectStartTime(time)
        viewModel.selectedStartTime.test {
            Assert.assertEquals(time, awaitItem())
        }
    }

    @Test
    fun `selectEndTime updates selectedEndTime`() = runTest {
        val time = LocalTime.of(11, 0)
        viewModel.selectEndTime(time)
        viewModel.selectedEndTime.test {
            Assert.assertEquals(time, awaitItem())
        }
    }

    @Test
    fun `resetDay clears day and time selections`() = runTest {
        viewModel.selectDay(LocalDate.now())
        viewModel.selectStartTime(LocalTime.of(10, 0))
        viewModel.selectEndTime(LocalTime.of(11, 0))

        viewModel.resetDay()

        viewModel.selectedDay.test {
            Assert.assertNull(awaitItem())
        }
        viewModel.selectedStartTime.test {
            Assert.assertNull(awaitItem())
        }
        viewModel.selectedEndTime.test {
            Assert.assertNull(awaitItem())
        }
    }

    @Test
    fun `toggleIsCurrentMonth switches value`() = runTest {
        viewModel.isCurrentMonth.test {
            val initial = awaitItem()
            viewModel.toggleIsCurrentMonth()
            Assert.assertEquals(!initial, awaitItem())
        }
    }

    @Test
    fun `resetMonth resets isCurrentMonth to true`() = runTest {
        viewModel.toggleIsCurrentMonth()
        viewModel.resetMonth()
        viewModel.isCurrentMonth.test {
            Assert.assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `getValidEndTimes returns all sequential 30min times after start`() {
        val startTime = LocalTime.of(9, 0)
        val availableTimes = listOf(
            LocalTime.of(9, 30),
            LocalTime.of(10, 0),
            LocalTime.of(10, 30),
            LocalTime.of(11, 0)
        )

        val result = viewModel.getValidEndTimes(startTime, availableTimes)
        val expected = listOf(
            LocalTime.of(9, 30),
            LocalTime.of(10, 0),
            LocalTime.of(10, 30),
            LocalTime.of(11, 0),
            LocalTime.of(11, 30)
        )
        Assert.assertEquals(expected, result)
    }

    @Test
    fun `resetState resets state variables to initial values`() = runTest {
        viewModel.selectDay(LocalDate.now())
        viewModel.selectStartTime(LocalTime.of(10, 0))
        viewModel.selectEndTime(LocalTime.of(11, 0))
        viewModel.toggleIsCurrentMonth()
        viewModel.resetState()

        viewModel.selectedDay.test {
            Assert.assertNull(awaitItem())
        }
        viewModel.selectedStartTime.test {
            Assert.assertNull(awaitItem())
        }
        viewModel.selectedEndTime.test {
            Assert.assertNull(awaitItem())
        }
        viewModel.isCurrentMonth.test {
            Assert.assertEquals(true, awaitItem())
        }
    }
}
