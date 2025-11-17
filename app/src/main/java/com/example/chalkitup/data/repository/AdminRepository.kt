package com.example.chalkitup.data.repository

import com.example.chalkitup.domain.model.Email
import com.example.chalkitup.domain.model.EmailMessage
import com.example.chalkitup.domain.repository.AdminRepositoryInterface
import com.example.chalkitup.ui.viewmodel.admin.User
import com.example.chalkitup.ui.viewmodel.admin.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : AdminRepositoryInterface {

    internal fun mapSnapshotsToUsers(docs: List<DocumentSnapshot>): List<User> =
        docs.mapNotNull { doc ->
            doc.toObject(User::class.java)?.apply { id = doc.id }
        }

    override fun getProfilePictures(userIds: List<String>): Flow<Map<String, String?>> = callbackFlow {
        val storage = storage
        val result = mutableMapOf<String, String?>()
        userIds.forEach { id ->
            val ref = storage.reference.child("$id/profilePicture.jpg")
            ref.downloadUrl
                .addOnSuccessListener { uri ->
                    result[id] = uri.toString()
                    if (result.size == userIds.size) trySend(result)
                }
                .addOnFailureListener {
                    result[id] = null
                    if (result.size == userIds.size) trySend(result)
                }
        }
        awaitClose { /* nothing to clean up */ }
    }

    override fun getUnapprovedTutors(): Flow<List<User>> = callbackFlow {
        val auth = auth
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()   // no user → close the flow
            return@callbackFlow
        }
        val sub = db.collection("users")
            .whereEqualTo("userType", "Tutor")
            .whereEqualTo("adminApproved", false)
            .whereEqualTo("active", true)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    // IGNORE permission errors
                    if ((err as? FirebaseFirestoreException)?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        return@addSnapshotListener
                    }
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(User::class.java)?.apply { id = doc.id }
                    }
                    ?.sortedBy { it.firstName }
                trySend(list ?: emptyList())
            }
        awaitClose { sub.remove() }
    }

    override fun getApprovedTutors(): Flow<List<User>> = callbackFlow {
        val auth = auth
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val sub = db.collection("users")
            .whereEqualTo("userType", "Tutor")
            .whereEqualTo("adminApproved", true)
            .whereEqualTo("active", true)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    val code = (err as? FirebaseFirestoreException)?.code
                    if (code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        // swallow and do nothing
                        return@addSnapshotListener
                    }
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(User::class.java)?.apply { id = doc.id }
                    }
                    ?.sortedBy { it.firstName }
                    ?: emptyList()
                trySend(list)
            }

        awaitClose { sub.remove() }
    }

    override fun getReports(): Flow<List<Report>> = callbackFlow {
        val auth = auth
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val sub = db.collection("reports")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    val code = (err as? FirebaseFirestoreException)?.code
                    if (code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        return@addSnapshotListener
                    }
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(Report::class.java)?.apply { id = doc.id }
                    }
                    ?: emptyList()

                trySend(list)
            }

        awaitClose { sub.remove() }
    }

    override fun getUsersWithReports(): Flow<List<User>> = callbackFlow {
        val auth = auth
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Launch a one‑off fetch in this flow's coroutine scope
        launch {
            try {
                val reportsSnap = db.collection("reports").get().await()
                val userIds = reportsSnap.documents
                    .mapNotNull { it.getString("userId") }
                    .distinct()

                val users = userIds.mapNotNull { userId ->
                    runCatching {
                        db.collection("users").document(userId).get().await()
                    }.getOrNull()
                        ?.toObject(User::class.java)
                        ?.apply { id = userId }
                }

                trySend(users)
            } catch (e: Exception) {
                trySend(emptyList())
            } finally {
                close()
            }
        }

        // No snapshot listener to remove here
        awaitClose { /* nothing to clean up */ }
    }

    override suspend fun approveTutor(tutorId: String) {
        // 0) Guard
        require(tutorId.isNotBlank()) {
            "approveTutor called with blank tutorId!"
        }

        // 2) Get the right document reference
        val userRef = db
            .collection("users")         // correct collection
            .document(tutorId.trim())    // ensure no whitespace

        // 3) Update both flags atomically
        userRef.update(
            mapOf(
                "adminApproved" to true,
                "active" to true
            )
        ).await()

        // 4) Fetch the updated user
        val snap = userRef.get().await()
        val tutor = snap.toObject(User::class.java)
            ?.apply { id = tutorId }
            ?: throw IllegalStateException("Tutor $tutorId not found after approval")

        // 5) Send the approval email/notification
        sendApprovalEmail(tutor, "")
    }



    override suspend fun denyTutor(tutor: User, reason: String, type: String) {
        val ref = db.collection("users").document(tutor.id)
        // deactivate
        ref.update("adminApproved", true, "active", false).await()
        // delete reports
        val reports = db.collection("reports")
            .whereEqualTo("userId", tutor.id).get().await()
        reports.documents.forEach { db.collection("reports").document(it.id).delete().await() }
        sendDeactivationEmail(tutor, reason, type)
    }

    override suspend fun resolveReport(reportId: String) {
        db.collection("reports").document(reportId).delete().await()
    }

    override fun signOut() {
        auth.signOut()
    }

    // — Helpers for emails & notifications —

    private suspend fun sendDeactivationEmail(tutor: User, reason: String, type: String) {
        val dType = if (type == "deny") "denied" else "deactivated"
        val subj = "Your account for ChalkItUp has been $dType"
        val html = """
            <p>Hi ${tutor.firstName},</p>
            <p>Your account has been <b>$dType</b>. Reason: $reason</p>
            <p>You can still log in, but will not be matched to sessions.</p>
            <p>- ChalkItUp Team</p>
        """.trimIndent()
        db.collection("mail").add(
            Email(
            to = tutor.email,
            message = EmailMessage(subj, html)
        )
        ).await()
        addNotification(
            tutor.id,
            tutor.firstName,
            LocalTime.now().toString(),
            LocalDate.now().toString(),
            "Your account has been $dType. Reason: $reason",
            "Deactivated"
        )
    }

    private suspend fun sendApprovalEmail(tutor: User, comments: String) {
        val subj = "Your Tutor account has been approved!"
        val html = """
            <p>Hi ${tutor.firstName},</p>
            <p>Your tutor account is now approved! $comments</p>
            <p>Start taking sessions today.</p>
            <p>- ChalkItUp Team</p>
        """.trimIndent()
        db.collection("mail").add(Email(
            to = tutor.email,
            message = EmailMessage(subj, html)
        )).await()
        addNotification(
            tutor.id,
            tutor.firstName,
            LocalTime.now().toString(),
            LocalDate.now().toString(),
            "Your account has been approved! $comments",
            "Approved"
        )
    }

    private suspend fun addNotification(
        userId: String,
        userName: String,
        time: String,
        date: String,
        comments: String,
        type: String
    ) {
        val data = mapOf(
            "notifUserID" to userId,
            "notifUserName" to userName,
            "notifTime" to time,
            "notifDate" to date,
            "comments" to comments,
            "notifType" to type
        )
        db.collection("notifications").add(data).await()
    }
}