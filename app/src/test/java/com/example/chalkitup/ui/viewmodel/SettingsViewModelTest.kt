package com.example.chalkitup.ui.viewmodel

import com.example.chalkitup.CoroutineTestRule
import com.example.chalkitup.domain.repository.SettingsRepositoryInterface
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val repo = mock<SettingsRepositoryInterface>()
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        viewModel = SettingsViewModel(repo)
    }

    @Test
    fun `getEmail returns email from repository`() {
        whenever(repo.getEmail()).thenReturn("user@email.com")

        val result = viewModel.getEmail()

        assertEquals("user@email.com", result)
    }

    @Test
    fun `deleteAccount invokes onSuccess on success`() = runTest {
        whenever(repo.deleteAccount()).thenReturn(Result.success(Unit))

        var successCalled = false
        var errorCalled = false

        viewModel.deleteAccount(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )

        advanceUntilIdle()

        assertTrue(successCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun `deleteAccount invokes onError on failure`() = runTest {
        whenever(repo.deleteAccount()).thenReturn(Result.failure(Exception("Error")))

        var successCalled = false
        var errorCalled = false

        viewModel.deleteAccount(
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )

        advanceUntilIdle()

        assertTrue(errorCalled)
        assertFalse(successCalled)
    }
}
