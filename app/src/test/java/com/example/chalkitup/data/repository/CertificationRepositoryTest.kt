package com.example.chalkitup.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CertificationRepositoryTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var repository: CertificationRepository

    @Before
    fun setUp() {
        // Create Mockito mocks for Context and ContentResolver.
        context = mock(Context::class.java)
        contentResolver = mock(ContentResolver::class.java)
        // Stub methods used by the repository.
        `when`(context.cacheDir).thenReturn(File(System.getProperty("java.io.tmpdir")))
        `when`(context.contentResolver).thenReturn(contentResolver)

        // Create mocks for FirebaseAuth and FirebaseStorage.
        auth = mock(FirebaseAuth::class.java)
        storage = mock(FirebaseStorage::class.java)

        // Instantiate the repository.
        repository = CertificationRepository(auth, storage, context)

        // Stub static call Uri.parse using MockK to prevent "not mocked" errors.
        mockkStatic(android.net.Uri::class)
        every { Uri.parse(any<String>()) } returns Uri.EMPTY
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testGetCurrentUserId_whenUserSignedIn_returnsUid() = runTest {
        val fakeUser = mock(FirebaseUser::class.java)
        `when`(fakeUser.uid).thenReturn("user123")
        `when`(auth.currentUser).thenReturn(fakeUser)

        val uid = repository.getCurrentUserId()
        assertEquals("user123", uid)
    }

    @Test
    fun testGetCurrentUserId_whenNoUserSignedIn_returnsNull() = runTest {
        `when`(auth.currentUser).thenReturn(null)
        val uid = repository.getCurrentUserId()
        assertNull(uid)
    }

    @Test
    fun testFetchCertifications_withNoCertifications_returnsEmptyLists() = runTest {
        val fakeUser = mock(FirebaseUser::class.java)
        `when`(fakeUser.uid).thenReturn("user123")
        `when`(auth.currentUser).thenReturn(fakeUser)

        val fakeListResult = mock(com.google.firebase.storage.ListResult::class.java)
        `when`(fakeListResult.items).thenReturn(emptyList())

        val fakeRootRef = mock(StorageReference::class.java)
        val fakeCertsRef = mock(StorageReference::class.java)
        `when`(storage.reference).thenReturn(fakeRootRef)
        `when`(fakeRootRef.child("user123/certifications")).thenReturn(fakeCertsRef)
        `when`(fakeCertsRef.listAll()).thenReturn(Tasks.forResult(fakeListResult))

        val result = repository.fetchCertifications("")
        assertTrue(result.isSuccess)
        val (certs, uris) = result.getOrThrow()
        assertTrue(certs.isEmpty())
        assertTrue(uris.isEmpty())
    }

}
