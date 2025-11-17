package com.example.chalkitup.ui.viewmodel

import com.example.chalkitup.MainDispatcherRule
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.domain.repository.ChatRepositoryInterface
import com.example.chalkitup.ui.viewmodel.chat.ChatViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Message
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.mockito.kotlin.any

@ExperimentalCoroutinesApi
class ChatViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val chatRepository = mock<ChatRepositoryInterface>()
    private lateinit var viewModel: ChatViewModel

    private val testUser = User(id = "user1", firstName = "Test", lastName = "User", userType = "Student")
    private val selectedUser = User(id = "user2", firstName = "Tutor", lastName = "Example", userType = "Tutor")

    @Before
    fun setup() {
        runTest {
            whenever(chatRepository.fetchCurrentUserId()).thenReturn(testUser.id)
            whenever(chatRepository.fetchUser(testUser.id)).thenReturn(Response.Success(testUser))
            viewModel = ChatViewModel(chatRepository)
        }
    }

    @Test
    fun `loadCurrentUser should update currentUser state on success`() = runTest {
        viewModel.loadCurrentUser(testUser.id)
        assertEquals(testUser, viewModel.currentUser.value)
    }

    @Test
    fun `loadCurrentUser should update error state on failure`() = runTest {
        whenever(chatRepository.fetchUser(testUser.id)).thenReturn(Response.Error("Not found"))
        viewModel.loadCurrentUser(testUser.id)
        assertEquals("Not found", viewModel.error.value)
    }

    @Test
    fun `loadSelectedUserProfile should update selectedUser state on success`() = runTest {
        whenever(chatRepository.fetchUser(selectedUser.id)).thenReturn(Response.Success(selectedUser))
        viewModel.loadSelectedUserProfile(selectedUser.id)
        assertEquals(selectedUser, viewModel.selectedUser.value)
    }

    @Test
    fun `loadSelectedUserProfile should update error state on failure`() = runTest {
        whenever(chatRepository.fetchUser(selectedUser.id)).thenReturn(Response.Error("User not found"))
        viewModel.loadSelectedUserProfile(selectedUser.id)
        assertEquals("User not found", viewModel.error.value)
    }

    @Test
    fun `sendMessage should return error when current user is not loaded`() = runTest {
        viewModel = ChatViewModel(chatRepository) // override init call
        val result = viewModel.sendMessage("Hello", "conversation1")
        assertTrue(result is Response.Error)
    }

    @Test
    fun `sendMessage should return error when selected user is not loaded`() = runTest {
        viewModel = ChatViewModel(chatRepository)
        viewModel.loadCurrentUser(testUser.id)
        val result = viewModel.sendMessage("Hello", "conversation1")
        assertTrue(result is Response.Error)
    }

    @Test
    fun `sendMessage should return error on blank message`() = runTest {
        whenever(chatRepository.fetchUser(testUser.id)).thenReturn(Response.Success(testUser))
        whenever(chatRepository.fetchUser(selectedUser.id)).thenReturn(Response.Success(selectedUser))

        viewModel.loadCurrentUser(testUser.id)
        viewModel.loadSelectedUserProfile(selectedUser.id)

        val result = viewModel.sendMessage("", "someConvoId")

        assertTrue(result is Response.Error && result.message == "Message cannot be empty")
    }

    @Test
    fun `sendMessage should create new conversation and message on success`() = runTest {
        // Arrange
        whenever(chatRepository.fetchUser(testUser.id)).thenReturn(Response.Success(testUser))
        whenever(chatRepository.fetchUser(selectedUser.id)).thenReturn(Response.Success(selectedUser))
        whenever(chatRepository.createConversation(testUser, selectedUser))
            .thenReturn(Response.Success("newConvo"))

        // ✅ This is what's missing!
        whenever(chatRepository.fetchMessages("newConvo")).thenReturn(
            flowOf(Response.Success(emptyList()))
        )

        whenever(chatRepository.createMessage(any(), any(), any()))
            .thenReturn(Response.Success(Unit))

        viewModel.loadCurrentUser(testUser.id)
        viewModel.loadSelectedUserProfile(selectedUser.id)

        // Act
        val result = viewModel.sendMessage("Hello", null)

        // Assert
        assertTrue(result is Response.Success && result.data == "newConvo")
    }

    @Test
    fun `sendMessage should return error when conversation creation fails`() = runTest {
        // Arrange
        whenever(chatRepository.fetchUser(testUser.id)).thenReturn(Response.Success(testUser))
        whenever(chatRepository.fetchUser(selectedUser.id)).thenReturn(Response.Success(selectedUser))
        whenever(chatRepository.createConversation(testUser, selectedUser))
            .thenReturn(Response.Error("Failed to create"))

        viewModel.loadCurrentUser(testUser.id)
        viewModel.loadSelectedUserProfile(selectedUser.id)

        // Act
        val result = viewModel.sendMessage("Hello", null)

        // Assert
        assertTrue(result is Response.Error && result.message == "Failed to create")
    }

    @Test
    fun `setupMessageListener with null conversationId sets messages to empty list`() = runTest {
        viewModel.setupMessageListener(null)
        assertTrue(viewModel.messages.value is Response.Success &&
                (viewModel.messages.value as Response.Success).data.isEmpty())
    }

    @Test
    fun `setupMessageListener collects messages on valid conversationId`() = runTest {
        val messagesFlow = flowOf(Response.Success(listOf(Message("user1", "Hi", 123L))))
        whenever(chatRepository.fetchMessages("convo123")).thenReturn(messagesFlow)

        viewModel.loadCurrentUser(testUser.id)
        viewModel.setupMessageListener("convo123")

        advanceUntilIdle()
        val result = viewModel.messages.value
        assertTrue(result is Response.Success && result.data.first().text == "Hi")
    }
}
