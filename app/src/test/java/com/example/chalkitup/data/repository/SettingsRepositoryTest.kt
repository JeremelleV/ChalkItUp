package com.example.chalkitup.data.repository

import com.example.chalkitup.CoroutineTestRule
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SettingsRepositoryTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockDb = mock<FirebaseFirestore>()
    private val mockAuth = mock<FirebaseAuth>()
    private val mockUser = mock<FirebaseUser>()

    private lateinit var repo: SettingsRepository

    @Before
    fun setUp() {
        repo = SettingsRepository(mockDb, mockAuth)
    }

    @Test
    fun `getEmail returns current user's email`() {
        whenever(mockAuth.currentUser).thenReturn(mockUser)
        whenever(mockUser.email).thenReturn("test@example.com")

        val result = repo.getEmail()
        assertEquals("test@example.com", result)
    }

    @Test
    fun `getEmail returns null when user is not signed in`() {
        whenever(mockAuth.currentUser).thenReturn(null)

        val result = repo.getEmail()
        assertNull(result)
    }

    @Test
    fun `deleteAccount fails when user is not signed in`() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        val result = repo.deleteAccount()
        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteAccount marks user as deleted and deletes auth user`() = runTest {
        val uid = "user123"
        val userDoc = mock<DocumentReference>()
        val usersColl = mock<CollectionReference>()

        whenever(mockAuth.currentUser).thenReturn(mockUser)
        whenever(mockUser.uid).thenReturn(uid)
        whenever(mockDb.collection("users")).thenReturn(usersColl)
        whenever(usersColl.document(uid)).thenReturn(userDoc)
        whenever(userDoc.update(any<Map<String, Any>>()))
            .thenReturn(Tasks.forResult(null))
        whenever(mockUser.delete()).thenReturn(Tasks.forResult(null))

        val result = repo.deleteAccount()

        assertTrue(result.isSuccess)
        verify(userDoc).update(
            mapOf(
                "firstName" to "deleted user",
                "lastName" to "",
                "active" to false
            )
        )
        verify(mockUser).delete()
    }
}
