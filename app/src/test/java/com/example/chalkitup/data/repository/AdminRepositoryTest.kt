package com.example.chalkitup.data.repository

import com.example.chalkitup.CoroutineTestRule
import com.example.chalkitup.ui.viewmodel.admin.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AdminRepositoryTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockDb = mock<FirebaseFirestore>()
    private val mockAuth = mock<FirebaseAuth>()
    private val mockStorage = mock<FirebaseStorage>()
    private lateinit var repo: AdminRepository

    @Before
    fun setUp() {
        repo = AdminRepository(mockDb, mockAuth, mockStorage)
    }

    @Test
    fun `getApprovedTutors emits empty list when no user signed in`() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        val result = repo.getApprovedTutors().first()
        assert(result.isEmpty())
    }

    @Test
    fun `getReports emits empty list when no user signed in`() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        val result = repo.getReports().first()
        assert(result.isEmpty())
    }

    @Test
    fun `getUsersWithReports emits users parsed from report documents`() = runTest {
        val currentUser = mock<FirebaseUser>()
        whenever(mockAuth.currentUser).thenReturn(currentUser)
        whenever(currentUser.uid).thenReturn("adminUser")

        // Mock reports snapshot with one userId
        val reportDoc = mock<DocumentSnapshot>()
        whenever(reportDoc.getString("userId")).thenReturn("tutorUser1")

        val reportSnap = mock<QuerySnapshot>()
        whenever(reportSnap.documents).thenReturn(listOf(reportDoc))

        val reportsColl = mock<CollectionReference>()
        whenever(mockDb.collection("reports")).thenReturn(reportsColl)
        whenever(reportsColl.get()).thenReturn(Tasks.forResult(reportSnap))

        // Mock users snapshot
        val userDoc = mock<DocumentSnapshot>()
        whenever(userDoc.toObject(User::class.java)).thenReturn(User().apply {
            firstName = "Bob"
            id = "tutorUser1"
        })

        val usersColl = mock<CollectionReference>()
        val userRef = mock<DocumentReference>()
        whenever(mockDb.collection("users")).thenReturn(usersColl)
        whenever(usersColl.document("tutorUser1")).thenReturn(userRef)
        whenever(userRef.get()).thenReturn(Tasks.forResult(userDoc))

        val users = repo.getUsersWithReports().first()

        assertEquals(1, users.size)
        assertEquals("Bob", users[0].firstName)
        assertEquals("tutorUser1", users[0].id)
    }

    @Test
    fun `getUnapprovedTutors emits empty list when no user signed in`() = runTest {
        whenever(mockAuth.currentUser).thenReturn(null)

        val result = repo.getUnapprovedTutors().first()
        assert(result.isEmpty())
    }

    @Test
    fun `mapSnapshotsToUsers maps docs correctly`() {
        val doc = mock<DocumentSnapshot> {
            on { toObject(User::class.java) } doReturn User().apply {
                firstName = "Alice"
                lastName = "Smith"
                id = "u1"
            }
            on { id } doReturn "u1"
        }

        val result = repo.mapSnapshotsToUsers(listOf(doc))
        assertEquals(1, result.size)
        assertEquals("Alice", result[0].firstName)
        assertEquals("u1", result[0].id)
    }


    @Test
    fun `approveTutor updates user and sends email and notification`() = runTest {
        val tutorId = "t1"
        // users collection
        val usersColl = mock<CollectionReference>()
        val userDoc = mock<DocumentReference>()
        whenever(mockDb.collection("users")).thenReturn(usersColl)
        whenever(usersColl.document(tutorId)).thenReturn(userDoc)
        whenever(userDoc.update(mapOf("adminApproved" to true, "active" to true)))
            .thenReturn(Tasks.forResult(null))
        // get user
        val snap = mock<DocumentSnapshot>()
        whenever(userDoc.get()).thenReturn(Tasks.forResult(snap))
        whenever(snap.toObject(User::class.java)).thenReturn(User().apply { id=tutorId })

        // mail collection
        val mailColl = mock<CollectionReference>()
        whenever(mockDb.collection("mail")).thenReturn(mailColl)
        whenever(mailColl.add(any())).thenReturn(Tasks.forResult(mock()))

        // notifications collection
        val notifColl = mock<CollectionReference>()
        whenever(mockDb.collection("notifications")).thenReturn(notifColl)
        whenever(notifColl.add(any<Map<String, Any>>())).thenReturn(Tasks.forResult(mock()))

        repo.approveTutor(tutorId)

        verify(usersColl).document(tutorId)
        verify(userDoc).update(mapOf("adminApproved" to true, "active" to true))
        verify(userDoc).get()
        verify(mailColl).add(any())
        verify(notifColl).add(any())
    }

    @Test
    fun `denyTutor deactivates user and deletes reports`() = runTest {
        val tutor = User().apply { id = "t2" }
        // users collection
        val usersColl = mock<CollectionReference>()
        val userDoc = mock<DocumentReference>()
        whenever(mockDb.collection("users")).thenReturn(usersColl)
        whenever(usersColl.document("t2")).thenReturn(userDoc)
        whenever(userDoc.update("adminApproved", true, "active", false))
            .thenReturn(Tasks.forResult(null))

        // reports query
        val reportsColl = mock<CollectionReference>()
        val query = mock<Query>()
        val querySnap = mock<QuerySnapshot>()
        val repDoc = mock<DocumentSnapshot>()
        whenever(repDoc.id).thenReturn("r1")
        whenever(querySnap.documents).thenReturn(listOf(repDoc))
        whenever(mockDb.collection("reports")).thenReturn(reportsColl)
        whenever(reportsColl.whereEqualTo("userId", "t2")).thenReturn(query)
        whenever(query.get()).thenReturn(Tasks.forResult(querySnap))
        // delete each
        val reportDoc = mock<DocumentReference>()
        whenever(reportsColl.document("r1")).thenReturn(reportDoc)
        whenever(reportDoc.delete()).thenReturn(Tasks.forResult(null))

        // mail & notifications
        val mailColl = mock<CollectionReference>()
        whenever(mockDb.collection("mail")).thenReturn(mailColl)
        whenever(mailColl.add(any())).thenReturn(Tasks.forResult(mock()))
        val notifColl = mock<CollectionReference>()
        whenever(mockDb.collection("notifications")).thenReturn(notifColl)
        whenever(notifColl.add(any<Map<String, Any>>())).thenReturn(Tasks.forResult(mock()))

        repo.denyTutor(tutor, "reason", "deny")

        verify(userDoc).update("adminApproved", true, "active", false)
        verify(query).get()
        verify(reportDoc).delete()
        verify(mailColl).add(any())
        verify(notifColl).add(any())
    }

    @Test
    fun `resolveReport deletes report document`() = runTest {
        val reportId = "r3"
        val reportsColl = mock<CollectionReference>()
        val reportDoc = mock<DocumentReference>()
        whenever(mockDb.collection("reports")).thenReturn(reportsColl)
        whenever(reportsColl.document(reportId)).thenReturn(reportDoc)
        whenever(reportDoc.delete()).thenReturn(Tasks.forResult(null))

        repo.resolveReport(reportId)

        verify(reportsColl).document(reportId)
        verify(reportDoc).delete()
    }

    @Test
    fun `signOut calls FirebaseAuth signOut`() {
        repo.signOut()
        verify(mockAuth).signOut()
    }

}