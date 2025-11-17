package com.example.chalkitup.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationViewModel: ViewModel() {

    private val _userType = MutableStateFlow<String?>("Unknown")
    val userType: StateFlow<String?> get() = _userType

    private val _notifications = MutableStateFlow<List<NotifClass>>(emptyList())
    val notifications: StateFlow<List<NotifClass>> get() = _notifications

    init {
        getUserName()
        grabNotifications()
    }

    // LiveData to hold and observe the user's profile picture URL
    private val _profilePictureUrl = MutableLiveData<String?>()
    val profilePictureUrl: LiveData<String?> get() = _profilePictureUrl

    // Function to load the profile picture from storage
    fun loadProfilePicture(userId: String) {
        val storageRef = Firebase.storage.reference.child("$userId/profilePicture.jpg")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            _profilePictureUrl.value = uri.toString()
        }.addOnFailureListener {
            _profilePictureUrl.value = null // Set to null if no profile picture exists
        }
    }

    private fun getUserName() {
        viewModelScope.launch {
            val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
            currentUserID?.let {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(it)
                val snapshot = userRef.get().await()
                _userType.value = snapshot.getString("userType") ?: "Unknown"
            }
        }
    }

    private fun grabNotifications() {
        viewModelScope.launch {
            val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserID != null) {
                val db = FirebaseFirestore.getInstance()
                val notifsRef = db.collection("notifications")
                val usersRef = db.collection("users")

                try {
                    val userNotifications = notifsRef
                        .whereEqualTo("notifUserID", currentUserID)
                        .get()
                        .await()
                        .documents

                    _notifications.value = userNotifications
                        .mapNotNull {
                            doc ->
                            val notification = doc.toObject(NotifClass::class.java)?.copy(notifID = doc.id)

                            notification?.let {
                                val userSnapshot = usersRef.document(it.notifUserID).get().await()
                                val userFirstName = userSnapshot.getString("firstName") ?: "Unknown"
                                val userLastName = userSnapshot.getString("lastName") ?: ""

                                it.copy(
                                    notifUserName = "$userFirstName $userLastName",
                                )
                            }
                        }
                        .sortedBy { notification ->
                            LocalDate.parse(notification.notifDate,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US))
                        }
                    println("Notifications: $userNotifications")

                } catch (e: Exception) {
                    println("Error fetching notifications: ${e.message}")
                }
            }
        }
    }

    // Firebase order: notifications/actual notification info
    private fun addNotification(
        notifType: String, // Update, Session, Message
        notifUserID: String,
        notifUserName: String, // Name of the person in the notification
        notifTime: String,
        notifDate: String,
        comments: String,
        sessType: String, // Booked, Rescheduled, Cancelled
        sessDate: String,
        sessTime: String,
        otherID: String, // ID of the other person in the notification
        otherName: String, // ID of the other person in the notification
        subject: String,
        grade: String,
        spec: String,
        mode: String,
        price: String
    ) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()

            val notifData = hashMapOf(
                "notifID" to "",
                "notifType" to notifType,
                "notifUserID" to notifUserID,
                "notifUserName" to notifUserName,
                "notifTime" to notifTime,
                "notifDate" to notifDate,
                "comments" to comments,
                "sessType" to sessType,
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

            db.collection("notifications")
                .add(notifData)
                .await()
        }
    }
}


/**
 * Data needed for the notification
 *
 * notifType: What the notification is about in one word (Update, Session, Message)
 *
 * Everything else contains the specific information about the notification
 **/
data class NotifClass (
    var notifID: String = "",
    var notifType: String = "", // Update, Session, Message
    val notifUserID: String = "", // ID of the person in the notification
    val notifUserName: String = "", // Name of the person in the notification
    val notifTime: String = "",
    var notifDate: String = "",
    var comments: String = "",
    var sessType: String = "", // Booked, Rescheduled, Cancelled
    val sessDate: String = "",
    val sessTime: String = "",
    val otherID: String = "", // ID of the other person in the notification
    val otherName: String = "", // ID of the other person in the notification
    val subject: String = "",
    val grade: String = "",
    val spec: String = "",
    val mode: String = "",
    val price: String = ""
)