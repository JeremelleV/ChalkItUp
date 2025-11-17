package com.example.chalkitup.ui.screens

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chalkitup.R
import com.example.chalkitup.ui.components.SelectedFileItem
import com.example.chalkitup.ui.components.SubjectGradeItem
import com.example.chalkitup.ui.components.TutorSubject
import com.example.chalkitup.ui.components.TutorSubjectError
import com.example.chalkitup.ui.components.validateTutorSubjects
import com.example.chalkitup.ui.viewmodel.CertificationViewModel
import com.example.chalkitup.ui.viewmodel.EditProfileViewModel

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.graphics.Brush
import com.example.chalkitup.domain.model.Interest
import com.example.chalkitup.domain.model.ProgressItem


/**
 * EditProfileScreen
 *
 * This composable function displays the Edit Profile screen, allowing users to
 * update their profile information, including name, bio, location, subjects
 * (for tutors), and certifications. Users can also change their profile
 * picture. Form validation ensures required fields are filled before saving.
 *
 * @param navController - The NavController for navigation between screens.
 * @param editProfileViewModel - ViewModel handling user profile data and profile picture upload.
 * @param certificationViewModel - ViewModel managing certification file selection, upload, and removal.
 */
@Composable
fun EditProfileScreen(
    navController: NavController,
    editProfileViewModel: EditProfileViewModel,
    certificationViewModel: CertificationViewModel,
) {
    //------------------------------VARIABLES----------------------------------------------

    // Scroll state for vertical scrolling.
    val scrollState = rememberScrollState()

    // Observing the user's profile data from the ViewModel.
    val userProfile by editProfileViewModel.userProfile.observeAsState()

    // Determines if the user is a tutor based on their profile information.
    val isTutor by remember(userProfile) {
        derivedStateOf { userProfile?.userType == "Tutor" }
    }

    // Observing the profile picture URL from the ViewModel.
    val profilePictureUrl by editProfileViewModel.profilePictureUrl.observeAsState()

    // Profile picture selection launcher.
    val launcherPFP = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { editProfileViewModel.uploadProfilePictureTemporarily(it) }
    }

    // State variables for profile fields.
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tutorSubjects by remember { mutableStateOf<List<TutorSubject>>(emptyList()) } // To store selected subjects
    var bio by remember { mutableStateOf("") }
    var startingPrice by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }

    // Lists for available subjects and grade levels.
    val availableSubjects = listOf("Math", "Science", "English", "Social", "Biology", "Physics", "Chemistry")
    val availableGradeLevels = listOf("7","8","9","10","20","30")
    val availableGradeLevelsBPC = listOf("20","30")
    val grade10Specs = listOf("- 1","- 2","Honours")
    val grade1112Specs = listOf("- 1","- 2","AP","IB")
    val availablePrice = listOf("$20/hr", "$25/hr", "$30/hr", "$35/hr", "$40/hr", "$45/hr", "$50/hr", "$55/hr", "$60/hr", "$65/hr", "$70/hr", "$75/hr", "$80/hr", "$85/hr", "$90/hr", "$95/hr", "$100/hr", "$105/hr", "$110/hr", "$115/hr", "$120/hr")

    // Error states for form validation. -> all check only for empty fields
    var tutorSubjectErrors by remember { mutableStateOf<List<TutorSubjectError>>(emptyList()) }
    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }
    //val progress_item = remember { mutableListOf<String>()}
    //val progress_grade =remember { mutableListOf<String>() }
    val mainHandler= Handler(Looper.getMainLooper())
    var updatedInterests by remember { mutableStateOf<List<Interest>>(emptyList()) }
    var progress = remember { mutableStateListOf<ProgressItem>() }
    var interests = remember { mutableStateListOf<Interest>() }
    //var interests by remember { mutableStateOf<List<Interest>>(emptyList()) }

    // Gradient brush for the screen's background.
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF06C59C),//Color(0xFF54A4FF), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface //95% white
        )
    )

    // Initialize profile fields when the user profile data changes.
    LaunchedEffect(userProfile) {
        userProfile?.let {
            firstName = it.firstName
            lastName = it.lastName
            email = it.email
            tutorSubjects = it.subjects
            bio = it.bio
            startingPrice = it.startingPrice
            experience = it.experience
        }
    }

    LaunchedEffect(Unit) {
        certificationViewModel.getCertifications()
    }

    // Context for accessing resources and system services.
    val context = LocalContext.current

    // State to hold the URIs of selected files for certification uploads.
    val selectedFiles by certificationViewModel.selectedFiles.collectAsState()

    // Launcher for selecting multiple files from the device's storage.
    val launcherCT =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                certificationViewModel.addSelectedFiles(uris)
            }
        }

    //------------------------------VARIABLES-END----------------------------------------------

    // Main layout.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(modifier = Modifier
            .padding(24.dp)
            .verticalScroll(scrollState)
            .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF42A5F5)
            )

            // Circular profile picture with a click to change.
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = profilePictureUrl ?: R.drawable.editprofilepicture,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { launcherPFP.launch("image/*") }
                )
            }

            // First Name input field.
            OutlinedTextField(
                value = firstName,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    firstName = it
                    if (firstName.isNotBlank()) firstNameError =
                        false // Clears the error if input is provided.
                },
                label = { Text("First Name") },
                supportingText = { if (firstNameError) Text("First name cannot be blank") }, // Shows an error message if there's an error.
                isError = firstNameError // Indicates that there's an error if firstNameError is true.
            )

            // Last Name input field.
            OutlinedTextField(
                value = lastName,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    lastName = it
                    if (lastName.isNotBlank()) lastNameError =
                        false // Clears the error if input is provided.
                },
                label = { Text("Last Name") },
                supportingText = { if (lastNameError) Text("Last name cannot be blank") }, // Shows an error message if there's an error.
                isError = lastNameError // Indicates that there's an error if lastNameError is true.
            )

            // Bio input field.
            OutlinedTextField(
                value = bio,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { bio = it },
                label = { Text("Bio") }
            )

            if (isTutor) {
                OutlinedTextField(
                    value = startingPrice,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { startingPrice = it },
                    label = { Text("Starting Price per Hour") }
                )
                OutlinedTextField(
                    value = experience,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { experience = it },
                    label = { Text("Experience (Years)") }
                )
            }


            // Tutor-Specific Fields.
            if (isTutor) {
                Spacer(modifier = Modifier.height(16.dp))

                if (subjectError) {
                    Text(
                        "You must be able to teach at least 1 subject",
                        color = Color.Red
                    )
                }

                Text(
                    text = "Subjects You Teach",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Tap the  +  to add a subject",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(50.dp))

                    // Add Subject button.
                    IconButton(
                        onClick = {
                            tutorSubjects =
                                tutorSubjects + TutorSubject("", "", "", "") // Add empty entry
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonColors(
                            Color(0xFF06C59C),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF06C59C),
                            disabledContentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Subject",
                            tint = Color.White
                        )
                    }
                }

                // Default text if no subjects added
                if (tutorSubjects.isEmpty()) {
                    Text(
                        text = "No subjects added",
                        color = Color.Gray
                    )
                }
                // Display list of subjects the tutor teaches.
                Box(modifier = Modifier.heightIn(20.dp, 500.dp)) {
                    LazyColumn {
                        itemsIndexed(tutorSubjects) { index, tutorSubject ->
                            SubjectGradeItem(
                                tutorSubject = tutorSubject, // Pass the entire TutorSubject object
                                availableSubjects = availableSubjects,
                                availableGradeLevels = availableGradeLevels,
                                availableGradeLevelsBPC = availableGradeLevelsBPC,
                                availablePrice = availablePrice,
                                grade10Specs = grade10Specs,
                                grade1112Specs = grade1112Specs,
                                onSubjectChange = { newSubject ->
                                    tutorSubjects = tutorSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(subject = newSubject)
                                    }
                                },
                                onGradeChange = { newGrade ->
                                    tutorSubjects = tutorSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(grade = newGrade)
                                    }
                                },
                                onSpecChange = { newSpec ->
                                    tutorSubjects = tutorSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(specialization = newSpec)
                                    }
                                },
                                onPriceChange = { newPrice ->
                                    tutorSubjects = tutorSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(price = newPrice)
                                    }
                                },

                                onRemove = {
                                    tutorSubjects =
                                        tutorSubjects.toMutableList().apply { removeAt(index) }
                                },
                                subjectError = tutorSubjectErrors.getOrNull(index)?.subjectError
                                    ?: false,
                                gradeError = tutorSubjectErrors.getOrNull(index)?.gradeError ?: false,
                                specError = tutorSubjectErrors.getOrNull(index)?.specError ?: false,
                                priceError = tutorSubjectErrors.getOrNull(index)?.priceError ?: false,
                                duplicateError = tutorSubjectErrors.getOrNull(index)?.duplicateError ?: false
                            )
                        }
                    }
                }

                // Certification file upload section.
                Text("Your Certifications", style = MaterialTheme.typography.titleMedium)

                // Button to trigger file upload.
                Button(
                    onClick = { launcherCT.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06C59C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload a file",
                    color = Color.White)
                }

                // Display selected files if any.
                if (selectedFiles.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedFiles.forEach { uri ->
                            val fileName =
                                certificationViewModel.getFileNameFromUri(context, uri)
                            SelectedFileItem(           // images not being displayed if being retrieved from db
                                fileName = fileName,
                                fileUri = uri,
                                onRemove = { certificationViewModel.removeSelectedFile(uri) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(text = "No files selected.", color = Color.Gray)
                }

            } else {
                Spacer(modifier = Modifier.height(16.dp))

                LaunchedEffect(userProfile) {
                    userProfile?.let {
                        progress.clear()
                        progress.addAll(it.progress.map { progress -> progress.copy() }) // Ensures a separate copy
                    }
                }
                Button(onClick = {
                    val newItem = ProgressItem("", "")
                    progress.add(newItem)
                },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06C59C)),
                    shape = RoundedCornerShape(8.dp)
                )
                { Text("add progress") }

                Box(modifier = Modifier.heightIn(20.dp, 500.dp)) {
                    LazyColumn {
                        itemsIndexed(progress) { index, item ->
                            ProgressInput(
                                progressItem = item,
                                onProgressChange = { updatedTitle, updatedGrade ->
                                    progress=progress.apply{
                                        this[index]=item.copy(title = updatedTitle, grade = updatedGrade)}},
                                onRemove={
                                    progress=progress.apply{removeAt(index)}}
                            )
                        }
                    }
                }
                //------------------INTERESTS--------------------------------------
                Spacer(modifier = Modifier.height(16.dp))

                Text("Select your Interests", style = MaterialTheme.typography.titleMedium)
                LaunchedEffect(userProfile) {
                    userProfile?.let {
                        interests.clear()
                        interests.addAll(it.interests.map { interest -> interest.copy() }) // Ensures a separate copy
                    }
                }
                Box(modifier = Modifier.heightIn(20.dp, 300.dp)) {
//                    LazyColumn {
//                        itemsIndexed(interests) { index, interest ->
//                            InterestItem(
//                                interest = interest,
//                                onInterestChange = { isSelected ->
//                                    interests[index] = interest.copy(isSelected = isSelected)
//                                }
//                            )
//                        }
//                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2), // 3 columns
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp), // Spacing between rows
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between columns
                    ) {
                        items(interests.size) { index ->
                            InterestItem(
                                interest = interests[index],
                                onInterestChange = { isSelected ->
                                    interests[index] = interests[index].copy(isSelected = isSelected)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(
                    onClick = {
                        // Error checks

                        tutorSubjectErrors = validateTutorSubjects(tutorSubjects)
                        subjectError = ((isTutor) && (tutorSubjects.isEmpty()))
                        firstNameError = firstName.isEmpty()
                        lastNameError = lastName.isEmpty()

                        if (!(tutorSubjectErrors.any { it.subjectError || it.gradeError || it.specError || it.priceError || it.duplicateError }) &&
                            !subjectError && !firstNameError && !lastNameError
                        ) {
                            //val  finalInterests=updatedInterests.toList()
                            editProfileViewModel.updateProfile(
                                firstName,
                                lastName,
                                tutorSubjects,
                                bio,
                                startingPrice,
                                experience,
                                progress,
                                interests
                            )
                            certificationViewModel.updateCertifications()
                            navController.navigate("profile/") // Navigate back to profile
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06C59C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Save Changes",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        editProfileViewModel.cancelProfilePictureChange()
                        navController.navigate("profile/") // Navigate back to profile
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06C59C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel",
                    color = Color.White)
                }
            }
        }
    }
}
