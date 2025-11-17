package com.example.chalkitup.ui.components

import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.type.DateTime

/**
 * Composable function for displaying and managing the information for booking sessions for tutors and students.i
 *
 * This component allows either user to select their subject, grade level, specialization (if applicable),
 * and the times they have selected for when to start and end a session
 * and handles the visual state for errors, dropdown menus, and the removal of the selection.
 *
 * Current usage: BookingScreen.kt
 *
 * @param sessionClassInfo The selected user's subject, grade, and specialization details.
 * @param sessionTimes The start and end times + dates of the session.
 * @param onSubjectChange A callback function to handle subject selection change.
 * @param onGradeChange A callback function to handle grade level selection change.
 * @param onSpecChange A callback function to handle specialization selection change.
 * @param onRemove A callback function to handle removal of the subject-grade-specialization selection.
 * @param subjectError A boolean flag indicating if there's an error with the subject selection.
 * @param gradeError A boolean flag indicating if there's an error with the grade level selection.
 * @param specError A boolean flag indicating if there's an error with the specialization selection.
 */
@Composable
fun BookSessionItem(
    sessionClassInfo: SessionClassInfo,
    sessionTimes: SessionTimes,
    onSubjectChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onSpecChange: (String) -> Unit,
    onRemove: () -> Unit,
    subjectError: Boolean,
    gradeError: Boolean,
    specError: Boolean
) {
    var expandedSubject by remember { mutableStateOf(false) }
    var expandedGrade by remember { mutableStateOf(false) }
    var expandedSpec by remember { mutableStateOf(false) }
}

/**
 * Data class representing a subject, grade, and specialization that a user has selected.
 *
 * This class holds information about the selected subject, grade level, and specialization.
 */
data class SessionClassInfo(
    val subject: String = "",
    val grade: String = "",
    val specialization: String = ""
)

/**
 * Data class to represent the dates and times for a selected session.
 *
 */
data class SessionTimes(
    val sessDate: DateTime,
    val startTime: TimePicker,
    val endTime: TimePicker
)

/**
 * Data class to represent errors in tutor subject details.
 *
 * This class contains flags for errors related to subject, grade, and specialization.
 */
data class UserSubjectError(
    val subjectError: Boolean,
    val gradeError: Boolean,
    val specError: Boolean
)




