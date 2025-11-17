package com.example.chalkitup.ui.viewmodel

import com.example.chalkitup.MainDispatcherRule
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Conversation
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.domain.repository.MessageListRepositoryInterface
import com.example.chalkitup.ui.viewmodel.chat.MessageListViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MessageListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repo = mock<MessageListRepositoryInterface>()
    private lateinit var viewModel: MessageListViewModel

    private val testUserId = "user1"
    private val testUserType = "Student"
    private val testUsers = listOf(
        User(id = "user1", firstName = "Alice", lastName = "Smith", userType = "Student"),
        User(id = "user2", firstName = "Bob", lastName = "Jones", userType = "Tutor")
    )
    private val testConversations = listOf(
        Conversation(
            studentId = "user1",
            tutorId = "user2",
            studentName = "Alice Smith",
            tutorName = "Bob Jones",
            lastMessage = "Hello",
            lastMessageReadByStudent = false,
            lastMessageReadByTutor = true,
            timestamp = 1000L
        )
    )

    @Before
    fun setup() = runTest {
        whenever(repo.getUserIdAndType()).thenReturn(Result.success(Pair(testUserId, testUserType)))
        viewModel = MessageListViewModel(repo)
    }

    @Test
    fun `init should set current user id and type`() = runTest {
        assertEquals(testUserId, viewModel.currentUserId.value)
        assertEquals(testUserType, viewModel.currentUserType.value)
    }

    @Test
    fun `loadUsers should update users on success`() = runTest {
        whenever(repo.fetchUsers()).thenReturn(flowOf(Response.Success(testUsers)))
        viewModel.loadUsers()
        assertEquals(Response.Success(testUsers), viewModel.users.value)
    }

    @Test
    fun `loadUsers should update error on failure`() = runTest {
        whenever(repo.fetchUsers()).thenReturn(flowOf(Response.Error("error")))
        viewModel.loadUsers()
        assertEquals("error", viewModel.error.value)
    }

    @Test
    fun `loadConversations should update conversations on success`() = runTest {
        whenever(repo.fetchConversations()).thenReturn(flowOf(Response.Success(testConversations)))
        viewModel.loadConversations()
        assertEquals(Response.Success(testConversations), viewModel.conversations.value)
    }

    @Test
    fun `loadConversations should update error on failure`() = runTest {
        whenever(repo.fetchConversations()).thenReturn(flowOf(Response.Error("fail")))
        viewModel.loadConversations()
        assertEquals("fail", viewModel.error.value)
    }

    @Test
    fun `getUserInfo returns the other user in conversation`() = runTest {
        whenever(repo.fetchUsers()).thenReturn(flowOf(Response.Success(testUsers)))
        viewModel.loadUsers()
        advanceUntilIdle()

        val user = viewModel.getUserInfo(testConversations.first())
        assertEquals("user2", user?.id)
    }

    @Test
    fun `fetchConversationId updates state on success`() = runTest {
        whenever(repo.fetchConversationId("user2", testUserId)).thenReturn(Response.Success("convo123"))
        val convoId = viewModel.fetchConversationId("user2")
        assertEquals("convo123", convoId)
        assertEquals("convo123", viewModel.conversationId.value)
    }

    @Test
    fun `fetchConversationId updates error on failure`() = runTest {
        whenever(repo.fetchConversationId("user2", testUserId)).thenReturn(Response.Error("fail"))
        val convoId = viewModel.fetchConversationId("user2")
        assertEquals(null, convoId)
        assertEquals("fail", viewModel.error.value)
    }

    @Test
    fun `updateSearchQuery should update value`() {
        viewModel.updateSearchQuery("bob")
        assertEquals("bob", viewModel.searchQuery.value)
    }

    @Test
    fun `getFilteredUsers filters users by query`() = runTest {
        // Arrange
        whenever(repo.fetchUsers()).thenReturn(flowOf(Response.Success(testUsers)))

        viewModel.loadUsers()
        viewModel.updateSearchQuery("Alice")

        val result = viewModel.getFilteredUsers().first { it.isNotEmpty() }

        // Assert
        assertEquals(1, result.size)
        assertEquals("Alice", result.first().firstName)
    }

    @Test
    fun `getFilteredConversations filters conversations by name`() = runTest {
        // Arrange
        whenever(repo.fetchConversations()).thenReturn(flowOf(Response.Success(testConversations)))

        viewModel.loadConversations()
        viewModel.updateSearchQuery("Bob")

        val result = viewModel.getFilteredConversations().first { it.isNotEmpty() }

        // Assert
        assertEquals(1, result.size)
        assertEquals("Bob Jones", result.first().tutorName)
    }

    @Test
    fun `isConversationUnread returns true for unread by student`() {
        val result = viewModel.isConversationUnread(testConversations[0], "Student")
        assertTrue(result)
    }

    @Test
    fun `isConversationUnread returns false for unread by tutor`() {
        val result = viewModel.isConversationUnread(testConversations[0], "Tutor")
        assertFalse(result)
    }

    @Test
    fun `markConversationAsRead calls repo with correct params`() = runTest {
        viewModel.markConversationAsRead("convo1")
        verify(repo).updateConversationStatus("convo1", testUserType)
    }
}
