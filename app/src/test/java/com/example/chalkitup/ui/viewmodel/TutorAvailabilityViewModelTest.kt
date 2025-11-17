package com.example.chalkitup.ui.viewmodel

import com.example.chalkitup.MainDispatcherRule
import com.example.chalkitup.domain.model.TimeSlot
import com.example.chalkitup.domain.model.TutorAvailability
import com.example.chalkitup.domain.model.TutorAvailabilityWrapper
import com.example.chalkitup.domain.repository.TutorAvailabilityRepositoryInterface
import com.google.firebase.firestore.ListenerRegistration
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TutorAvailabilityViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repo = mock<TutorAvailabilityRepositoryInterface>()
    private lateinit var viewModel: TutorAvailabilityViewModel

    @Before
    fun setup() = runTest {
        whenever(repo.getCurrentUserId()).thenReturn("tutor123")
        whenever(repo.initializeSessionCount(any(), any())).thenReturn(Result.success(Unit))
        whenever(
            repo.observeAvailability(any(), any(), any(), any())
        ).thenAnswer {
            val callback = it.getArgument<(TutorAvailabilityWrapper?) -> Unit>(2)
            callback(null)
            mock<ListenerRegistration>()
        }

        viewModel = TutorAvailabilityViewModel(repo)
    }

    @Test
    fun `selectDay sets selected time slots correctly`() = runTest {
        val timeSlot = TimeSlot("10:00 AM", online = true)
        viewModel.selectDay("2025-04-15")
        viewModel._tutorAvailabilityList.value = listOf(TutorAvailability("2025-04-15", listOf(timeSlot)))
        viewModel.selectDay("2025-04-15")

        assertEquals("2025-04-15", viewModel.selectedDay.value)
        assertEquals(setOf(timeSlot), viewModel.selectedTimeSlots.value)
    }

    @Test
    fun `toggleTimeSlotSelection adds and removes slot correctly`() {
        val time = "10:00 AM"
        viewModel.selectDay("2025-04-15")
        viewModel.toggleTimeSlotSelection(time, "online")

        val slot = viewModel.selectedTimeSlots.value.first()
        assertTrue(slot.online)

        viewModel.toggleTimeSlotSelection(time, "online")
        assertTrue(viewModel.selectedTimeSlots.value.isEmpty())
    }

    @Test
    fun `saveAvailability updates Firestore when saving`() = runTest {
        val slot = TimeSlot("10:00 AM", online = true)
        val wrapper = TutorAvailabilityWrapper(listOf(TutorAvailability("2025-04-15", listOf(slot))))

        whenever(repo.saveAvailability(any(), any(), eq(wrapper))).thenReturn(Result.success(Unit))

        viewModel.selectDay("2025-04-15")
        viewModel._selectedTimeSlots.value = setOf(slot)
        viewModel._tutorAvailabilityList.value = emptyList()

        viewModel.saveAvailability()
        assertFalse(viewModel.isEditing.value)
        assertEquals(1, viewModel.tutorAvailabilityList.value.size)
    }
}
