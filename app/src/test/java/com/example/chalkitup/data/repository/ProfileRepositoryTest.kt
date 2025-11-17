package com.example.chalkitup.data.repository

import org.mockito.Mockito.*
import kotlinx.coroutines.test.runTest
import android.net.Uri
import com.example.chalkitup.CoroutineTestRule
import com.example.chalkitup.domain.model.UserProfile
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProfileRepositoryTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockFirestore = mock<FirebaseFirestore>()
    private val mockAuth = mock<FirebaseAuth>()
    private val mockStorage = mock<FirebaseStorage>()
    private lateinit var repo: ProfileRepository

    @Before
    fun setUp() {
        repo = ProfileRepository(mockFirestore, mockAuth, mockStorage)
    }

    @Test
    fun `getCurrentUserId returns current uid`() {
        val mockUser = mock(FirebaseUser::class.java)
        `when`(mockUser.uid).thenReturn("uid123")

        whenever(mockAuth.currentUser).thenReturn(mockUser)

        val result = repo.getCurrentUserId()
        assertEquals("uid123", result)
    }

    @Test
    fun `fetchUserProfile returns UserProfile`() = runTest {
        val userId = "uid123"
        val mockDocRef = mock<DocumentReference>()
        val mockSnap = mock<DocumentSnapshot>()
        val user = UserProfile(
            userType = "Tutor",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com"
        )

        val usersCol = mock<CollectionReference>()
        whenever(mockFirestore.collection("users")).thenReturn(usersCol)
        whenever(usersCol.document(userId)).thenReturn(mockDocRef)
        whenever(mockDocRef.get()).thenReturn(Tasks.forResult(mockSnap))
        whenever(mockSnap.toObject(UserProfile::class.java)).thenReturn(user)

        val result = repo.fetchUserProfile(userId)
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun `loadProfilePicture returns download URL`() = runTest {
        val userId = "uid123"
        val fakeUrl = "https://test.firebase.com/download.jpg"

        val mockStorageRef = mock(StorageReference::class.java)
        val mockUserStorageRef = mock(StorageReference::class.java)
        val mockUri = mock(Uri::class.java)

        `when`(mockStorage.reference).thenReturn(mockStorageRef)
        `when`(mockStorageRef.child("$userId/profilePicture.jpg")).thenReturn(mockUserStorageRef)
        `when`(mockUserStorageRef.downloadUrl).thenReturn(Tasks.forResult(mockUri))
        `when`(mockUri.toString()).thenReturn(fakeUrl)

        val result = repo.loadProfilePicture(userId)

        assertTrue(result.isSuccess)
        assertEquals(fakeUrl, result.getOrNull())
    }

    @Test
    fun `reportUser adds report to Firestore`() = runTest {
        val userId = "u456"
        val message = "Inappropriate behavior"
        val reportsCol = mock<CollectionReference>()
        whenever(mockFirestore.collection("reports")).thenReturn(reportsCol)
        whenever(reportsCol.add(any())).thenReturn(Tasks.forResult(mock()))

        val result = repo.reportUser(userId, message)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateTutorStats updates user fields`() = runTest {
        val userId = "uid123"
        val docRef = mock<DocumentReference>()
        val usersCol = mock<CollectionReference>()
        whenever(mockFirestore.collection("users")).thenReturn(usersCol)
        whenever(usersCol.document(userId)).thenReturn(docRef)
        whenever(docRef.update(any<Map<String, Any>>())).thenReturn(Tasks.forResult(null))

        val result = repo.updateTutorStats(userId, 10, 4.5)
        assertTrue(result.isSuccess)
    }
}
