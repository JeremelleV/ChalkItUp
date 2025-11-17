package com.example.chalkitup.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chalkitup.R
import com.example.chalkitup.lifecycle.AppLifecycleObserver
import com.example.chalkitup.ui.viewmodel.CertificationViewModel
import com.example.chalkitup.ui.viewmodel.ProfileViewModel
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import com.example.chalkitup.domain.model.Certification
import com.example.chalkitup.domain.model.Interest
import com.example.chalkitup.domain.model.ProgressItem

/**
 * Composable function for the Profile Screen.
 *
 * This screen displays the user's profile information, including their name, email, location, bio,
 * certifications, academic performance (for students), and subjects they can tutor (for tutors).
 * The profile screen also includes the ability to view and download certifications.
 *
 * @param profileViewModel The ViewModel responsible for handling user profile data.
 * @param certificationViewModel The ViewModel responsible for handling certifications and file downloads.
 * @param navController The NavController for navigating between screens.
 */
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    certificationViewModel: CertificationViewModel,
    navController: NavController,
    targetedUser: String // To view another user's profile, other user's ID is passed
) {
    //------------------------------VARIABLES----------------------------------------------

    // Observing state changes for user profile data, tutor status, certifications, and profile picture URL.
    val userProfile by profileViewModel.userProfile.observeAsState()
    val isTutor by profileViewModel.isTutor.observeAsState()
    val certifications by certificationViewModel.certifications.collectAsState()
    val profilePictureUrl by profileViewModel.profilePictureUrl.observeAsState()

    // Accessing starting price and experience from userprofile
    val startingPrice = userProfile?.startingPrice ?: ""
    val experience = userProfile?.experience ?: ""


    // Scroll state.
    val scrollState = rememberScrollState()

    // Context to access system resources, such as accessing file URIs.
    val context = LocalContext.current

    // File URI for the certification that the user intends to open.
    val fileUri by certificationViewModel.fileUri.observeAsState()

    // Gradient brush for the screen's background.
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface //95% white
        )
    )

    var showReportDialog by remember { mutableStateOf(false) }

    //------------------------------VARIABLES-END---------------------------------------------

    // Trigger to reload user profile when the profile screen is launched.
    LaunchedEffect(Unit) {
        println("Loading user profile for $targetedUser")
        profileViewModel.loadUserProfile(targetedUser) // Fetches the user profile when entering the profile screen
        certificationViewModel.getCertifications(targetedUser) // Fetches certifications when entering the profile screen
    }

    LaunchedEffect(isTutor) {
        if (isTutor == true) {
            if(targetedUser.isNotEmpty()) {
                profileViewModel.startListeningForPastSessions(targetedUser)
            } else {
                profileViewModel.startListeningForPastSessions()
            }
        }
    }

    val totalSessions by profileViewModel.totalSessions.collectAsState()
    val totalHours by profileViewModel.totalHours.collectAsState()
    val formattedHours = formatTotalHours(totalHours * 60) // Convert minutes to decimal hours


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if(targetedUser.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Report button
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_report_gmailerrorred_24),
                            contentDescription = "Report button",
                            modifier = Modifier.size(30.dp),
                        )
                    }
                    if (showReportDialog) {
                        ReportDialog(
                            onDismiss = { showReportDialog = false },
                            viewModel = profileViewModel,
                            targetedUser
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Row to align profile picture with icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Icon
                Image(
                    painter = painterResource(id = R.drawable.eraser_reading),
                    contentDescription = "Eraser",
                    modifier = Modifier.size(110.dp)
                )

                // Profile Picture
                AsyncImage(
                    model = profilePictureUrl ?: R.drawable.chalkitup,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )

                // Right Icon
                Image(
                    painter = painterResource(id = R.drawable.chalk2),
                    contentDescription = "Chalk Icon 2",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            userProfile?.let {
                Text(
                    text = "${it.firstName} ${it.lastName}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(it.email)

                if (isTutor == true) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Price and Experience
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Starting Price", fontWeight = FontWeight.Bold)
                            Text(
                                "${startingPrice}/hour",
                                color = Color(0xFF06C59C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Experience", fontWeight = FontWeight.Bold)
                            Text(
                                "$experience Years",
                                color = Color(0xFF06C59C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    //Bottom line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF06C59C),
                                        Color.Transparent
                                    )
                                )
                            )

                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                //Bio
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(
                            width = 3.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF2196F3))
                            ),
                            shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = it.bio.ifEmpty { "" },
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (targetedUser.isEmpty()) {
                // Notifications Button
                Button(
                    onClick = { navController.navigate("notifications") },
                    modifier = Modifier
                        .height(55.dp)
                        .width(400.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF06C59C), Color(0xFF007BFF))
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            // Notification Bell Icon
                            Icon(
                                painter = painterResource(id = R.drawable.notifications_foreground),
                                contentDescription = "Notification Bell",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(80.dp)
                                    .fillMaxHeight()

                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Text: "Notifications"
                            Text(
                                text = "Notifications",
                                fontSize = 19.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Right-Side Oval Arrow Button
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(80.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFF06C59C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Arrow",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                //------------------------------TUTOR-SPECIFIC----------------------------------------------

                if (isTutor == true) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // ------------- Subjects Offered (Now Swipe-able) -------------
                    userProfile?.let { profile ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Subjects Offered", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(profile.subjects.distinctBy { it.subject }) { subject ->
                                var showAllSubjectInfo by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                        .clickable(onClick = { showAllSubjectInfo = !showAllSubjectInfo })
                                ) {
                                    // Temporary icons for subjects
                                    val subjectIcon = when (subject.subject) {
                                        "Math" -> R.drawable.ic_math2
                                        "Physics" -> R.drawable.ic_physics2
                                        "Chemistry" -> R.drawable.ic_chemistry2
                                        "Social" -> R.drawable.ic_social2
                                        "English" -> R.drawable.ic_english2
                                        "Science" -> R.drawable.ic_science2
                                        "Biology" -> R.drawable.ic_biology
                                        else -> R.drawable.chalkitup
                                    }

                                    Image(
                                        painter = painterResource(id = subjectIcon),
                                        contentDescription = subject.subject,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.7f))
                                            .align(Alignment.BottomCenter)
                                    ) {
                                        Column {
                                            Text(
                                                text = subject.subject,// + " " + subject.grade + " " + subject.specialization,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                            if (showAllSubjectInfo) {
                                                for (item in profile.subjects) {
                                                    if (item.subject == subject.subject) {
                                                        Text(
                                                            text = subject.subject + " " +item.grade + " " + item.specialization,
                                                            color = Color.White,
                                                            fontSize = 14.sp,
                                                            modifier = Modifier.padding(4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        Spacer(modifier = Modifier.height(42.dp))


                        Text("Certifications & Qualifications", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        // If no certifications found, display a message.
                        if (certifications.isEmpty()) {
                            Text("No certifications found.")
                        } else {
                            // Display certifications in a grid.
                            CertificationGrid(
                                certifications,
                                onItemClick = { fileName ->
                                    certificationViewModel.downloadFileToCache(
                                        fileName,
                                        targetedUser
                                    ) // Download selected certification file
                                }
                            )
                        }

                        // Register a lifecycle observer to reset file URI when app resumes.
                        val lifecycleOwner = LocalLifecycleOwner.current
                        DisposableEffect(lifecycleOwner) {
                            val observer = AppLifecycleObserver {
                                // Reset the fileUri when app is resumed.
                                certificationViewModel.resetFileUri()
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)

                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(observer)
                            }
                        }

                        // Observe and launch file opening if a file URI is available.
                        LaunchedEffect(fileUri) {
                            fileUri?.let { uri ->
                                certificationViewModel.openFile(
                                    context,
                                    uri
                                ) // Open the certification file
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))

                        // ------------- Achievements -------------
                        Text("Achievements", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        QualificationCard(
                            icon = Icons.Default.CheckCircle,
                            title = "Total Sessions",
                            value = "$totalSessions",
                            valueColor = Color(0xFF06C59C),
                            cardColor = Color(0xFF42A5F5)
                        )

                        QualificationCard(
                            icon = Icons.Default.Timer,
                            title = "Total Tutor Hours",
                            value = formattedHours,
                            valueColor = Color(0xFF06C59C),
                            cardColor = Color(0xFF42A5F5)
                        )

//                        QualificationCard(
//                            icon = Icons.Default.Star,
//                            title = "Overall Rating",
//                            value = "4.4/5",
//                            valueColor = Color(0xFF06C59C),
//                            cardColor = Color(0xFF42A5F5)
//                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        //Will be moving to subjects offered, keeping for now.
                        // ------------- Subjects Tutor Can Teach -------------
//                    Text("Subjects you can tutor:", fontWeight = FontWeight.Bold)
//                    val formattedSubjects = profile.subjects.map { subject ->
//                        listOf(subject.subject, subject.grade, subject.specialization)
//                            .filter { it.isNotEmpty() }
//                            .joinToString(" ") // Format subject info
//                    }
//                    Box(modifier = Modifier.heightIn(20.dp, 500.dp)) {
//                        LazyColumn {
//                            items(formattedSubjects) { subject ->
//                                Text(
//                                    text = subject,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(8.dp)
//                                )
//                            }
//                        }
//                    }
                    }

                    //------------------------------TUTOR-SPECIFIC-END---------------------------------------------
                } else {
                    //------------------------------STUDENT-SPECIFIC---------------------------------------------

                    userProfile?.let { profile ->
                        val selectedInterests = profile.interests.filter { it.isSelected }

                        if (selectedInterests.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Interests", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(selectedInterests) { interest ->
                                    Box(
                                        modifier = Modifier
                                            .size(160.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.LightGray)
                                    ) {
                                        val interestIcon = when (interest.name) {
                                            "Accounting" -> R.drawable.accounting
                                            "Agriculture" -> R.drawable.agriculture
                                            "Ancient History" -> R.drawable.ancienthistory
                                            "Animal" -> R.drawable.animal
                                            "Art" -> R.drawable.art
                                            "Art-History" -> R.drawable.arthistory
                                            "Biology" -> R.drawable.ic_biology
                                            "Business" -> R.drawable.business
                                            "Computer Science" -> R.drawable.computerscience
                                            "Cell-Biology" -> R.drawable.cellbiology
                                            "Chemistry" -> R.drawable.ic_chemistry2
                                            "Earth-Science" -> R.drawable.earthscience
                                            "English" -> R.drawable.ic_english2
                                            "Engineering" -> R.drawable.engineering
                                            "Finance" -> R.drawable.finance
                                            "French" -> R.drawable.french
                                            "Food" -> R.drawable.food
                                            "Geology" -> R.drawable.geology
                                            "Government" -> R.drawable.government
                                            "Kinesiology" -> R.drawable.kinesiology
                                            "Language" -> R.drawable.language
                                            "Legal" -> R.drawable.legal
                                            "Marketing" -> R.drawable.marketing
                                            "Math" -> R.drawable.ic_math2
                                            "Medical Science" -> R.drawable.medicalscience
                                            "Music" -> R.drawable.music
                                            "Nutrition" -> R.drawable.nutrition
                                            "Physics" -> R.drawable.ic_physics2
                                            "Psychology" -> R.drawable.psychology
                                            "Social Studies" -> R.drawable.ic_social2
                                            "Physical Activity" -> R.drawable.physicalactivity
                                            "Zoology" -> R.drawable.zoology
                                            else -> R.drawable.chalkitup
                                        }

                                        Image(
                                            painter = painterResource(id = interestIcon),
                                            contentDescription = interest.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.7f))
                                                .align(Alignment.BottomCenter)
                                        ) {
                                            Text(
                                                text = interest.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                "No Interests Have been Listed",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    //------------------------------Academic Performance---------------------------------------------
                    val addedProgress: MutableList<Pair<String, String>> = mutableListOf()
                    userProfile?.let {
                        Spacer(modifier = Modifier.height(34.dp))
                        Text(
                            "Academic Performance", fontSize = 20.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        it.progress.forEach { progress ->
                            if (progress.title.isNotEmpty() && progress.grade.isNotEmpty()) {
                                addedProgress.add(progress.title to progress.grade)
                            }
                        }

                        if (addedProgress.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                addedProgress.forEach { (title, grade) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color(0xFF42A5F5),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 19.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    Color(0xFF06C59C),
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = grade,
                                                fontSize = 20.sp,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                "No Progress Update",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun ReportDialog(onDismiss: () -> Unit, viewModel: ProfileViewModel, userID: String) {
    var reportReason by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report User") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Are you sure you want to report this user?")
                OutlinedTextField(
                    value = reportReason,
                    onValueChange = { reportReason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.reportUser(userID, reportReason, onSuccess = {
                        Toast.makeText(context, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    })
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06C59C),
                )
            ) {
                Text("Report")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF54A4FF),
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

// Qualification card using for Tutors Achievements, currently not editable by user, using it right now more for looks.
@Composable
fun QualificationCard(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color,
    cardColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(cardColor, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .background(valueColor, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun CertificationGrid(
    certifications: List<Certification>,
    onItemClick: (String) -> Unit
) {
    Box(modifier = Modifier.heightIn(max = 400.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(certifications) { certification ->
                CertificationItem(
                    certification,
                    onItemClick = { fileName ->
                        onItemClick(fileName) // Handle item click
                    }
                )
            }
        }
    }
}

/**
 * Composable function to display an individual certification item.
 *
 * This function displays either an image or a file icon for each certification
 * based on the file type (e.g., image or non-image file).
 *
 * @param certification The certification item to be displayed.
 * @param onItemClick Function to handle click events on the certification item.
 */
@Composable
fun CertificationItem(
    certification: Certification,
    onItemClick: (String) -> Unit
) {
    val fileName = certification.fileName
    val fileExtension = fileName.substringAfterLast('.', "").lowercase()

    Box(
        modifier = Modifier
            .clickable {
                onItemClick(fileName) // Handle item click
            }
            .size(120.dp)
            .padding(4.dp)
            .background(Color(0xFF42A5F5), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (fileExtension in listOf("jpg", "jpeg", "png", "gif", "bmp")) {
            // Display image if the file is an image type
            AsyncImage(
                model = certification.fileUrl,
                contentDescription = "Certification Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Show file icon & filename for non-image files
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_insert_drive_file_24),
                    contentDescription = "File Icon",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
                Text(
                    text = fileName,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

fun formatTotalHours(totalMinutes: Double): String {
    val hours = (totalMinutes / 60).toInt()
    return "$hours hours"
}

@Composable
fun InterestItem(
    interest: Interest,
    onInterestChange: (Boolean) -> Unit,
) {
    val selectedButtonColor = Color(0xFF54A4FF)
    val defaultButtonColor = Color.LightGray
    val errorButtonColor = Color.Red
    var isSelected by remember { mutableStateOf(interest.isSelected) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subject Selection Button and Dropdown
        Button(
            onClick = { onInterestChange(!interest.isSelected)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (interest.isSelected) selectedButtonColor else defaultButtonColor),
            //subjectError -> errorButtonColor
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(interest.name)
        }
        //---------------
    }
}


@Composable
fun ProgressInput(
    progressItem: ProgressItem,
    onProgressChange: (String, String) -> Unit,
    onRemove:() ->Unit
) {
    val selectedButtonColor = Color(0xFF54A4FF)
    val defaultButtonColor = Color.LightGray
    val errorButtonColor = Color.Red
    var title by remember { mutableStateOf(progressItem.title) }
    var grade by remember { mutableStateOf(progressItem.grade) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                onProgressChange(title, grade)},
            label = { Text("Title") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = grade,
            onValueChange = {
                grade= it
                onProgressChange(title, grade)},
            label = { Text("Grade") },
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = Color.Gray
            )
        }
    }
}