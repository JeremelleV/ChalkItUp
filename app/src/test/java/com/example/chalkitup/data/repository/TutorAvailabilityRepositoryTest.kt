package com.example.chalkitup.data.repository

import com.example.chalkitup.MainDispatcherRule
import com.example.chalkitup.domain.model.TutorAvailabilityWrapper
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TutorAvailabilityRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val mockFirestore = mock<FirebaseFirestore>()
    private val mockAuth = mock<FirebaseAuth>()
    private val mockUser = mock<FirebaseUser>()

    private val mockDocumentSnapshot = mock<DocumentSnapshot>()
    private val mockDocumentReference = mock<DocumentReference>()
    private val mockCollectionReference = mock<CollectionReference>()

    private lateinit var repository: TutorAvailabilityRepository

    @Before
    fun setUp() {
        whenever(mockAuth.currentUser).thenReturn(mockUser)
        whenever(mockUser.uid).thenReturn("testTutor")
        repository = TutorAvailabilityRepository(mockFirestore, mockAuth)
    }

    @Test
    fun `getCurrentUserId returns correct uid`() {
        val result = repository.getCurrentUserId()
        assertEquals("testTutor", result)
    }

    @Test
    fun `initializeSessionCount sets data if document does not exist`() = runTest {
        val tutorId = "testTutor"
        val monthYear = "2024-04"

        whenever(mockFirestore.collection("availability")).thenReturn(mockCollectionReference)
        whenever(mockCollectionReference.document(monthYear)).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.collection(tutorId)).thenReturn(mockCollectionReference)
        whenever(mockCollectionReference.document("sessionCount")).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
        whenever(mockDocumentSnapshot.exists()).thenReturn(false)
        whenever(
            mockDocumentReference.set(
                mapOf(
                    "week1" to 0,
                    "week2" to 0,
                    "week3" to 0,
                    "week4" to 0,
                    "week5" to 0
                )
            )
        ).thenReturn(Tasks.forResult(null))

        val result = repository.initializeSessionCount(tutorId, monthYear)

        assertTrue("Expected success but got failure: ${result.exceptionOrNull()}", result.isSuccess)
    }

    @Test
    fun `saveAvailability stores the availability data successfully`() = runTest {
        val tutorId = "testTutor"
        val monthYear = "2024-04"
        val wrapper = TutorAvailabilityWrapper(listOf())

        whenever(mockFirestore.collection("availability")).thenReturn(mockCollectionReference)
        whenever(mockCollectionReference.document(monthYear)).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.collection(tutorId)).thenReturn(mockCollectionReference)
        whenever(mockCollectionReference.document("availabilityData")).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.set(wrapper)).thenReturn(Tasks.forResult(null))

        val result = repository.saveAvailability(tutorId, monthYear, wrapper)

        assertTrue("Expected success but got failure: ${result.exceptionOrNull()}", result.isSuccess)
    }
}
