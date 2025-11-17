package com.example.chalkitup.data.repository

import com.example.chalkitup.domain.model.Email
import com.example.chalkitup.domain.model.EmailMessage
import com.example.chalkitup.domain.model.TimeSlot
import com.example.chalkitup.domain.model.TutorAvailabilityWrapper
import com.example.chalkitup.domain.model.UserInfo
import com.example.chalkitup.domain.repository.BookingRepositoryInterface
import com.example.chalkitup.ui.components.TutorSubject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : BookingRepositoryInterface {

    // --- USER INFO & STATUS ---

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun fetchUserInfo(userId: String): Result<UserInfo> = try {
        val userDoc = firestore.collection("users").document(userId).get().await()
        if (userDoc.exists()) {
            val userType = userDoc.getString("userType") ?: "Unknown"
            val firstName = userDoc.getString("firstName") ?: "Unknown"
            val email = userDoc.getString("email") ?: "Unknown"
            Result.success(UserInfo(userType, firstName, email))
        } else {
            Result.failure(Exception("User document does not exist for userId: $userId"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun fetchUserActiveStatus(): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No user signed in"))
            val userDoc = firestore.collection("users").document(userId).get().await()
            Result.success(userDoc.getBoolean("active") ?: false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUserFullName(userId: String): Result<String> = try {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val firstName = userDoc.getString("firstName") ?: ""
        val lastName = userDoc.getString("lastName") ?: ""
        Result.success("$firstName $lastName")
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendEmail(
        tutorID: String,
        fName: String,
        tutorName: String,
        subject: TutorSubject,
        date: String,
        timeSlot: String,
        price: String,
        userEmail: String
    ): Result<Unit> = try {
        val tutorDoc = firestore.collection("users").document(tutorID).get().await()
        val tutorEmail = tutorDoc.getString("email") ?: "Unknown"
        val formattedSubject = "${subject.subject} ${subject.grade} ${subject.specialization}"
        val emailSubj = "Your appointment for $formattedSubject has been booked"
        val emailHTML = "<p>Hi $fName,<br>Your appointment for <b>$formattedSubject</b> with $tutorName at $date $timeSlot has been booked.</p><p>Rate: $price</p>"
        val tutorHTML = "<p>Hi $tutorName,<br>Your appointment for <b>$formattedSubject</b> with $fName at $date $timeSlot has been booked.</p><p>Rate: $price</p>"
        firestore.collection("mail").add(Email(to = userEmail, message = EmailMessage(emailSubj, emailHTML))).await()
        firestore.collection("mail").add(Email(to = tutorEmail, message = EmailMessage(emailSubj, tutorHTML))).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateSessionCount(tutorId: String, yearMonth: String, weekNumber: Int): Result<Unit> = try {
        firestore.collection("availability").document(yearMonth).collection(tutorId)
            .document("sessionCount").update("week$weekNumber", FieldValue.increment(1)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markTimesAsBooked(
        tutorId: String,
        yearMonth: String,
        day: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Result<Unit> {
        return try {
            val ref = firestore.collection("availability").document(yearMonth).collection(tutorId).document("availabilityData")
            val doc = ref.get().await()
            val list = doc.get("availability") as? List<Map<String, Any>> ?: return Result.failure(Exception("No data"))
            val idx = list.indexOfFirst { it["day"] == day.toString() }
            if (idx == -1) return Result.failure(Exception("No entry for day $day"))
            val slots = (list[idx]["timeSlots"] as List<Map<String, Any>>).map {
                TimeSlot(it["time"] as String, it["inPerson"] as Boolean, it["online"] as Boolean, it["booked"] as Boolean)
            }.toMutableList()
            val fmt = DateTimeFormatter.ofPattern("h:mm a")
            var ct = startTime
            while (ct < endTime) {
                slots.find { it.time == ct.format(fmt) }?.booked = true
                ct = ct.plusMinutes(30)
            }
            val updated = list.toMutableList().apply {
                this[idx] = (list[idx].toMutableMap().apply { put("timeSlots", slots.map { mapOf("time" to it.time, "inPerson" to it.inPerson, "online" to it.online, "booked" to it.booked) }) })
            }
            ref.update("availability", updated).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addSession(
        tutorId: String,
        comments: String,
        sessionType: String,
        day: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        subject: TutorSubject,
        studentId: String,
        tutorFullName: String,
        studentFullName: String
    ): Result<Unit> = runCatching {
        // 1. Add appointment document
        val formattedSubject = "${subject.subject} ${subject.grade} ${subject.specialization}"
        val fmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        val timeRange = "${startTime.format(fmt)} - ${endTime.format(fmt)}"
        val mode = if (sessionType == "inPerson") "In Person" else "Online"
        firestore.collection("appointments").add(
            mapOf(
                "tutorID" to tutorId,
                "comments" to comments,
                "mode" to mode,
                "date" to day.toString(),
                "time" to timeRange,
                "status" to "booked",
                "subject" to formattedSubject,
                "studentID" to studentId,
                "tutorName" to tutorFullName,
                "studentName" to studentFullName,
                "subjectObject" to subject
            )
        ).await()

        // 2. Write notifications for student and tutor
        addBookingNotification(
            notifUserID = studentId,
            notifUserName = studentFullName,
            sessDate = day.toString(),
            sessTime = timeRange,
            otherID = tutorId,
            otherName = tutorFullName,
            subject = subject.subject,
            grade = subject.grade,
            spec = subject.specialization,
            mode = mode,
            price = subject.price
        ).getOrThrow()

        addBookingNotification(
            notifUserID = tutorId,
            notifUserName = tutorFullName,
            sessDate = day.toString(),
            sessTime = timeRange,
            otherID = studentId,
            otherName = studentFullName,
            subject = subject.subject,
            grade = subject.grade,
            spec = subject.specialization,
            mode = mode,
            price = subject.price
        ).getOrThrow()
    }

    private suspend fun addBookingNotification(
        notifUserID: String,
        notifUserName: String,
        sessDate: String,
        sessTime: String,
        otherID: String,
        otherName: String,
        subject: String,
        grade: String,
        spec: String,
        mode: String,
        price: String
    ): Result<Unit> = runCatching {
        val notifData = mapOf(
            "notifID" to "",
            "notifType" to "Session",
            "notifUserID" to notifUserID,
            "notifUserName" to notifUserName,
            "notifTime" to LocalTime.now().toString(),
            "notifDate" to LocalDate.now().toString(),
            "comments" to "",
            "sessType" to "Booked",
            "sessDate" to sessDate,
            "sessTime" to sessTime,
            "otherID" to otherID,
            "otherName" to otherName,
            "subject" to subject,
            "grade" to grade,
            "spec" to spec,
            "mode" to mode,
            "price" to price
        )
        firestore.collection("notifications").add(notifData).await()
    }

    override suspend fun fetchTutors(
        selectedSubject: TutorSubject,
        priceRange: ClosedFloatingPointRange<Float>
    ): Result<List<String>> = try {
        val snaps = firestore.collection("users").whereEqualTo("userType","Tutor").whereEqualTo("active",true).get().await()
        val matched = mutableListOf<String>()
        snaps.documents.forEach { doc ->
            (doc.get("subjects") as? List<Map<String,String>>)?.forEach { m ->
                val p = m["price"]?.let { Regex("""\d+(\.\d+)?""").find(it)?.value?.toFloat() }
                if (m["subject"]==selectedSubject.subject && m["grade"]==selectedSubject.grade && m["specialization"]==selectedSubject.specialization && p!=null && p in priceRange) matched+=doc.id
            }
        }
        Result.success(matched)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun fetchAvailabilityForTutors(
        tutorIds: List<String>,
        isCurrentMonth: Boolean,
        mode: String
    ): Result<Pair<Map<LocalDate, List<LocalTime>>, Map<String, Map<LocalDate, List<LocalTime>>>>> = coroutineScope {
        try {
            val monthYear = if(isCurrentMonth) SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(
                Date()
            )
            else SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(Calendar.getInstance().apply{add(Calendar.MONTH,1)}.time)
            val availMap= mutableMapOf<LocalDate,MutableList<LocalTime>>()
            val tutorMap= mutableMapOf<String,MutableMap<LocalDate,MutableList<LocalTime>>>()
            tutorIds.map{ id ->
                async {
                    firestore.collection("availability").document(monthYear).collection(id).document("availabilityData").get().await() to id
                }
            }.awaitAll().forEach{ (doc,id) ->
                if(doc.exists()){
                    doc.toObject(TutorAvailabilityWrapper::class.java)?.availability?.forEach{ ta->
                        val date=LocalDate.parse(ta.day)
                        val times=ta.timeSlots.mapNotNull{ ts->
                            when(mode){
                                "inPerson"-> if(ts.inPerson&&!ts.booked) LocalTime.parse(ts.time,DateTimeFormatter.ofPattern("h:mm a")) else null
                                "online"-> if(ts.online&&!ts.booked) LocalTime.parse(ts.time,DateTimeFormatter.ofPattern("h:mm a")) else null
                                else->null
                            }
                        }.sorted()
                        if(times.isNotEmpty()){
                            tutorMap.getOrPut(id){mutableMapOf()}.getOrPut(date){mutableListOf()}.addAll(times)
                            availMap.getOrPut(date){mutableListOf()}.addAll(times)
                        }
                    }
                }
            }
            Result.success(availMap to tutorMap)
        } catch(e:Exception){
            Result.failure(e)
        }
    }

    override suspend fun fetchTutorPriceForSubject(
        tutorId: String,
        selectedSubject: TutorSubject
    ): Result<String> {
        return try {
            val doc=firestore.collection("users").document(tutorId).get().await()
            val subjects=doc.get("subjects") as? List<Map<String,String>>?:return Result.failure(Exception("Subjects missing"))
            subjects.forEach{ m->
                if(m["subject"]==selectedSubject.subject && m["grade"]==selectedSubject.grade && m["specialization"]==selectedSubject.specialization)
                    return Result.success(m["price"]!!)
            }
            Result.failure(Exception("Price not found"))
        } catch(e:Exception){Result.failure(e)}
    }

    override suspend fun getSessionCountForWeek(
        tutorId: String,
        yearMonth: String,
        weekNumber: Int
    ): Result<Int> = try {
        val doc=firestore.collection("availability").document(yearMonth).collection(tutorId).document("sessionCount").get().await()
        Result.success(doc.getLong("week$weekNumber")?.toInt()?:0)
    } catch(e:Exception){Result.failure(e)}

}
