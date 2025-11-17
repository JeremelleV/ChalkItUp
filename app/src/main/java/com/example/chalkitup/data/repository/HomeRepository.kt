package com.example.chalkitup.data.repository

import com.example.chalkitup.domain.model.Appointment
import com.example.chalkitup.domain.model.TutorAvailabilityWrapper
import com.example.chalkitup.domain.repository.HomeRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : HomeRepositoryInterface {

    override suspend fun loadProfilePicture(userId: String): Result<String?> = runCatching {
        storage.reference.child("$userId/profilePicture.jpg")
            .downloadUrl.await().toString()
    }

    override suspend fun getUserNameAndType(): Result<Pair<String, String>> = runCatching {
        val uid = auth.currentUser?.uid ?: throw Exception("No user")
        val doc = firestore.collection("users").document(uid).get().await()
        val name = doc.getString("firstName") ?: "User"
        val type = doc.getString("userType") ?: "Unknown"
        name to type
    }

    override suspend fun fetchBookedDates(): Result<List<String>> = runCatching {
        val uid = auth.currentUser?.uid ?: throw Exception("No user")
        val ref = firestore.collection("appointments")
        val u = ref.whereEqualTo("studentID", uid).get().await().documents
        val t = ref.whereEqualTo("tutorID", uid).get().await().documents
        (u + t).mapNotNull { it.getString("date")?.replace("\"","") }
    }

    override suspend fun fetchAppointments(): Result<List<Appointment>> = runCatching {
        val uid = auth.currentUser?.uid ?: throw Exception("No user")
        val ref = firestore.collection("appointments")
        val usersRef = firestore.collection("users")
        val docs = (ref.whereEqualTo("studentID", uid).get().await().documents +
                ref.whereEqualTo("tutorID", uid).get().await().documents)
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        docs.mapNotNull { d ->
            val a = d.toObject(Appointment::class.java)?.copy(appointmentID = d.id) ?: return@mapNotNull null
            val tn = usersRef.document(a.tutorID).get().await()
            val sn = usersRef.document(a.studentID).get().await()
            a.copy(
                tutorName = "${tn.getString("firstName")?:""} ${tn.getString("lastName")?:""}".trim(),
                studentName = "${sn.getString("firstName")?:""} ${sn.getString("lastName")?:""}".trim()
            )
        }.filter {
            LocalDate.parse(it.date, fmt).isAfter(LocalDate.now().minusDays(1))
        }.sortedBy { LocalDate.parse(it.date, fmt) }
    }

    override suspend fun cancelAppointment(appointment: Appointment): Result<Unit> = runCatching {
        val doc = firestore.collection("appointments").document(appointment.appointmentID).get().await()
        val date = doc.getString("date") ?: throw Exception("No date")
        val time = doc.getString("time") ?: throw Exception("No time")
        val slots = parseTimeRangeExcludingLast(time)
        val monthYear = date.substring(0,7)
        markAvailable(appointment.tutorID, date, slots)
        updateSessionCount(appointment.tutorID, monthYear, getWeekNumber(LocalDate.parse(appointment.date)))
        sendCancellationEmails(appointment)

        val tutorNotif = mapOf(
            "notifID" to "",
            "notifType" to "Session",
            "notifUserID" to appointment.tutorID,
            "notifUserName" to appointment.tutorName,
            "notifTime" to LocalTime.now().toString(),
            "notifDate" to LocalDate.now().toString(),
            "comments" to appointment.comments,
            "sessType" to "Cancelled",
            "sessDate" to appointment.date,
            "sessTime" to appointment.time,
            "otherID" to appointment.studentID,
            "otherName" to appointment.studentName,
            "subject" to appointment.subjectObject["subject"].toString(),
            "grade" to appointment.subjectObject["grade"].toString(),
            "spec" to appointment.subjectObject["specialization"].toString(),
            "mode" to appointment.mode,
            "price" to appointment.subjectObject["price"].toString()
        )
        val studentNotif = tutorNotif.mapKeys { it.key }  // swap user IDs and names
            .toMutableMap().apply {
                this["notifUserID"] = appointment.studentID
                this["notifUserName"] = appointment.studentName
                this["otherID"] = appointment.tutorID
                this["otherName"] = appointment.tutorName
            }
        firestore.collection("notifications").add(tutorNotif).await()
        firestore.collection("notifications").add(studentNotif).await()

        firestore.collection("appointments").document(appointment.appointmentID).delete().await()
    }

    private suspend fun markAvailable(tid: String, date: String, slots: List<String>) {
        val m = firestore.collection("availability").document(date.substring(0,7))
            .collection(tid).document("availabilityData")
        val w = m.get().await().toObject(TutorAvailabilityWrapper::class.java)?.availability ?: return
        val idx = w.indexOfFirst { it.day==date }
        if(idx>=0){
            val entry=w[idx]
            val updated = entry.timeSlots.map{ts->
                if(ts.time in slots) ts.copy(booked=false) else ts
            }
            val newList = w.toMutableList().apply{this[idx]=entry.copy(timeSlots=updated)}
            m.set(TutorAvailabilityWrapper(newList)).await()
        }
    }

    private suspend fun updateSessionCount(tid:String, ym:String, week:Int) {
        firestore.collection("availability").document(ym).collection(tid)
            .document("sessionCount").update("week$week", com.google.firebase.firestore.FieldValue.increment(-1)).await()
    }

    private suspend fun sendCancellationEmails(a:Appointment) {
        suspend fun email(uid:String)= firestore.collection("users").document(uid).get().await().getString("email")?:""
        val tutorEmail=email(a.tutorID); val studentEmail=email(a.studentID)
        val userID = auth.currentUser?.uid ?: throw Exception("No user")
        val formattedSubject = a.subjectObject["subject"].toString() + a.subjectObject["grade"].toString() + a.subjectObject["specialization"].toString()
        val subj="Your appointment for ${a.subjectObject["subject"]} has been cancelled"
        val html="<p> Hi ${if (userID == a.studentID) a.studentName
        else a.tutorName},<br><br> Your appointment for <b>$formattedSubject</b>" +
                " with ${if (userID == a.studentID) a.tutorName
                else a.studentName} at ${a.date}: ${a.time} has been cancelled. </p>" +
                "<p> The appointment has been removed from your calendar. </p>" +
                "<p> Have a good day! </p>" +
                "<p> -ChalkItUp Tutors </p>"
        firestore.collection("mail").add(mapOf("to" to tutorEmail,"message" to mapOf("subject" to subj,"html" to html))).await()
        firestore.collection("mail").add(mapOf("to" to studentEmail,"message" to mapOf("subject" to subj,"html" to html))).await()
    }

    private fun parseTimeRangeExcludingLast(r:String):List<String>{
        val fmt=DateTimeFormatter.ofPattern("h:mm a",Locale.US)
        val (s,e)=r.split(" - ")
        var cur=LocalTime.parse(s,fmt); val end=LocalTime.parse(e,fmt)
        val out= mutableListOf<String>()
        while(cur<end){out+=cur.format(fmt);cur=cur.plusMinutes(30)}
        return out
    }

    private fun getWeekNumber(date:LocalDate):Int{
        val f=date.withDayOfMonth(1);val d=f.dayOfWeek.value
        return (date.dayOfMonth+d-1)/7+1
    }

}