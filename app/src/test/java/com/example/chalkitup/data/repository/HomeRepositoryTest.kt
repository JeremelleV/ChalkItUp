@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.chalkitup.data.repository

import com.example.chalkitup.MainDispatcherRule
import com.example.chalkitup.domain.model.Appointment
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
import io.mockk.*
import junit.framework.Assert.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val mockDb = mockk<FirebaseFirestore>(relaxed = true)
    private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockStorage = mockk<FirebaseStorage>(relaxed = true)
    private lateinit var repo: HomeRepository

    @Before
    fun setUp() {
        repo = HomeRepository(mockDb, mockAuth, mockStorage)
    }

    @Test
    fun `getUserNameAndType returns correct name and type`() = runTest {
        val uid = "user1"
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        val userDocRef = mockk<DocumentReference>(relaxed = true)
        val snapshot = mockk<DocumentSnapshot>(relaxed = true)

        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns uid
        every { mockDb.collection("users").document(uid) } returns userDocRef
        every { userDocRef.get() } returns Tasks.forResult(snapshot)
        every { snapshot.getString("firstName") } returns "Alice"
        every { snapshot.getString("userType") } returns "Tutor"

        val result = repo.getUserNameAndType()

        assertTrue(result.isSuccess)
        assertEquals(Pair("Alice", "Tutor"), result.getOrNull())
    }

    @Test
    fun `fetchBookedDates returns list of dates`() = runTest {
        val uid = "user123"
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        val appointmentsCol = mockk<CollectionReference>(relaxed = true)
        val doc1 = mockk<DocumentSnapshot>(relaxed = true)
        val doc2 = mockk<DocumentSnapshot>(relaxed = true)
        val studentQuery = mockk<Query>(relaxed = true)
        val tutorQuery = mockk<Query>(relaxed = true)
        val studentSnap = mockk<QuerySnapshot>()
        val tutorSnap = mockk<QuerySnapshot>()

        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns uid
        every { mockDb.collection("appointments") } returns appointmentsCol

        every { appointmentsCol.whereEqualTo("studentID", uid) } returns studentQuery
        every { appointmentsCol.whereEqualTo("tutorID", uid) } returns tutorQuery

        every { studentQuery.get() } returns Tasks.forResult(studentSnap)
        every { tutorQuery.get() } returns Tasks.forResult(tutorSnap)

        every { doc1.getString("date") } returns "\"2025-04-10\""
        every { doc2.getString("date") } returns "\"2025-04-15\""
        every { studentSnap.documents } returns listOf(doc1)
        every { tutorSnap.documents } returns listOf(doc2)

        val result = repo.fetchBookedDates()

        assertTrue(result.isSuccess)
        assertEquals(listOf("2025-04-10", "2025-04-15"), result.getOrNull())
    }

    @Test
    fun `fetchAppointments returns sorted list of appointments`() = runTest {
        val uid = "u1"
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        val appointmentsCol = mockk<CollectionReference>(relaxed = true)
        val usersCol = mockk<CollectionReference>(relaxed = true)
        val doc = mockk<DocumentSnapshot>(relaxed = true)
        val tutorDocSnap = mockk<DocumentSnapshot>(relaxed = true)
        val studentDocSnap = mockk<DocumentSnapshot>(relaxed = true)
        val tutorDocRef = mockk<DocumentReference>(relaxed = true)
        val studentDocRef = mockk<DocumentReference>(relaxed = true)
        val tutorQuery = mockk<Query>(relaxed = true)
        val tutorSnap = mockk<QuerySnapshot>()

        val futureDate = LocalDate.now().plusDays(1).toString()

        val appointment = Appointment(
            appointmentID = "id1",
            tutorID = "t1",
            studentID = "s1",
            date = futureDate,
            time = "10:00 AM - 10:30 AM",
            subjectObject = mapOf("subject" to "Math", "grade" to "10", "specialization" to "", "price" to "40"),
            mode = "Online",
            tutorName = "",
            studentName = "",
            comments = "None"
        )

        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns uid
        every { mockDb.collection("appointments") } returns appointmentsCol
        every { mockDb.collection("users") } returns usersCol

        every { appointmentsCol.whereEqualTo("studentID", uid).get() } returns Tasks.forResult(mockk(relaxed = true) {
            every { documents } returns emptyList()
        })

        every { appointmentsCol.whereEqualTo("tutorID", uid) } returns tutorQuery
        every { tutorQuery.get() } returns Tasks.forResult(tutorSnap)
        every { tutorSnap.documents } returns listOf(doc)

        every { doc.id } returns "id1"
        every { doc.toObject(Appointment::class.java) } returns appointment

        every { usersCol.document("t1") } returns tutorDocRef
        every { tutorDocRef.get() } returns Tasks.forResult(tutorDocSnap)
        every { tutorDocSnap.getString("firstName") } returns "Tutor"
        every { tutorDocSnap.getString("lastName") } returns "Name"

        every { usersCol.document("s1") } returns studentDocRef
        every { studentDocRef.get() } returns Tasks.forResult(studentDocSnap)
        every { studentDocSnap.getString("firstName") } returns "Student"
        every { studentDocSnap.getString("lastName") } returns "Name"

        val result = repo.fetchAppointments()

        assertTrue(result.isSuccess)
        val appointments = result.getOrNull()!!
        assertEquals(1, appointments.size)
        assertEquals("Tutor Name", appointments[0].tutorName)
        assertEquals("Student Name", appointments[0].studentName)
    }
}
