package com.example.chalkitup.data.repository

import android.content.Context
import com.example.chalkitup.domain.model.UserProfile
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class EditProfileRepositoryTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var context: Context
    private lateinit var repository: EditProfileRepository

    @Before
    fun setUp() {
        auth = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        context = mockk(relaxed = true)
        repository = EditProfileRepository(firestore, auth, storage, context)
    }

    @Test
    fun `getCurrentUserId returns correct uid`() {
        val user = mockk<FirebaseUser>()
        every { auth.currentUser } returns user
        every { user.uid } returns "testUID"

        val result = runBlocking { repository.getCurrentUserId() }

        assertEquals("testUID", result)
    }

    @Test
    fun `loadUserProfile returns UserProfile when document exists`() = runTest {
        val userId = "testUser"
        val userProfile = UserProfile(userId, "Student", "John", "Doe", emptyList(), "", "", "", emptyList(), emptyList())

        val documentSnapshot = mockk<DocumentSnapshot>()
        every { documentSnapshot.toObject(UserProfile::class.java) } returns userProfile

        val documentRef = mockk<DocumentReference>()
        val collectionRef = mockk<CollectionReference>()

        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.document(userId) } returns documentRef
        coEvery { documentRef.get() } returns Tasks.forResult(documentSnapshot)

        every { auth.currentUser?.uid } returns userId

        val result = repository.loadUserProfile("")

        assertTrue(result.isSuccess)
        assertEquals(userProfile, result.getOrNull())
    }

    // Add similar tests for
    // profile picture upload,
    // delete,
    // update...
}
