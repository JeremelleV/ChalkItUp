package com.example.chalkitup.data.repository

import com.example.chalkitup.CoroutineTestRule
import com.example.chalkitup.ui.components.TutorSubject
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val mockAuth = mock<FirebaseAuth>()
    private val mockDb   = mock<FirebaseFirestore>()
    private lateinit var repo: AuthRepository

    @Before fun setUp() {
        repo = AuthRepository(mockDb, mockAuth)
    }

    @Test
    fun `signupWithEmail creates user, saves data and sends verification email`() = runTest {
        val email = "newuser@example.com"
        val password = "securePass123"
        val firstName = "Jane"
        val lastName = "Doe"
        val userType = "Tutor"
        val uid = "generatedUID"
        val subjects = listOf<TutorSubject>() // empty list for simplicity

        val mockCollection = mock<CollectionReference>()
        val mockDocRef = mock<DocumentReference>()
        val mockUser = mock<FirebaseUser>()
        val authResult = mock<com.google.firebase.auth.AuthResult>()

        whenever(authResult.user).thenReturn(mockUser)
        whenever(mockUser.uid).thenReturn(uid)
        whenever(mockAuth.createUserWithEmailAndPassword(email, password))
            .thenReturn(Tasks.forResult(authResult))

        whenever(mockUser.reload()).thenReturn(Tasks.forResult(null))
        whenever(mockUser.sendEmailVerification()).thenReturn(Tasks.forResult(null))

        whenever(mockDb.collection("users")).thenReturn(mockCollection)
        whenever(mockCollection.document(uid)).thenReturn(mockDocRef)
        whenever(mockDocRef.set(any())).thenReturn(Tasks.forResult(null))

        val result = repo.signupWithEmail(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            userType = userType,
            subjects = subjects
        )

        assert(result.isSuccess)
        assert(result.getOrNull() === mockUser)

        verify(mockAuth).createUserWithEmailAndPassword(email, password)
        verify(mockUser).reload()
        verify(mockUser).sendEmailVerification()
        verify(mockDocRef).set(check { data ->
            val map = data as Map<*, *>
            assert(map["email"] == email)
            assert(map["firstName"] == firstName)
            assert(map["userType"] == userType)
            assert(map["subjects"] == subjects)
            assert(map["agreeToTerms"] == false)
            assert(map["adminApproved"] == false)
            assert(map["active"] == true)
        })
    }


    @Test
    fun `getCurrentUser returns null when no user signed in`() {
        whenever(mockAuth.currentUser).thenReturn(null)
        val user = repo.getCurrentUser()
        assert(user == null)
    }

    @Test
    fun `getCurrentUser returns FirebaseUser when signed in`() {
        val fUser = mock<FirebaseUser>()
        whenever(mockAuth.currentUser).thenReturn(fUser)
        val result = repo.getCurrentUser()
        assert(result === fUser)
    }

    @Test
    fun `signOut calls FirebaseAuth signOut`() {
        repo.signOut()
        verify(mockAuth).signOut()
    }

    @Test
    fun `isAdminApproved returns true for Student`() = runTest {
        // Stub Firestore user doc
        val userId = "u1"
        val docRef = mock<com.google.firebase.firestore.DocumentReference>()
        val snap   = mock<com.google.firebase.firestore.DocumentSnapshot>()
        whenever(mockAuth.currentUser).thenReturn(mock())
        whenever(mockAuth.currentUser?.uid).thenReturn(userId)
        whenever(mockDb.collection("users")).thenReturn(mock())
        whenever(mockDb.collection("users").document(userId)).thenReturn(docRef)
        whenever(docRef.get()).thenReturn(Tasks.forResult(snap))
        whenever(snap.getString("userType")).thenReturn("Student")

        val approved = repo.isAdminApproved(userId)
        assert(approved.isSuccess && approved.getOrNull() == true)
    }

    @Test
    fun `isAdminApproved returns false for Tutor not approved`() = runTest {
        val uid = "tutor1"
        val mockCollection = mock<CollectionReference>()
        val mockDoc = mock<DocumentReference>()
        val snap = mock<DocumentSnapshot>()

        whenever(mockDb.collection("users")).thenReturn(mockCollection)
        whenever(mockCollection.document(uid)).thenReturn(mockDoc)
        whenever(mockDoc.get()).thenReturn(Tasks.forResult(snap))
        whenever(snap.getString("userType")).thenReturn("Tutor")
        whenever(snap.getBoolean("adminApproved")).thenReturn(false)

        val result = repo.isAdminApproved(uid)
        assert(result.isSuccess && result.getOrNull() == false)
    }

    @Test
    fun `isAdminUser returns true for Admin`() = runTest {
        val uid = "admin1"
        val mockCollection = mock<CollectionReference>()
        val mockDoc = mock<DocumentReference>()
        val snap = mock<DocumentSnapshot>()

        whenever(mockDb.collection("users")).thenReturn(mockCollection)
        whenever(mockCollection.document(uid)).thenReturn(mockDoc)
        whenever(mockDoc.get()).thenReturn(Tasks.forResult(snap))
        whenever(snap.getString("userType")).thenReturn("Admin")

        val result = repo.isAdminUser(uid)
        assert(result.isSuccess && result.getOrNull() == true)
    }

    @Test
    fun `isEmailVerified returns false if user is null`() {
        whenever(mockAuth.currentUser).thenReturn(null)
        assert(!repo.isEmailVerified())
    }

    @Test
    fun `isEmailVerified returns correct value from user`() {
        val mockUser = mock<FirebaseUser>()
        whenever(mockUser.isEmailVerified).thenReturn(true)
        whenever(mockAuth.currentUser).thenReturn(mockUser)

        assert(repo.isEmailVerified())
    }

    @Test
    fun `loginWithEmailAndPassword returns user on success`() = runTest {
        val email = "test@example.com"
        val pass = "password123"
        val mockUser = mock<FirebaseUser>()
        val authResult = mock<com.google.firebase.auth.AuthResult>()
        whenever(authResult.user).thenReturn(mockUser)

        whenever(mockAuth.signInWithEmailAndPassword(email, pass))
            .thenReturn(Tasks.forResult(authResult))

        val result = repo.loginWithEmailAndPassword(email, pass)
        assert(result.isSuccess && result.getOrNull() == mockUser)
    }

    @Test
    fun `loginWithGoogleCredential returns success`() = runTest {
        val credential = mock<AuthCredential>()
        val resultTask = Tasks.forResult(mock<com.google.firebase.auth.AuthResult>())

        whenever(mockAuth.signInWithCredential(credential)).thenReturn(resultTask)

        val result = repo.loginWithGoogleCredential(credential)
        assert(result.isSuccess)
    }

    @Test
    fun `resendVerificationEmail sends email if user exists`() = runTest {
        val mockUser = mock<FirebaseUser>()
        whenever(mockAuth.currentUser).thenReturn(mockUser)
        whenever(mockUser.sendEmailVerification()).thenReturn(Tasks.forResult(null))

        val result = repo.resendVerificationEmail()
        assert(result.isSuccess && result.getOrNull() == "Email sent")
    }

    @Test
    fun `resetPassword sends email`() = runTest {
        val email = "reset@example.com"
        whenever(mockAuth.sendPasswordResetEmail(email)).thenReturn(Tasks.forResult(null))

        val result = repo.resetPassword(email)
        assert(result.isSuccess)
    }

    @Test
    fun `agreeToTerms updates agreeToTerms field`() = runTest {
        val uid = "user123"
        val user = mock<FirebaseUser>()
        val mockCollection = mock<CollectionReference>()
        val mockDoc = mock<DocumentReference>()

        whenever(mockAuth.currentUser).thenReturn(user)
        whenever(user.uid).thenReturn(uid)
        whenever(mockDb.collection("users")).thenReturn(mockCollection)
        whenever(mockCollection.document(uid)).thenReturn(mockDoc)
        whenever(mockDoc.update("agreeToTerms", true)).thenReturn(Tasks.forResult(null))

        repo.agreeToTerms()

        verify(mockDoc).update("agreeToTerms", true)
    }

    @Test
    fun `hasAgreedToTerms returns true if user agreed`() = runTest {
        val uid = "user1"
        val mockCollection = mock<CollectionReference>()
        val mockDoc = mock<DocumentReference>()
        val snap = mock<DocumentSnapshot>()

        whenever(mockDb.collection("users")).thenReturn(mockCollection)
        whenever(mockCollection.document(uid)).thenReturn(mockDoc)
        whenever(mockDoc.get()).thenReturn(Tasks.forResult(snap))
        whenever(snap.getBoolean("agreeToTerms")).thenReturn(true)

        val result = repo.hasAgreedToTerms(uid)
        assert(result)
    }


    @Test
    fun `addNotification adds welcome notification`() = runTest {
        val coll = mock<com.google.firebase.firestore.CollectionReference>()
        whenever(mockDb.collection("notifications")).thenReturn(coll)
        whenever(coll.add(any())).thenReturn(Tasks.forResult(mock()))

        val result = repo.addNotification("u1", "Alice", "3:00 PM", "2024-04-01")
        assert(result.isSuccess)
    }
}
