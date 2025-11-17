package com.example.chalkitup.data.repository

import com.example.chalkitup.ui.components.TutorSubject
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class BookingRepositoryTest {

    @Mock
    lateinit var firestore: FirebaseFirestore
    @Mock lateinit var auth: FirebaseAuth
    @Mock lateinit var documentRef: DocumentReference
    @Mock lateinit var collectionRef: CollectionReference
    @Mock lateinit var documentSnapshot: DocumentSnapshot
    @Mock lateinit var querySnapshot: QuerySnapshot
    @Mock lateinit var task: Task<DocumentSnapshot>
    @Mock lateinit var currentUser: FirebaseUser

    private lateinit var repository: BookingRepository

    @Before
    fun setUp() {
        repository = BookingRepository(firestore, auth)
    }

    @Test
    fun `getCurrentUserId returns UID when user is logged in`() {
        Mockito.`when`(auth.currentUser).thenReturn(currentUser)
        Mockito.`when`(currentUser.uid).thenReturn("testUid")

        val result = repository.getCurrentUserId()

        assertEquals("testUid", result)
    }

    @Test
    fun `getCurrentUserId returns null when user is not logged in`() {
        Mockito.`when`(auth.currentUser).thenReturn(null)

        val result = repository.getCurrentUserId()

        assertNull(result)
    }

    @Test
    fun `fetchUserInfo returns success when document exists`() = runTest {
        val userId = "user123"
        val docRef = mock(DocumentReference::class.java)
        val userCollection = mock(CollectionReference::class.java)

        whenever(firestore.collection("users")).thenReturn(userCollection)
        whenever(userCollection.document(userId)).thenReturn(docRef)
        whenever(docRef.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentSnapshot.getString("userType")).thenReturn("Tutor")
        whenever(documentSnapshot.getString("firstName")).thenReturn("John")
        whenever(documentSnapshot.getString("email")).thenReturn("john@example.com")

        val result = repository.fetchUserInfo(userId)

        assertTrue(result.isSuccess)
        assertEquals("Tutor", result.getOrNull()?.userType)
    }

    @Test
    fun `fetchUserInfo returns failure when document does not exist`() = runTest {
        val userId = "user123"
        val userCollection = mock(CollectionReference::class.java)
        val docRef = mock(DocumentReference::class.java)

        whenever(firestore.collection("users")).thenReturn(userCollection)
        whenever(userCollection.document(userId)).thenReturn(docRef)
        whenever(docRef.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(false)

        val result = repository.fetchUserInfo(userId)

        assertTrue(result.isFailure)
    }

    @Test
    fun `fetchUserActiveStatus returns true when active`() = runTest {
        val userId = "uid123"
        whenever(auth.currentUser).thenReturn(currentUser)
        whenever(currentUser.uid).thenReturn(userId)

        val userCollection = mock(CollectionReference::class.java)
        val docRef = mock(DocumentReference::class.java)

        whenever(firestore.collection("users")).thenReturn(userCollection)
        whenever(userCollection.document(userId)).thenReturn(docRef)
        whenever(docRef.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.getBoolean("active")).thenReturn(true)

        val result = repository.fetchUserActiveStatus()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun `fetchUserFullName returns full name`() = runTest {
        val userId = "user123"
        val userCollection = mock(CollectionReference::class.java)
        val docRef = mock(DocumentReference::class.java)

        whenever(firestore.collection("users")).thenReturn(userCollection)
        whenever(userCollection.document(userId)).thenReturn(docRef)
        whenever(docRef.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.getString("firstName")).thenReturn("Anna")
        whenever(documentSnapshot.getString("lastName")).thenReturn("Lee")

        val result = repository.fetchUserFullName(userId)

        assertTrue(result.isSuccess)
        assertEquals("Anna Lee", result.getOrNull())
    }

    @Test
    fun `updateSessionCount increments week count`() = runTest {
        val tutorId = "t123"
        val yearMonth = "2024-04"
        val weekNumber = 2

        // 1. Mock full path availability/yearMonth/tutorId/sessionCount
        val availabilityCollection = mock<CollectionReference>()
        val yearMonthDoc = mock<DocumentReference>()
        val tutorCollection = mock<CollectionReference>()
        val sessionCountDoc = mock<DocumentReference>()

        // 2. Return mocks
        whenever(firestore.collection("availability")).thenReturn(availabilityCollection)
        whenever(availabilityCollection.document(yearMonth)).thenReturn(yearMonthDoc)
        whenever(yearMonthDoc.collection(tutorId)).thenReturn(tutorCollection)
        whenever(tutorCollection.document("sessionCount")).thenReturn(sessionCountDoc)

        // 3. Properly stub update() to return a Task<Void>
        val mockTask: Task<Void> = Tasks.forResult(null)
        whenever(sessionCountDoc.update(eq("week$weekNumber"), any())).thenReturn(mockTask)

        // 4. Call the real method
        val result = repository.updateSessionCount(tutorId, yearMonth, weekNumber)

        // 5. Assert success
        assertTrue("Expected success but got ${result.exceptionOrNull()}", result.isSuccess)
    }

    @Test
    fun `sendEmail sends booking confirmation to both user and tutor`() = runTest {
        val tutorId = "t123"
        val fName = "Student"
        val tutorName = "Tutor"
        val subject = TutorSubject("Math", "9", "Algebra", "$40")
        val date = "2025-04-10"
        val time = "3:00 PM - 4:00 PM"
        val price = "$40"
        val userEmail = "student@example.com"

        val usersColl = mock(CollectionReference::class.java)
        whenever(firestore.collection("users")).thenReturn(usersColl)
        whenever(usersColl.document(tutorId)).thenReturn(documentRef)
        whenever(documentRef.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.getString("email")).thenReturn("tutor@example.com")

        val mailColl = mock(CollectionReference::class.java)
        whenever(firestore.collection("mail")).thenReturn(mailColl)
        whenever(mailColl.add(any())).thenReturn(Tasks.forResult(mock()))

        val result = repository.sendEmail(
            tutorId, fName, tutorName, subject, date, time, price, userEmail
        )

        assertTrue(result.isSuccess)
        verify(mailColl, times(2)).add(any())
    }

    @Test
    fun `fetchTutorPriceForSubject returns correct price`() = runTest {
        val tutorId = "t123"
        val subject = TutorSubject("Math", "9", "Algebra", "")

        val subjectData = listOf(
            mapOf("subject" to "Math", "grade" to "9", "specialization" to "Algebra", "price" to "$35")
        )

        val usersColl = mock(CollectionReference::class.java)
        whenever(firestore.collection("users")).thenReturn(usersColl)
        whenever(usersColl.document(tutorId)).thenReturn(documentRef)
        whenever(documentRef.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.get("subjects")).thenReturn(subjectData)

        val result = repository.fetchTutorPriceForSubject(tutorId, subject)

        assertTrue(result.isSuccess)
        assertEquals("$35", result.getOrNull())
    }

    @Test
    fun `fetchUserActiveStatus returns success when user is active`() = runTest {
        Mockito.`when`(auth.currentUser).thenReturn(currentUser)
        Mockito.`when`(currentUser.uid).thenReturn("userId")
        val userDocRef = mock(DocumentReference::class.java)
        val userCollection = mock(CollectionReference::class.java)
        Mockito.`when`(firestore.collection("users")).thenReturn(userCollection)
        Mockito.`when`(userCollection.document("userId")).thenReturn(userDocRef)
        Mockito.`when`(userDocRef.get()).thenReturn(Tasks.forResult(documentSnapshot))
        Mockito.`when`(documentSnapshot.getBoolean("active")).thenReturn(true)

        val result = repository.fetchUserActiveStatus()

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `getSessionCountForWeek returns correct count`() = runTest {
        val tutorId = "t456"
        val yearMonth = "2024-04"
        val weekNumber = 2
        val docRef = mock(DocumentReference::class.java)
        val availabilityCollection = mock(CollectionReference::class.java)
        val tutorCollection = mock(CollectionReference::class.java)
        Mockito.`when`(firestore.collection("availability")).thenReturn(availabilityCollection)
        Mockito.`when`(availabilityCollection.document(yearMonth)).thenReturn(documentRef)
        Mockito.`when`(documentRef.collection(tutorId)).thenReturn(tutorCollection)
        Mockito.`when`(tutorCollection.document("sessionCount")).thenReturn(docRef)
        Mockito.`when`(docRef.get()).thenReturn(Tasks.forResult(documentSnapshot))
        Mockito.`when`(documentSnapshot.getLong("week2")).thenReturn(3L)

        val result = repository.getSessionCountForWeek(tutorId, yearMonth, weekNumber)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull())
    }

}
