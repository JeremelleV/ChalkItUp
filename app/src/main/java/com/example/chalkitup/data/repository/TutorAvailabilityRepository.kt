package com.example.chalkitup.data.repository

import com.example.chalkitup.domain.model.TutorAvailabilityWrapper
import com.example.chalkitup.domain.repository.TutorAvailabilityRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorAvailabilityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TutorAvailabilityRepositoryInterface {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun initializeSessionCount(tutorId: String, monthYear: String): Result<Unit> = runCatching {
        val ref = firestore.collection("availability").document(monthYear).collection(tutorId).document("sessionCount")
        val doc = ref.get().await()
        if (!doc.exists()) {
            ref.set(mapOf("week1" to 0, "week2" to 0, "week3" to 0, "week4" to 0, "week5" to 0)).await()
        }
    }

    override fun observeAvailability(
        tutorId: String,
        monthYear: String,
        onAvailabilityUpdate: (TutorAvailabilityWrapper?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection("availability")
            .document(monthYear)
            .collection(tutorId)
            .document("availabilityData")
            .addSnapshotListener { doc, err ->
                if (err != null) { onError(Exception(err)); return@addSnapshotListener }
                onAvailabilityUpdate(doc?.toObject(TutorAvailabilityWrapper::class.java))
            }
    }

    override suspend fun saveAvailability(
        tutorId: String,
        monthYear: String,
        availabilityWrapper: TutorAvailabilityWrapper
    ): Result<Unit> = runCatching {
        firestore.collection("availability")
            .document(monthYear)
            .collection(tutorId)
            .document("availabilityData")
            .set(availabilityWrapper)
            .await()
    }
}
