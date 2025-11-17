package com.example.chalkitup.data.repository

import android.net.Uri
import com.example.chalkitup.CoroutineTestRule
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Conversation
import com.example.chalkitup.domain.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.toList
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MessageListRepositoryTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockAuth: FirebaseAuth
    @Mock private lateinit var mockStorage: FirebaseStorage

    @Mock private lateinit var mockCurrentUser: FirebaseUser
    @Mock private lateinit var mockUsersCollection: CollectionReference
    @Mock private lateinit var mockConvosCollection: CollectionReference

    @Mock private lateinit var mockUserDocRef: DocumentReference
    @Mock private lateinit var mockUserDocSnapshot: DocumentSnapshot

    // Queries
    @Mock private lateinit var mockQuery: Query
    @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

    // Individual conversation documents
    @Mock private lateinit var mockConvoDocRef: DocumentReference
    @Mock private lateinit var mockConvoDocSnapshot: DocumentSnapshot

    @Mock private lateinit var mockStorageRef: StorageReference

    private lateinit var repository: MessageListRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = MessageListRepository(mockAuth, mockStorage, mockFirestore)
    }

    @Test
    fun `getUserIdAndType returns user id and type`() = runTest {
        val testUid = "uid123"
        val testUserType = "Student"

        whenever(mockAuth.currentUser).thenReturn(mockCurrentUser)
        whenever(mockCurrentUser.uid).thenReturn(testUid)

        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(testUid)).thenReturn(mockUserDocRef)
        whenever(mockUserDocRef.get()).thenReturn(Tasks.forResult(mockUserDocSnapshot))
        whenever(mockUserDocSnapshot.getString("userType")).thenReturn(testUserType)

        val result = repository.getUserIdAndType()

        assertTrue(result.isSuccess)
        val (userId, userType) = result.getOrThrow()
        assertEquals(testUid, userId)
        assertEquals(testUserType, userType)
    }

    @Test
    fun `getUserIdAndType returns failure when currentUser is null`() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        val result = repository.getUserIdAndType()

        assertTrue(result.isFailure)
        assertEquals("No user", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserIdAndType returns failure when firestore call fails`() = runTest {
        val testUid = "uid123"

        whenever(mockAuth.currentUser).thenReturn(mockCurrentUser)
        whenever(mockCurrentUser.uid).thenReturn(testUid)

        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(testUid)).thenReturn(mockUserDocRef)

        whenever(mockUserDocRef.get()).thenThrow(RuntimeException("Firestore error"))

        val result = repository.getUserIdAndType()

        assertTrue(result.isFailure)
        assertEquals("Firestore error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchConversations returns conversations for student type`() = runTest {
        val testUid = "uid123"
        val testUserType = "Student"

        whenever(mockAuth.currentUser).thenReturn(mockCurrentUser)
        whenever(mockCurrentUser.uid).thenReturn(testUid)

        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(testUid)).thenReturn(mockUserDocRef)
        whenever(mockUserDocRef.get()).thenReturn(Tasks.forResult(mockUserDocSnapshot))
        whenever(mockUserDocSnapshot.getString("userType")).thenReturn(testUserType)

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)
        whenever(mockConvosCollection.whereEqualTo("studentId", testUid)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

        // Create conversation documents
        whenever(mockConvoDocSnapshot.id).thenReturn("convo1")
        whenever(mockConvoDocSnapshot.getString("studentId")).thenReturn(testUid)
        whenever(mockConvoDocSnapshot.getString("tutorId")).thenReturn("uid523")
        whenever(mockConvoDocSnapshot.getString("studentName")).thenReturn("Zac")
        whenever(mockConvoDocSnapshot.getString("tutorName")).thenReturn("Bob")
        whenever(mockConvoDocSnapshot.getString("lastMessage")).thenReturn("Hello")
        whenever(mockConvoDocSnapshot.getLong("timestamp")).thenReturn(1743577730722L)
        whenever(mockConvoDocSnapshot.getBoolean("lastMessageReadByStudent")).thenReturn(false)
        whenever(mockConvoDocSnapshot.getBoolean("lastMessageReadByTutor")).thenReturn(true)

        val fakeDoc2: DocumentSnapshot = mock()
        whenever(fakeDoc2.id).thenReturn("convo2")
        whenever(fakeDoc2.getString("studentId")).thenReturn(testUid)
        whenever(fakeDoc2.getString("tutorId")).thenReturn("uid789")
        whenever(fakeDoc2.getString("studentName")).thenReturn("Zac")
        whenever(fakeDoc2.getString("tutorName")).thenReturn("Tim")
        whenever(fakeDoc2.getString("lastMessage")).thenReturn("Great!")
        whenever(fakeDoc2.getLong("timestamp")).thenReturn(2234567890L)
        whenever(fakeDoc2.getBoolean("lastMessageReadByStudent")).thenReturn(true)
        whenever(fakeDoc2.getBoolean("lastMessageReadByTutor")).thenReturn(false)

        whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockConvoDocSnapshot, fakeDoc2))

        val emissions = repository.fetchConversations().toList()

        // Response.Loading is emitted first
        assertTrue(emissions.first() is Response.Loading)

        // Response.Success is emitted last
        val success = emissions.last() as? Response.Success<List<Conversation>>

        assertNotNull(success)
        val convos = success?.data!!
        assertEquals(2, convos.size)

        // Verify fields of the conversations
        val firstConvo = convos.first { it.id == "convo1" }
        assertEquals(testUid, firstConvo.studentId)
        assertEquals("uid523", firstConvo.tutorId)
        assertEquals("Zac", firstConvo.studentName)
        assertEquals("Bob", firstConvo.tutorName)
        assertEquals("Hello", firstConvo.lastMessage)
        assertEquals(1743577730722L, firstConvo.timestamp)
        assertEquals(false, firstConvo.lastMessageReadByStudent)
        assertEquals(true, firstConvo.lastMessageReadByTutor)

        val secondConvo = convos.first { it.id == "convo2" }
        assertEquals(testUid, secondConvo.studentId)
        assertEquals("uid789", secondConvo.tutorId)
        assertEquals("Zac", secondConvo.studentName)
        assertEquals("Tim", secondConvo.tutorName)
        assertEquals("Great!", secondConvo.lastMessage)
        assertEquals(2234567890L, secondConvo.timestamp)
        assertEquals(true, secondConvo.lastMessageReadByStudent)
        assertEquals(false, secondConvo.lastMessageReadByTutor)
    }

    @Test
    fun `fetchConversations returns conversations for tutor type`() = runTest {
        val currentUid = "uid123"
        val currentUserType = "Tutor"
        val currentUserName = "Bob"

        whenever(mockAuth.currentUser).thenReturn(mockCurrentUser)
        whenever(mockCurrentUser.uid).thenReturn(currentUid)

        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(currentUid)).thenReturn(mockUserDocRef)
        whenever(mockUserDocRef.get()).thenReturn(Tasks.forResult(mockUserDocSnapshot))
        whenever(mockUserDocSnapshot.getString("userType")).thenReturn(currentUserType)

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)
        whenever(mockConvosCollection.whereEqualTo("tutorId", currentUid)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

        // Create conversation documents
        whenever(mockConvoDocSnapshot.id).thenReturn("convo1")
        whenever(mockConvoDocSnapshot.getString("studentId")).thenReturn(currentUid)
        whenever(mockConvoDocSnapshot.getString("tutorId")).thenReturn("uid523")
        whenever(mockConvoDocSnapshot.getString("studentName")).thenReturn("Timmy")
        whenever(mockConvoDocSnapshot.getString("tutorName")).thenReturn(currentUserName)
        whenever(mockConvoDocSnapshot.getString("lastMessage")).thenReturn("Hello there")
        whenever(mockConvoDocSnapshot.getLong("timestamp")).thenReturn(1743577730722L)
        whenever(mockConvoDocSnapshot.getBoolean("lastMessageReadByStudent")).thenReturn(false)
        whenever(mockConvoDocSnapshot.getBoolean("lastMessageReadByTutor")).thenReturn(true)

        val fakeDoc2: DocumentSnapshot = mock()
        whenever(fakeDoc2.id).thenReturn("convo2")
        whenever(fakeDoc2.getString("studentId")).thenReturn(currentUid)
        whenever(fakeDoc2.getString("tutorId")).thenReturn("uid789")
        whenever(fakeDoc2.getString("studentName")).thenReturn("Zac")
        whenever(fakeDoc2.getString("tutorName")).thenReturn(currentUserName)
        whenever(fakeDoc2.getString("lastMessage")).thenReturn("Great!")
        whenever(fakeDoc2.getLong("timestamp")).thenReturn(2234567890L)
        whenever(fakeDoc2.getBoolean("lastMessageReadByStudent")).thenReturn(true)
        whenever(fakeDoc2.getBoolean("lastMessageReadByTutor")).thenReturn(false)

        whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockConvoDocSnapshot, fakeDoc2))

        val emissions = repository.fetchConversations().toList()

        // Response.Loading is emitted first
        assertTrue(emissions.first() is Response.Loading)

        // Response.Success is emitted last
        val success = emissions.last() as? Response.Success<List<Conversation>>

        assertNotNull(success)
        val convos = success?.data!!
        assertEquals(2, convos.size)

        // Verify fields of the conversations
        val firstConvo = convos.first { it.id == "convo1" }
        assertEquals(currentUid, firstConvo.studentId)
        assertEquals("uid523", firstConvo.tutorId)
        assertEquals("Timmy", firstConvo.studentName)
        assertEquals(currentUserName, firstConvo.tutorName)
        assertEquals("Hello there", firstConvo.lastMessage)
        assertEquals(1743577730722L, firstConvo.timestamp)
        assertEquals(false, firstConvo.lastMessageReadByStudent)
        assertEquals(true, firstConvo.lastMessageReadByTutor)

        val secondConvo = convos.first { it.id == "convo2" }
        assertEquals(currentUid, secondConvo.studentId)
        assertEquals("uid789", secondConvo.tutorId)
        assertEquals("Zac", secondConvo.studentName)
        assertEquals(currentUserName, secondConvo.tutorName)
        assertEquals("Great!", secondConvo.lastMessage)
        assertEquals(2234567890L, secondConvo.timestamp)
        assertEquals(true, secondConvo.lastMessageReadByStudent)
        assertEquals(false, secondConvo.lastMessageReadByTutor)
    }

    @Test
    fun `fetchConversations returns empty list when no documents exist`() = runTest {
        val currentUid = "uid123"
        val currentUserType = "Student"

        whenever(mockAuth.currentUser).thenReturn(mockCurrentUser)
        whenever(mockCurrentUser.uid).thenReturn(currentUid)

        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(currentUid)).thenReturn(mockUserDocRef)
        whenever(mockUserDocRef.get()).thenReturn(Tasks.forResult(mockUserDocSnapshot))
        whenever(mockUserDocSnapshot.getString("userType")).thenReturn(currentUserType)

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)
        whenever(mockConvosCollection.whereEqualTo("studentId", currentUid)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

        whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

        val emissions = repository.fetchConversations().toList()
        assertTrue(emissions.first() is Response.Loading)
        val successResponse = emissions.last() as? Response.Success<List<Conversation>>
        assertNotNull(successResponse)
        assertEquals(0, successResponse?.data!!.size)
    }

    @Test
    fun `fetchConversations returns error when firestore query fails`() = runTest {
        val currentUid = "uid123"
        val currentUserType = "Student"

        whenever(mockAuth.currentUser).thenReturn(mockCurrentUser)
        whenever(mockCurrentUser.uid).thenReturn(currentUid)

        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(currentUid)).thenReturn(mockUserDocRef)
        whenever(mockUserDocRef.get()).thenReturn(Tasks.forResult(mockUserDocSnapshot))
        whenever(mockUserDocSnapshot.getString("userType")).thenReturn(currentUserType)

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)
        whenever(mockConvosCollection.whereEqualTo("studentId", currentUid)).thenReturn(mockQuery)

        whenever(mockQuery.get()).thenThrow(RuntimeException("Firestore query error"))

        val emissions = repository.fetchConversations().toList()
        assertTrue(emissions.first() is Response.Loading)

        val errorResponse = emissions.last() as? Response.Error
        assertNotNull(errorResponse)
        assertTrue(errorResponse?.message!!.contains("Firestore query error"))
    }

    @Test
    fun `fetchUsers returns list of users for student current user`() = runTest {
        val currentUserId = "uid123"
        val currentUserType = "Student"
        val oppositeType = "Tutor"

        val fakeUrl1 = "https://test.firebase.com/download1.jpg"
        val mockUri1 = mock(Uri::class.java)

        // Clear cached users
        repository.clearCache()

        whenever(mockAuth.currentUser).thenReturn(mockCurrentUser)
        whenever(mockCurrentUser.uid).thenReturn(currentUserId)

        // Mock Firestore collections
        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(currentUserId)).thenReturn(mockUserDocRef)
        whenever(mockUserDocRef.get()).thenReturn(Tasks.forResult(mockUserDocSnapshot))
        whenever(mockUserDocSnapshot.getString("userType")).thenReturn(currentUserType)

        whenever(mockUsersCollection.whereEqualTo("userType", oppositeType)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

        // Mock user documents
        val mockUserDoc1: DocumentSnapshot = mock()
        whenever(mockUserDoc1.id).thenReturn("uid222")
        whenever(mockUserDoc1.getString("firstName")).thenReturn("Billy")
        whenever(mockUserDoc1.getString("lastName")).thenReturn("Bob")
        whenever(mockUserDoc1.getString("userType")).thenReturn(oppositeType)

        val mockUserDoc2: DocumentSnapshot = mock()
        whenever(mockUserDoc2.id).thenReturn("uid333")
        whenever(mockUserDoc2.getString("firstName")).thenReturn("John")
        whenever(mockUserDoc2.getString("lastName")).thenReturn("Smith")
        whenever(mockUserDoc2.getString("userType")).thenReturn(oppositeType)

        whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockUserDoc1, mockUserDoc2))

        val mockRootStorageRef: StorageReference = mock()
        whenever(mockStorage.reference).thenReturn(mockRootStorageRef)

        // Create storage references for each user
        val storageRefUser1: StorageReference = mock()
        val storageRefUser2: StorageReference = mock()
        whenever(mockStorageRef.child("uid222/profilePicture.jpg")).thenReturn(storageRefUser1)
        whenever(storageRefUser1.metadata).thenReturn(Tasks.forResult(null))
        whenever(storageRefUser1.downloadUrl).thenReturn(Tasks.forResult(mockUri1))
        whenever(mockUri1.toString()).thenReturn(fakeUrl1)

        // User 2 does not have a custom profile picture
        whenever(mockStorageRef.child("uid333/profilePicture.jpg")).thenReturn(storageRefUser2)
        val storageException = mock<StorageException>()
        whenever(storageException.errorCode).thenReturn(StorageException.ERROR_OBJECT_NOT_FOUND)
        whenever(storageRefUser2.metadata).thenThrow(storageException)

        val emissions = repository.fetchUsers().toList()

         // Response.Loading is emitted first
        assertTrue(emissions.first() is Response.Loading)

        // Response.Success is emitted last
        val successResponse = emissions.last() as? Response.Success<List<User>>
        assertNotNull(successResponse)
        val users = successResponse?.data!!

        assertEquals(2, users.size)

        // Verify users
        val user1 = users.first { it.id == "uid222" }
        assertEquals("Billy", user1.firstName)
        assertEquals("Bob", user1.lastName)
        assertEquals(oppositeType, user1.userType)
        assertEquals(fakeUrl1, user1.userProfilePictureUrl)

        val user2 = users.first { it.id == "uid333" }
        assertEquals("John", user2.firstName)
        assertEquals("Smith", user2.lastName)
        assertEquals(oppositeType, user2.userType)
        assertEquals(null, user2.userProfilePictureUrl)
    }

    @Test
    fun `fetchUsers returns error when firestore query fails`() = runTest {
        val currentUserId = "uid123"
        val currentUserType = "Student"

        whenever(mockAuth.currentUser).thenReturn(mockCurrentUser)
        whenever(mockCurrentUser.uid).thenReturn(currentUserId)

        whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(currentUserId)).thenReturn(mockUserDocRef)
        whenever(mockUserDocRef.get()).thenReturn(Tasks.forResult(mockUserDocSnapshot))
        whenever(mockUserDocSnapshot.getString("userType")).thenReturn(currentUserType)

        whenever(mockUsersCollection.whereEqualTo("userType", "Tutor")).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenThrow(RuntimeException("Firestore query error"))

        val emissions = repository.fetchUsers().toList()

        assertTrue(emissions.first() is Response.Loading)
        val errorResponse = emissions.last() as? Response.Error
        assertNotNull(errorResponse)
        assertTrue(errorResponse?.message!!.contains("Firestore query error"))
    }

    @Test
    fun `fetchProfilePictures returns download URLs when storage calls succeed`() = runTest {
        val userIds = listOf("uid123", "uid456")
        val fakeUrl1 = "https://test.firebase.com/download1.jpg"
        val fakeUrl2 = "https://test.firebase.com/download2.jpg"
        val mockUri1 = mock(Uri::class.java)
        val mockUri2 = mock(Uri::class.java)

        val storageRefUser1: StorageReference = mock()
        val storageRefUser2: StorageReference = mock()

        whenever(mockStorage.reference).thenReturn(mockStorageRef)
        whenever(mockStorage.reference.child("uid123/profilePicture.jpg")).thenReturn(storageRefUser1)
        whenever(mockStorage.reference.child("uid456/profilePicture.jpg")).thenReturn(storageRefUser2)

        // Assume metadata call is successful (metadata value is not used)
        whenever(storageRefUser1.metadata).thenReturn(Tasks.forResult(null))
        whenever(storageRefUser2.metadata).thenReturn(Tasks.forResult(null))

        // Create Uris for each user
        whenever(storageRefUser1.downloadUrl).thenReturn(Tasks.forResult(mockUri1))
        whenever(storageRefUser2.downloadUrl).thenReturn(Tasks.forResult(mockUri2))
        whenever(mockUri1.toString()).thenReturn(fakeUrl1)
        whenever(mockUri2.toString()).thenReturn(fakeUrl2)

        val result = repository.fetchProfilePictures(userIds)

        assertEquals(mockUri1.toString(), result["uid123"])
        assertEquals(mockUri2.toString(), result["uid456"])
    }


    @Test
    fun `updateConversationStatus returns success for Student`() = runTest {
        val conversationId = "convo123"

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)
        whenever(mockConvosCollection.document(conversationId)).thenReturn(mockConvoDocRef)
        whenever(mockConvoDocRef.update("lastMessageReadByStudent", true))
            .thenReturn(Tasks.forResult(null))

        val result = repository.updateConversationStatus(conversationId, "Student")

        assertTrue(result is Response.Success)
    }

    @Test
    fun `updateConversationStatus returns error when update fails`() = runTest {
        val conversationId = "convo123"
        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)
        whenever(mockConvosCollection.document(conversationId)).thenReturn(mockConvoDocRef)
        whenever(mockConvoDocRef.update("lastMessageReadByStudent", true))
            .thenThrow(RuntimeException("Update failed"))

        val result = repository.updateConversationStatus(conversationId, "Student")

        assertTrue(result is Response.Error)

        if (result is Response.Error) {
            assertTrue(result.message.contains("Update failed"))
        }
    }

    @Test
    fun `fetchConversationId returns conversation id from query if conversation exists`() = runTest {
        val currentUserId = "uid1"
        val selectedUserId = "uid2"
        val expectedConvId = "convo1"
        val mockQuery2: Query = mock()
        val mockQuerySnapshot2: QuerySnapshot = mock()

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)

        // Query 1: studentId equals currentUserId and tutorId equals selectedUserId
        whenever(mockConvosCollection.whereEqualTo("studentId", currentUserId)).thenReturn(mockQuery)
        whenever(mockQuery.whereEqualTo("tutorId", selectedUserId)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

        whenever(mockConvoDocSnapshot.id).thenReturn(expectedConvId)
        whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockConvoDocSnapshot))

        // Query 2 returns empty snapshot
        whenever(mockConvosCollection.whereEqualTo("tutorId", currentUserId)).thenReturn(mockQuery2)
        whenever(mockQuery2.whereEqualTo("studentId", selectedUserId)).thenReturn(mockQuery2)
        whenever(mockQuery2.get()).thenReturn(Tasks.forResult(mockQuerySnapshot2))
        whenever(mockQuerySnapshot2.documents).thenReturn(emptyList())

        val result = repository.fetchConversationId(selectedUserId, currentUserId)

        assertTrue(result is Response.Success)
        val convoId = (result as? Response.Success)?.data
        assertEquals(expectedConvId, convoId)
    }

    @Test
    fun `fetchConversationId returns success with null when no conversation exists`() = runTest {
        val currentUserId = "uid1"
        val selectedUserId = "uid2"

        val mockQuery2: Query = mock()
        val mockQuerySnapshot2: QuerySnapshot = mock()

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)
        whenever(mockConvosCollection.whereEqualTo("studentId", currentUserId)).thenReturn(mockQuery)
        whenever(mockQuery.whereEqualTo("tutorId", selectedUserId)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
        whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

        whenever(mockConvosCollection.whereEqualTo("tutorId", currentUserId)).thenReturn(mockQuery2)
        whenever(mockQuery2.whereEqualTo("studentId", selectedUserId)).thenReturn(mockQuery2)
        whenever(mockQuery2.get()).thenReturn(Tasks.forResult(mockQuerySnapshot2))
        whenever(mockQuerySnapshot2.documents).thenReturn(emptyList())

        val result = repository.fetchConversationId(selectedUserId, currentUserId)

        assertTrue(result is Response.Success)
        val convoId = (result as? Response.Success)?.data
        assertEquals(null, convoId)
    }

    @Test
    fun `fetchConversationId returns error when firestore query fails`() = runTest {
        val currentUserId = "uid1"
        val selectedUserId = "uid2"

        whenever(mockFirestore.collection("conversations")).thenReturn(mockConvosCollection)
        whenever(mockConvosCollection.whereEqualTo("studentId", currentUserId)).thenReturn(mockQuery)
        whenever(mockQuery.whereEqualTo("tutorId", selectedUserId)).thenReturn(mockQuery)
        whenever(mockQuery.get()).thenThrow(RuntimeException("Query failed"))

        val result = repository.fetchConversationId(selectedUserId, currentUserId)

        assertTrue(result is Response.Error)
        if (result is Response.Error) {
            assertTrue(result.message.contains("Query failed"))
        }
    }


}