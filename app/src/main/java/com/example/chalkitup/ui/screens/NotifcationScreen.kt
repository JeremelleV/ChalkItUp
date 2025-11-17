package com.example.chalkitup.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chalkitup.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chalkitup.ui.viewmodel.NotifClass
import com.example.chalkitup.ui.viewmodel.NotificationViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// Notification Information Class

/**
 * How notifications will be displayed to the user, both in short and long form.
 * These will also contain information about account creations/ approvals
 *
 * Short:
 *
 * Notification Type
 * Date | Time
 * (Student/ Tutor Name if applicable)
 *
 * Long:
 *
 * Notification Type
 * Date| Time
 * Student/ Tutor Name
 * Will contain other comments/ notes made when changin
 * 
 * Notes that the user had put down when creating/ rebooking/ cancelling this session.
 *      The notes cannot be edited.
 *
**/


@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel,
    userId: String
) {

    //------------------------------VARIABLES----------------------------------------------

    // Scroll state for the entire screen
    val scrollState = rememberScrollState()

    // When type is specified it determines which icon to display beside the notification

    // var
    var selectedNotification by remember { mutableStateOf<NotifClass?>(null) }

    // State to hold user type
    var userType by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch user type from Firestore
    LaunchedEffect(userId) {
        try {
            val userDocument = Firebase.firestore.collection("users").document(userId).get().await()
            userType = userDocument.getString("userType")
        } catch (exception: Exception) {
            Log.e("NotificationScreen", "Error fetching user type: ${exception.message}")
        }
        isLoading = false
    }

    // Show loading indicator while fetching data
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    }
    val gradientBrushEmail = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF06C59C), // 5% Blue
            Color(0xFF54A4FF) // 95% White
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrushEmail),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LoadNotifications { notification ->
                selectedNotification = notification
            }

        }
    }

    // Long form version of notification when clicked
    selectedNotification?.let { notification ->
        NotifLargeForm(
            notif = notification,
            onDismiss = { selectedNotification = null },
            navController = navController
        )
    }

}

@Composable
fun LoadNotifications(
    notificationViewModel: NotificationViewModel = viewModel(),
    onNotificationClick: (NotifClass) -> Unit
) {
    val notifications by notificationViewModel.notifications.collectAsState()

    Column(modifier = Modifier.padding(10.dp)) {
        notifications.forEach { notification ->

//            val formattedDate = try {
//                val date = LocalDate.parse(notification.notifDate, DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US))
//                date.format(DateTimeFormatter.ofPattern("MMM d", Locale.US)) // Outputs "Mar 6"
//            } catch (e: java.lang.Exception) {
//                "Invalid Date"
//            }
           // notification.notifDate = formattedDate

            println("Notification: $notification")

           PastNotifications(
               notif = notification,
               onClick = { onNotificationClick(notification) }
           )
        }
    }
}

// UI for each notification
@Composable
fun PastNotifications(
    notificationViewModel: NotificationViewModel = viewModel(),
    notif: NotifClass,
    onClick: () -> Unit
){
    //------------------------------VARIABLES----------------------------------------------
    val userType by notificationViewModel.userType.collectAsState()

    var subjMessage = ""
    var notifLine1 = ""
    var notifLine2 = ""
    var notifLine3 = ""

    val notifImages = mapOf(
        "Update" to R.drawable.gear,
        "Booked" to R.drawable.pluscalendar,
        "Rescheduled" to R.drawable.circlecalendar,
        "Cancelled" to  R.drawable.minuscalendar,
        "Message" to R.drawable.message,
        "Deactivated" to R.drawable.deactivated_eraser,
        "Approved" to R.drawable.approved_eraser,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row (
            modifier = Modifier
                .height(120.dp)
                .width(400.dp)
                .background(Color.White, shape = RoundedCornerShape(20.dp)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (notif.notifType == "Session") {
                    notifImages[notif.sessType]?.let { painterResource(id = it) }?.let {
                        Image(
                            painter = it,
                            contentDescription = notif.sessType,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                } else {
                    notifImages[notif.notifType]?.let { painterResource(id = it) }?.let {
                        Image(
                            painter = it,
                            contentDescription = notif.notifType,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }
            }

            if (notif.notifType == "Update") {
                subjMessage = "$userType - Update"
                notifLine1 = "${notif.notifDate} | ${notif.notifTime}"
                notifLine2 = "Account: $userType"
                notifLine3 = notif.comments

            // Rescheduled may not happen, for the time being it is not added
            }
            else if (notif.notifType == "Session" && notif.sessType == "Rescheduled") {
                subjMessage = "${notif.notifType} - ${notif.sessType}"
                notifLine1 = "${notif.sessDate} | ${notif.sessTime}"
                notifLine2 = "${notif.subject} ${notif.grade} ${notif.spec}"
                notifLine3 =
                    if (userType == "Student") "Tutor: " + notif.otherName
                    else "Student: " + notif.otherName

            } else if (notif.notifType == "Session" && (
                        notif.sessType == "Booked" || notif.sessType == "Cancelled")) {
                subjMessage = "${notif.notifType} - ${notif.sessType}"
                notifLine1 = "${notif.sessDate} | ${notif.sessTime}"
                notifLine2 = "${notif.subject} ${notif.grade} ${notif.spec}"
                notifLine3 =
                    if (userType == "Student") "Tutor: " + notif.otherName
                    else "Student: " + notif.otherName

            } else if (notif.notifType == "Message") {
                subjMessage =
                    if (userType == "Student") "Message - Tutor"
                    else "Message - Student"
                notifLine1 = "${notif.notifDate} | ${notif.notifTime}"
                notifLine2 =
                    if (userType == "Student") "Tutor: " + notif.otherName
                    else "Student: " + notif.otherName
                notifLine3 =
                    if (userType == "Student") "A tutor has reached out"
                    else "A student has reached out"
            } else if (notif.notifType == "Deactivated") {
                subjMessage = "Account - Deactivated"
                notifLine1 = "${notif.notifDate} | ${notif.notifTime}"
                notifLine2 = "An Admin has deactivated your account"
                notifLine3 = notif.comments
            } else if (notif.notifType == "Approved") {
                subjMessage = "Account - Approved"
                notifLine1 = "${notif.notifDate} | ${notif.notifTime}"
                notifLine2 = "Welcome to ChalkItUp!"
                notifLine3 = notif.comments
            }

            Column(
                modifier = Modifier
                    .width(256.dp)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(subjMessage, fontSize = 20.sp,
                    modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                Text(notifLine1, fontSize = 14.sp, color = Color.Gray)
                Text(notifLine2, fontSize = 14.sp, color = Color.Gray)
                Text(notifLine3, fontSize = 14.sp, color = Color.Gray)
            }
        }

    Spacer(modifier = Modifier.height(10.dp))

    }

}

// Notification with more details.
@Composable
fun NotifLargeForm(
    notificationViewModel: NotificationViewModel = viewModel(),
    navController: NavController,
    notif: NotifClass,
    onDismiss: () -> Unit
){
    val userType by notificationViewModel.userType.collectAsState()
    val profilePictureUrl by notificationViewModel.profilePictureUrl.observeAsState()
    LaunchedEffect(Unit){
        notificationViewModel.loadProfilePicture(notif.otherID)
    }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },

        // Handles: Sessions (Booked/ Rescheduled/ Cancelled), Update, Messages
        //title = { Text(text = "Notification Details") },
        text = {
            Column {
                when (notif.notifType) {
                    "Session" -> {
                        Text(text = "$userType Session ${notif.sessType}",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        if (userType == "Tutor") {
                            Text(
                                text = "Student:              ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Tutor:              ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // Profile Picture
                            AsyncImage(
                                model = profilePictureUrl ?: R.drawable.chalkitup,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Gray, CircleShape)
                                    .clickable {
                                        navController.navigate("profile/${notif.otherID}")
                                    }
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = notif.otherName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold)
                        }
//
                        Row {
                            Text(text = "Notification Date:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = "${notif.notifTime} ${notif.notifDate}")
                        }
                        //Text(text = "Notification Time & Date: ${notif.notifTime} ${notif.notifDate}")
                        Row {
                            Text(text = "Session Time:         ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = notif.sessTime)
                        }
                        //Text(text = "Session Time: ${notif.sessTime}")
                        Row {
                            Text(text = "Session Date:          ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = notif.sessDate)
                        }
                        //Text(text = "Session Date: ${notif.sessDate}")
                        Row {
                            Text(
                                text = "Subject:                    ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = notif.subject.substringBefore(" ") + " " + notif.grade + " " + notif.spec)
                        }
                        //Text(text = "Subject: ${notif.subject}")
                        //Text(text = "Grade: ${notif.grade}")
                        //Text(text = "Specialization: ${notif.spec}")
                        Row {
                            Text(text = "Price:                        ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = notif.price)
                        }
                        //Text(text = "Price: ${notif.price}")
                        Row {
                            Text(text = "Mode:                        ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = notif.mode)
                        }
                        //Text(text = "Location: ${notif.mode}")
                        if (notif.comments != "") {
                            Text(
                                text = "Comments:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = notif.comments)
                        }
                    }
                    "Update" -> {
                        Text(text = "$userType Account Update",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text(text = "Notification Date:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = "${notif.notifTime} ${notif.notifDate}")
                        }
                        if (notif.comments != "") {
                            Text(
                                text = "Comments:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = notif.comments)
                        }
                    }
                    "Messages" -> {
                        Text(text =
                        if (userType == "Student") "Message - Tutor"
                        else "Message - Student",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text(text = "Notification Date:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = "${notif.notifTime} ${notif.notifDate}")
                        }
                        Text(text =
                        if (userType == "Student") "A tutor has reached out to you"
                        else "A student has reached out to you")
                    }
                    "Deactivated" -> {
                        Text(text = "Account - Deactivated",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text(text = "Notification Date:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = "${notif.notifTime} ${notif.notifDate}")
                        }
                        if (notif.comments != "") {
                            Text(
                                text = "Comments:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = notif.comments)
                        }
                    }
                    "Approved" -> {
                        Text(text = "Account - Approved",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text(text = "Notification Date:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text(text = "${notif.notifTime} ${notif.notifDate}")
                        }
                        if (notif.comments != "") {
                            Text(
                                text = "Comments:   ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = notif.comments)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                }
            ) { Text("Close") }
        }
    )
}
