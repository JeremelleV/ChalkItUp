package com.example.chalkitup.data.repository

import android.net.Uri
import com.example.chalkitup.CoroutineTestRule
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Message
import com.example.chalkitup.domain.model.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.whenever
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ChatRepositoryTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockAuth: FirebaseAuth
    @Mock private lateinit var mockStorage: FirebaseStorage

    @Mock private lateinit var mockFirebaseUser: FirebaseUser
    @Mock private lateinit var mockStorageRef: StorageReference
    @Mock private lateinit var mockUsersCollection: CollectionReference
    @Mock private lateinit var mockUserDocRef: DocumentReference
    @Mock private lateinit var mockUserDocSnap: DocumentSnapshot

    @Mock private lateinit var mockConvoCollection: CollectionReference
    @Mock private lateinit var mockMessageDocRef: DocumentReference

    private lateinit var repository: ChatRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = ChatRepository(mockAuth, mockStorage, mockFirestore)
    }

    @Test
    fun `fetchCurrentUserId returns correct uid`() = runTest {
        whenever(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("uid123")

        val result = repository.fetchCurrentUserId()
        assertEquals("uid123", result)
    }

    @Test
    fun `fetchUser returns user data and profile URL on success`() = runTest {
        val testUid = "user123"

        // Mock Firestore
        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(testUid)).thenReturn(mockUserDocRef)
        whenever(mockUserDocRef.get()).thenReturn(Tasks.forResult(mockUserDocSnap))

        // Mock document data
        whenever(mockUserDocSnap.id).thenReturn(testUid)
        whenever(mockUserDocSnap.getString("firstName")).thenReturn("Billy")
        whenever(mockUserDocSnap.getString("lastName")).thenReturn("Bob")
        whenever(mockUserDocSnap.getString("userType")).thenReturn("Student")

        // Mock Storage
        val mockProfilePicRef = mock<StorageReference>()
        whenever(mockStorage.reference).thenReturn(mockStorageRef)
        whenever(mockStorageRef.child("$testUid/profilePicture.jpg")).thenReturn(mockProfilePicRef)

        // Mock Uri
        val mockUri = mock<Uri>()
        whenever(mockUri.toString()).thenReturn("http://chalk.com/profilePic.jpg")
        whenever(mockProfilePicRef.metadata).thenReturn(Tasks.forResult(mock()))
        whenever(mockProfilePicRef.downloadUrl).thenReturn(Tasks.forResult(mockUri))

        val result = repository.fetchUser(testUid)

        assertTrue(result is Response.Success)
        val user = (result as Response.Success).data
        assertEquals(testUid, user.id)
        assertEquals("Billy", user.firstName)
        assertEquals("Bob", user.lastName)
        assertEquals("Student", user.userType)
        assertEquals("http://chalk.com/profilePic.jpg", user.userProfilePictureUrl)
    }

    @Test
    fun `createConversation returns conversation id for valid student`() = runTest {
        val currentUser = User(id="uid1", firstName="Alice", lastName="Yi", userType="Student", userProfilePictureUrl="")
        val selectedUser = User(id="uid2", firstName="Bob", lastName="Brown", userType="Tutor", userProfilePictureUrl="")

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvoCollection)
        val mockDocRef = mock<DocumentReference>()
        whenever(mockConvoCollection.add(any())).thenReturn(Tasks.forResult(mockDocRef))
        whenever(mockDocRef.id).thenReturn("convo123")

        val result = repository.createConversation(currentUser, selectedUser)
        assertTrue(result is Response.Success)
        if (result is Response.Success) {
            assertEquals("convo123", result.data)
        }
    }

    @Test
    fun `createConversation returns error if Firestore add fails`() = runTest {
        val currentUser = User("uidStudent", "Alice", "Anderson", "Student", "")
        val selectedUser = User("uidTutor", "Bob", "Brown", "Tutor", "")

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvoCollection)
        whenever(mockConvoCollection.add(any())).thenThrow(RuntimeException("Add failed"))

        val result = repository.createConversation(currentUser, selectedUser)
        assertTrue(result is Response.Error)
        if (result is Response.Error) {
            assertTrue(result.message.contains("Add failed"))
        }
    }

    @Test
    fun `createMessage returns success after sending a message`() = runTest {
        val conversationId = "convo123"
        val message = Message("uidStudent", "Hello there", System.currentTimeMillis())
        val userType = "Student"

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvoCollection)
        whenever(mockConvoCollection.document(conversationId)).thenReturn(mockMessageDocRef)

        // For message creation
        whenever(mockMessageDocRef.collection("messages").add(any()))
            .thenReturn(Tasks.forResult(null))

        // For last message update
        whenever(mockMessageDocRef.update(anyMap())).thenReturn(Tasks.forResult(null))

        val result = repository.createMessage(conversationId, message, userType)
        assertTrue(result is Response.Success)
    }

    @Test
    fun `createMessage returns error if creation fails`() = runTest {
        val conversationId = "convoXYZ"
        val message = Message("uidTutor", "Hi!", 1234567890L)
        val userType = "Tutor"

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvoCollection)
        whenever(mockConvoCollection.document(conversationId)).thenReturn(mockMessageDocRef)

        // Simulate an exception when adding the new message doc
        whenever(mockMessageDocRef.collection("messages").add(any()))
            .thenThrow(RuntimeException("Creation failed"))

        val result = repository.createMessage(conversationId, message, userType)
        assertTrue(result is Response.Error)
        if (result is Response.Error) {
            assertTrue(result.message.contains("Creation failed"))
        }
    }



}
