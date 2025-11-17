package com.example.chalkitup.data.repository

import com.example.chalkitup.domain.model.UserProfile
import com.example.chalkitup.domain.repository.ProfileRepositoryInterface
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : ProfileRepositoryInterface {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun fetchUserProfile(userId: String): Result<UserProfile?> = runCatching {
        firestore.collection("users").document(userId).get().await()
            .toObject(UserProfile::class.java)
    }

    override suspend fun loadProfilePicture(userId: String): Result<String?> = runCatching {
        storage.reference.child("$userId/profilePicture.jpg").downloadUrl.await().toString()
    }

    override suspend fun reportUser(userId: String, reportMessage: String): Result<Unit> = runCatching {
        val data = mapOf(
            "userId" to userId,
            "reportMessage" to reportMessage,
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("reports").add(data).await()
    }

    override fun startListeningForPastSessions(
        userId: String,
        onStatsUpdate: (sessionCount: Int, totalHours: Double) -> Unit
    ): ListenerRegistration {
        return firestore.collection("appointments")
            .whereEqualTo("tutorID", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                var count = 0; var hours = 0.0
                val today = LocalDate.now()
                val df = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                val tf = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
                snap?.documents?.forEach { d ->
                    val date = d.getString("date") ?: return@forEach
                    val range = d.getString("time") ?: return@forEach
                    val sd = try { LocalDate.parse(date, df) } catch(_ : Exception) { return@forEach }
                    if (sd.isBefore(today)) {
                        val (s,e)=range.split(" - ")
                        val st=LocalTime.parse(s,tf); val et=LocalTime.parse(e,tf)
                        hours += ChronoUnit.MINUTES.between(st,et)/60.0
                        count++
                    }
                }
                onStatsUpdate(count,hours)
            }
    }

    override suspend fun updateTutorStats(
        userId: String,
        sessionCount: Int,
        totalHours: Double
    ): Result<Unit> = runCatching {
        firestore.collection("users").document(userId)
            .update(mapOf(
                "totalSessions" to sessionCount,
                "totalHoursTutored" to totalHours
            )).await()
    }

}
