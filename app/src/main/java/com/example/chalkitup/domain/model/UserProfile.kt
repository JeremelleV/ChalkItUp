package com.example.chalkitup.domain.model

import com.example.chalkitup.ui.components.TutorSubject
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

// Data class to represent the user profile data
data class UserProfile(
    val userType: String = "",  // Type of user ("Tutor" or "Student")
    val firstName: String = "", // First name of the user
    val lastName: String = "",  // Last name of the user
    val email: String = "",     // Email address of the user
    val subjects: List<TutorSubject> = emptyList(), // List of subjects the user is associated with (for tutors)
    val bio: String = "",        // User's bio
    var startingPrice: String = "", // Tutors startingPrice
    var experience: String = "", // Tutors years of experience
    val interests: List<Interest> =listOf( Interest("Accounting", false), Interest("Agriculture", false),
        Interest("Ancient History", false), Interest("Animal", false),
        Interest("Art", false), Interest("Art-History", false),
        Interest("Biology", false), Interest("Business", false),
        Interest("Computer Science", false), Interest("Cell-Biology", false),
        Interest("Chemistry", false), Interest("Earth-Science", false),
        Interest("English", false), Interest("Engineering", false),
        Interest("Finance", false), Interest("French", false),
        Interest("Food", false), Interest("Geology", false),
        Interest("Government", false), Interest("Kinesiology", false),
        Interest("Language", false), Interest("Legal", false),
        Interest("Marketing", false), Interest("Math", false),
        Interest("Medical Science", false), Interest("Music", false),
        Interest("Nutrition", false), Interest("Physics", false),
        Interest("Psychology", false), Interest("Social Studies", false),
        Interest("Physical Activity", false), Interest("Zoology", false)
    ),
    val progress: List<ProgressItem> =emptyList()
) {
    fun copyWith(
        userType: String = this.userType,
        firstName: String = this.firstName,
        lastName: String = this.lastName,
        email: String = this.email,
        subjects: List<TutorSubject> = this.subjects,
        interests: List<Interest> = this.interests,
        progress: List<ProgressItem> = this.progress
    ): UserProfile {
        return copy(
            userType = userType,
            firstName = firstName,
            lastName = lastName,
            email = email,
            subjects = subjects,
            interests = interests,
            progress = progress
        )
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userType" to userType,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "subjects" to subjects,
            "interests" to interests,
            "progress" to progress
        )
    }

    companion object {
        fun fromUser(user: FirebaseUser, onUserProfileLoaded: (UserProfile?) -> Unit) {
            val userId = user.uid
            val userProfileRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userProfileRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userType = document.getString("userType") ?: ""
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val email = user.email ?: ""

                        val subjects = (document.get("subjects") as? List<Map<String, Any>>)?.map {
                            TutorSubject.fromMap(it)
                        } ?: emptyList()

                        val interests = (document.get("interests") as? List<Map<String, Any>>)?.map {
                            Interest.fromMap(it)
                        } ?: emptyList()

                        val progress = (document.get("progress") as? List<Map<String, Any>>)?.map {
                            ProgressItem.fromMap(it)
                        } ?: emptyList()

                        onUserProfileLoaded(
                            UserProfile(
                                userType = userType,
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                subjects = subjects,
                                interests = interests,
                                progress = progress
                            )
                        )
                    } else {
                        onUserProfileLoaded(null) // Handle missing document
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    onUserProfileLoaded(null) // Handle failure case
                }
        }
    }
}


data class Interest(val name: String = "", var isSelected: Boolean = false) {
    companion object {
        fun fromMap(map: Map<String, Any>): Interest {
            return Interest(
                name = map["name"] as? String ?: "",
                isSelected = map["isSelected"] as? Boolean ?: false
            )
        }
    }
}
data class ProgressItem(val title: String = "", val grade: String = "") {
    companion object {
        fun fromMap(map: Map<String, Any>): ProgressItem {
            return ProgressItem(
                title = map["title"] as? String ?: "",
                grade = map["grade"] as? String ?: ""
            )
        }
    }
}