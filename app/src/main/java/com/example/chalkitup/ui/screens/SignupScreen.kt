package com.example.chalkitup.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chalkitup.ui.viewmodel.AuthViewModel
import com.example.chalkitup.ui.viewmodel.CertificationViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.IconButtonColors
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.chalkitup.R
import com.example.chalkitup.ui.components.SelectedFileItem
import com.example.chalkitup.ui.components.SubjectGradeItem
import com.example.chalkitup.ui.components.TutorSubject
import com.example.chalkitup.ui.components.TutorSubjectError
import com.example.chalkitup.ui.components.validateTutorSubjects
import android.util.Log


/**
 * Composable function for the Signup Screen.
 *
 * This screen allows users to create a new account by providing their details,
 * selecting their user type (Student or Tutor), and agreeing to the Terms and Conditions.
 *
 * @param authViewModel The ViewModel responsible for authentication-related operations.
 * @param certificationViewModel The ViewModel responsible for handling certifications and file uploads.
 * @param navController The NavController for navigating between screens.
 */

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    certificationViewModel: CertificationViewModel,
    navController: NavController
) {
    //------------------------------VARIABLES----------------------------------------------

    // Scroll states for the main form and the Terms & Conditions box.
    val scrollState = rememberScrollState() // Main form scroll state - entire screen

    // Context for accessing resources and system services.
    val context = LocalContext.current

    // State to hold the URIs of selected files for certification uploads.
    val selectedFiles by certificationViewModel.selectedFiles.collectAsState()

    // Launcher for selecting multiple files from the device's storage.
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                certificationViewModel.addSelectedFiles(uris)
            }
        }

    // State variables for user input fields.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf<UserType?>(null) } // Track user type: Student or Tutor
    var tutorSubjects by remember { mutableStateOf<List<TutorSubject>>(emptyList()) } // To store selected subjects

    // State variables to track input field errors.
    var emailError by remember { mutableStateOf(false) }        // Tracks Empty Field
    var invalidEmailError by remember { mutableStateOf(false) } // Tracks Empty Field
    var passwordError by remember { mutableStateOf(false) }     // Tracks Empty Field
    var confirmPasswordError by remember { mutableStateOf(false) }   // Tracks Empty Field
    var passGreaterThan6Error by remember { mutableStateOf(false) }  // Password Specific Error
    var passIncludesNumError by remember { mutableStateOf(false) }   // Password Specific Error
    var passIncludesLowerError by remember { mutableStateOf(false) } // Password Specific Error
    var passMatchError by remember { mutableStateOf(false) }         // Password Specific Error
    var firstNameError by remember { mutableStateOf(false) }    // Tracks Empty Field
    var lastNameError by remember { mutableStateOf(false) }     // Tracks Empty Field
    var userTypeError by remember { mutableStateOf(false) }     // Tracks Empty Field
    var subjectError by remember { mutableStateOf(false) }      // Tracks Empty Field
    //var termsError by remember { mutableStateOf(false) }        // Tracks Not Checked

    // State to track errors in tutor subject selections.
    var tutorSubjectErrors by remember { mutableStateOf<List<TutorSubjectError>>(emptyList()) }

    // Lists for subject and grade level selections.
    val availableSubjects = listOf("Math", "Science", "English", "Social", "Biology", "Physics", "Chemistry")
    val availableGradeLevels = listOf("7", "8", "9", "10", "20", "30")
    val availableGradeLevelsBPC = listOf("20", "30")
    val grade10Specs = listOf("- 1", "- 2", "Honours")
    val grade1112Specs = listOf("- 1", "- 2", "AP", "IB")
    val availablePrice = listOf("$20/hr", "$25/hr", "$30/hr", "$35/hr", "$40/hr", "$45/hr", "$50/hr", "$55/hr", "$60/hr", "$65/hr", "$70/hr", "$75/hr", "$80/hr", "$85/hr", "$90/hr", "$95/hr", "$100/hr", "$105/hr", "$110/hr", "$115/hr", "$120/hr")


    //val availableInterests = listOf("Art History", "Genetics", "Animals", "Astronomy", "Environment", "Health Science")

    // Icons for password requirement checks.
    val passCheckIcon = Icons.Default.CheckCircle
    val passFailIcon = Icons.Default.Clear

    /* didnt like.
    val AtkinsonFont = FontFamily(
        Font(R.font.atkinson_regular, FontWeight.Normal),
        Font(R.font.atkinson_light, FontWeight.Light),
        Font(R.font.atkinson_bold, FontWeight.Bold),
        Font(R.font.atkinson_extrabold, FontWeight.ExtraBold)
    )
    */

    // Font family
    val MontserratFont = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_semibold, FontWeight.SemiBold),
        Font(R.font.montserrat_bold, FontWeight.Bold)
    )

    // Gradient brush for the screen's background.
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface //95% white
        )
    )

    //------------------------------VARIABLES-END----------------------------------------------

    // Main layout for the screen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        // Back Button. Couldn't get it to look like the login page one.. will reattempt soon -Kaitlyn
//        IconButton(
//            onClick = { navController.navigate("start") },
//            modifier = Modifier
//                .size(58.dp)
//                .padding(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                contentDescription = "Back",
//                tint = Color.Black,
//                modifier = Modifier.size(106.dp)
//            )
//        }

        // Main content column.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(170.dp))

            // Title for the sign-up screen.
            Text(
                "Sign Up",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ) // Not using rn : fontFamily = MontserratFont, -Kaitlyn

            Spacer(modifier = Modifier.height(75.dp))

            // User type selection prompt.
            Text(
                if (userTypeError) "Please select an account type"
                else "Select account type to get started",
                fontFamily = MontserratFont,
                fontSize = 14.sp,
                color = if (userTypeError) Color.Red // Changes the text color to red if there's an error.
                else Color.Gray
            ) // Otherwise, the text is gray.

            // Row that contains buttons for selecting user type (Student or Tutor).
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Button for selecting the "Student" user type.
                Button(
                    onClick = {
                        userType = UserType.Student
                    }, // Sets the user type to Student when clicked.
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                        if (userType == UserType.Student) Color(0xFF06C59C) // Green color if selected.
                        else if (userTypeError) Color.Red // Red if there's an error.
                        else Color.LightGray // Light gray when not selected.
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Student", color = Color.DarkGray, fontSize = 16.sp) }

                // Button for selecting the "Tutor" user type.
                Button(
                    onClick = {
                        userType = UserType.Tutor
                    }, // Sets the user type to Tutor when clicked.
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userType == UserType.Tutor) Color(0xFF54A4FF) // Blue color if selected.
                        else if (userTypeError) Color.Red // Red if there's an error.
                        else Color.LightGray // Light gray when not selected.
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Tutor", color = Color.DarkGray, fontSize = 16.sp) }

                // Resets userTypeError if a userType is selected.
                if (userType != null) {
                    userTypeError = false
                }
            }

            // Outlined text field for entering the user's first name.
            OutlinedTextField(
                value = firstName, // Binds the firstName variable to the input value.
                onValueChange = {
                    firstName = it // Updates the firstName when the input changes.
                    if (firstName.isNotBlank()) firstNameError =
                        false // Clears the error if input is provided.
                },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { if (firstNameError) Text("Please enter your First Name") }, // Shows an error message if there's an error.
                isError = firstNameError // Indicates that there's an error if firstNameError is true.
            )

            // Outlined text field for entering the user's last name.
            OutlinedTextField(
                value = lastName, // Binds the lastName variable to the input value.
                onValueChange = {
                    lastName = it // Updates the lastName when the input changes.
                    if (lastName.isNotBlank()) lastNameError =
                        false // Clears the error if input is provided.
                },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { if (lastNameError) Text("Please enter your Last Name") }, // Shows an error message if there's an error.
                isError = lastNameError // Indicates that there's an error if lastNameError is true.
            )

            // Outlined text field for entering the user's email address.
            OutlinedTextField(
                value = email, // Binds the email variable to the input value.
                onValueChange = {
                    email = it // Updates the email when the input changes.
                    if (email.isNotBlank()) {
                        emailError = false // Clears the error if input is provided.
                        invalidEmailError = false // Clears invalid email error if input is valid.
                    }
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (emailError) Text("Please enter your Email") // Shows an error message if emailError is true.
                    else if (invalidEmailError) Text("Invalid email address") // Shows an invalid email error if invalidEmailError is true.
                    else Text("") // No error message when email is valid.
                },
                isError = emailError || invalidEmailError
            )

            // Outlined text field for entering the user's password.
            OutlinedTextField(
                value = password, // Binds the password variable to the input value.
                onValueChange = {
                    password = it // Updates the password when the input changes.
                    if (password.isNotBlank()) passwordError =
                        false // Clears the error if input is provided.
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { if (passwordError) Text("Please enter your Password") }, // Shows an error message if there's a password error.
                isError = passwordError || passGreaterThan6Error || passIncludesNumError || passIncludesLowerError // Indicates that there's an error based on password validity.
            )

            // Column containing password requirement checks.
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                // Row for checking password length requirement.
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon indicating the status of the password length requirement.
                    Icon(
                        imageVector = if (password.length < 6) passFailIcon else passCheckIcon, // Displays a fail or check icon.
                        contentDescription = "Password Requirement Status",
                        tint = if (passGreaterThan6Error || passwordError) Color.Red // Red if there’s an error.
                        else if (password.length < 6) Color.Gray // Gray if password is too short.
                        else Color.Green, // Green if the password meets the requirement.
                        modifier = Modifier.size(10.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        "Is 6 or more characters long", // Displays the password requirement message.
                        color = if (passGreaterThan6Error || passwordError) Color.Red // Red if there's an error.
                        else if (password.length < 6) Color.Gray // Gray if password is too short.
                        else Color.Green, // Green if the password meets the requirement.
                        fontSize = 13.sp
                    )
                    // Clears the error if the password meets the length requirement.
                    if (password.length >= 6) {
                        passGreaterThan6Error = false
                    }
                }
                // Row for checking if the password includes at least one digit.
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon indicating the status of the password digit requirement.
                    Icon(
                        imageVector = if (password.any { it.isDigit() }) passCheckIcon else passFailIcon, // Displays a fail or check icon.
                        contentDescription = "Password Requirement Status",
                        tint = if (passIncludesNumError || passwordError) Color.Red // Red if there’s an error.
                        else if (password.any { it.isDigit() }) Color.Green // Green if the password includes a digit.
                        else Color.Gray, // Gray if the password doesn't include a digit.
                        modifier = Modifier.size(10.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        "Includes at least 1 digit", // Displays the password requirement message.
                        color = if (passIncludesNumError || passwordError) Color.Red // Red if there's an error.
                        else if (password.any { it.isDigit() }) Color.Green // Green if the password includes a digit.
                        else Color.Gray, // Gray if the password doesn't include a digit.
                        fontSize = 13.sp
                    )
                    // Clears the error if the password includes at least one digit.
                    if (password.any { it.isDigit() }) {
                        passIncludesNumError = false
                    }
                }
                // Row for checking if the password includes at least one lowercase letter.
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon indicating the status of the password lowercase letter requirement.
                    Icon(
                        imageVector = if (password.any { it.isLowerCase() }) passCheckIcon else passFailIcon, // Displays a fail or check icon.
                        contentDescription = "Password Requirement Status",
                        tint = if (passIncludesLowerError || passwordError) Color.Red // Red if there's an error.
                        else if (password.any { it.isLowerCase() }) Color.Green // Green if the password includes a lowercase letter.
                        else Color.Gray, // Gray if the password doesn't include a lowercase letter.
                        modifier = Modifier.size(10.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        "Includes at least 1 lowercase letter",  // Displays the password requirement message.
                        color = if (passIncludesLowerError || passwordError) Color.Red // Red if there's an error.
                        else if (password.any { it.isLowerCase() }) Color.Green // Green if the password includes a lowercase letter.
                        else Color.Gray, // Gray if the password doesn't include a lowercase letter.
                        fontSize = 13.sp
                    )
                    // Clears the error if the password includes at least one lowercase letter.
                    if (password.any { it.isLowerCase() }) {
                        passIncludesLowerError = false
                    }
                }
            }

            // Field for confirming the password input.
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    // Update the confirm password value and reset error if the field is not blank.
                    confirmPassword = it
                    if (confirmPassword.isNotBlank()) confirmPasswordError = false
                },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { if (confirmPasswordError) Text("Please confirm your Password") }, // Error message if there is a confirmation error..
                isError = confirmPasswordError || passMatchError // Show error if passwords do not match or are empty.
            )

            // Column containing the password match validation status
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                // Row to check if the password and confirm password match
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display icon based on password match status.
                    Icon(
                        imageVector = if ((password == confirmPassword) && password.isNotEmpty()) passCheckIcon else passFailIcon,
                        contentDescription = "Password Requirement Status",
                        tint = if (passMatchError || confirmPasswordError) Color.Red
                        else if ((password != confirmPassword) || confirmPassword.isEmpty()) Color.Gray
                        else Color.Green,
                        modifier = Modifier.size(10.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    // Text showing password match status.
                    Text(
                        if (password != confirmPassword) "Passwords do not match"
                        else if (password.isEmpty()) "Password is empty"
                        else "Passwords match",
                        color = if (passMatchError || confirmPasswordError) Color.Red
                        else if ((password != confirmPassword) || confirmPassword.isEmpty()) Color.Gray
                        else Color.Green,
                        fontSize = 13.sp
                    )
                    // Reset error if passwords match.
                    if ((password == confirmPassword) && password.isNotEmpty()) {
                        passMatchError = false
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subject selection (only visible for Tutors)
            if (userType == UserType.Tutor) {
                // Display text for selecting subjects.
                Text("Select Subjects", style = MaterialTheme.typography.titleMedium)

                // Display error if no subjects have been selected.
                if (subjectError) {
                    Text(
                        "You must be able to tutor at least 1 subject",
                        color = Color.Red,
                        fontSize = 15.sp
                    )
                }

                // Button to add subjects to the tutor's list.
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tap the  +  to add a subject",
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(50.dp))

                    // Button to add a new subject to the list.
                    IconButton(
                        onClick = {
                            // Add an empty tutor subject entry.
                            subjectError = false
                            tutorSubjects =
                                tutorSubjects + TutorSubject("", "", "", "") // Add empty entry
                        },
                        modifier = Modifier.size(36.dp),
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

                // Default text if no subjects have been added yet.
                if (tutorSubjects.isEmpty()) {
                    Text(
                        text = "No subjects added",
                        color = Color.Gray
                    )
                }

                // Display list of selected subjects and their grade levels.
                Box(modifier = Modifier.heightIn(20.dp, 500.dp)) {
                    LazyColumn {
                        itemsIndexed(tutorSubjects) { index, tutorSubject ->
                            // Display each tutor subject item and its details.
                            SubjectGradeItem( // SubjectGradeItem function is defined below.
                                tutorSubject = tutorSubject,
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
                                onPriceChange = { newPrice ->
                                    tutorSubjects = tutorSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(price = newPrice)
                                    }
                                },
                                onSpecChange = { newSpec ->
                                    tutorSubjects = tutorSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(specialization = newSpec)
                                    }
                                },
                                onRemove = {
                                    tutorSubjects =
                                        tutorSubjects.toMutableList().apply { removeAt(index) }
                                    tutorSubjectErrors = tutorSubjectErrors.toMutableList().apply {
                                        if (index < size) removeAt(index)
                                    }
                                },
                                subjectError = tutorSubjectErrors.getOrNull(index)?.subjectError
                                    ?: false,
                                gradeError = tutorSubjectErrors.getOrNull(index)?.gradeError
                                    ?: false,
                                specError = tutorSubjectErrors.getOrNull(index)?.specError
                                    ?: false,
                                priceError= tutorSubjectErrors.getOrNull(index)?.priceError
                                    ?: false,
                                duplicateError = tutorSubjectErrors.getOrNull(index)?.duplicateError
                                    ?: false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Section for uploading certifications.
                Text("Upload Certifications", style = MaterialTheme.typography.titleMedium)

                // Button to trigger file upload.
                Button(
                    onClick = { launcher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06C59C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload a file",
                        color = Color.White)
                }

                // Display selected files if any.
                if (selectedFiles.isNotEmpty()) {
                    Text(text = "Selected Files:", style = MaterialTheme.typography.titleMedium)
                    Column( // Use Column instead of LazyColumn
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedFiles.forEach { uri ->
                            val fileName =
                                certificationViewModel.getFileNameFromUri(context, uri)
                            SelectedFileItem(
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

            }

            Spacer(modifier = Modifier.height(16.dp))

//            // Terms and conditions section.
//            Text("Terms and Conditions", style = MaterialTheme.typography.titleMedium)
//            // Box to display the terms and conditions text with scroll.
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .border(1.dp, Color.Gray, RoundedCornerShape(5.dp))
//                    .padding(8.dp)
//                    .verticalScroll(termsScrollState) // Separate scroll state
//            ) {
//                // Converted to an annotated string to individually format each line
//                // Easier to edit individual lines + style them.
//                Text(
//                    buildAnnotatedString {
//                        //MaterialTheme.typography.bodyMedium)
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("Terms and Conditions\n\n")
//                            append("Last Updated March 7, 2025\n\n")
//                        }
//                        append("Welcome to ChalkItUp! By signing up and using our platform," +
//                                " you agree to the following Terms and Conditions. Please read them carefully.\n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("1. Introduction\n")
//                        }
//                        append("1.1 ChalkItUp provides a platform that connects students " +
//                                "with tutors for educational sessions. By using our app, you " +
//                                "acknowledge that you have read, understood, and agreed to these terms.\n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("2. User Agreement\n")
//                        }
//                        append("2.1. By registering, you confirm that you are either (a) at least 18 years old, or (b) a minor with parental or legal guardian consent.\n" +
//                                "2.2. Parents/guardians must approve and monitor minors' use of the Platform.\n" +
//                                "2.3. Users must provide accurate, up-to-date information during registration.\n" +
//                                "2.4. We reserve the right to suspend or terminate accounts for violations of these terms.\n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("3. User Responsibilities\n")
//                        }
//                        append("3.1. Tutors must provide accurate qualifications and availability. Misrepresentation may result in account suspension\n" +
//                                "3.2. Students and parents must ensure that sessions are attended as scheduled.\n" +
//                                "3.3. Users must adhere to professional conduct and avoid inappropriate communication.\n" +
//                                "3.4. The Platform is not responsible for the academic performance of the student. \n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("4. Payments & Fees\n")
//                        }
//                        append("4.1. Tutors are paid based on completed session\n" +
//                                "4.2 Students are responsible to pay tutor within 24 hours of session completion through the method decided between the tutor and student\n\n")
//
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("5. Privacy and Data Protection\n")
//                        }
//                        append("5.1. We comply with the Personal Information Protection and Electronic Documents Act (PIPEDA) and applicable provincial privacy laws.\n" +
//                                "5.2. By using the Platform, you consent to the collection, use, and storage of personal data as outlined in our Privacy Policy.\n" +
//                                "5.3.  Users may request access to or deletion of their personal information.\n" +
//                                "5.4. We implement security measures to protect user data but cannot guarantee complete security. \n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("6. Messaging & Communication\n")
//                        }
//                        append("6.1. Tutors and students may communicate only through Platform-approved channels.\n" +
//                                "6.2. Messaging content must remain professional and educational.\n" +
//                                "6.3. We reserve the right to monitor communications to ensure compliance with child protection laws.\n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("7. Intellectual Property\n")
//                        }
//
//                        append("7.1. All content provided on the Platform, including lesson materials and resources, is either owned by or licensed to the Platform.\n" +
//                                "7.2. Users may not reproduce, distribute, or share proprietary content without permission.\n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("8. Termination of Services\n")
//                        }
//
//                        append("8.1. We reserve the right to terminate or suspend access for users who violate these Terms.\n" +
//                                "8.2. Users may delete their accounts, but payment obligations must be settled before account closure.\n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("9. Dispute Resolution\n")
//                        }
//                        append("9.1. Any disputes should first be addressed through our Support Team.\n" +
//                                "9.2. If unresolved, disputes shall be governed by the laws of Alberta, Canada and subject to Canadian courts.\n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("10. Liability Disclaimer\n")
//                        }
//                        append("10.1. The Platform is a facilitator and is not responsible for any disputes, misconduct, or learning outcomes.\n" +
//                                "10.2. We disclaim liability for technical failures, loss of data, or unauthorized access.\n" +
//                                "10.3. Users assume full responsibility for their interactions, and we encourage parental supervision for minors.\n\n")
//
//                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
//                            append("11. Amendments\n")
//                        }
//                        append("11.1. We may update these Terms from time to time, and users will be notified of significant changes.\n" +
//                                "11.2. Continued use of the Platform constitutes acceptance of revised Terms.")
//                    }
//                )
//            }

//            // Checkbox for agreeing to terms and conditions.
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.padding(top = 8.dp)
//            ) {
//                Checkbox(
//                    checked = hasAgreedToTerms,
//                    onCheckedChange = { hasAgreedToTerms = it },
//                    enabled = hasScrolledToBottom,
//                    colors = CheckboxColors(
//                        checkedCheckmarkColor = Color.White,
//                        uncheckedCheckmarkColor = Color.DarkGray,
//                        checkedBoxColor = Color(0xFF54A4FF),
//                        uncheckedBoxColor = Color.White,
//                        disabledCheckedBoxColor = Color.DarkGray,
//                        disabledUncheckedBoxColor = Color.Gray,
//                        disabledIndeterminateBoxColor = Color.DarkGray,
//                        checkedBorderColor = Color(0xFF54A4FF),
//                        uncheckedBorderColor = Color.DarkGray,
//                        disabledBorderColor = Color.DarkGray,
//                        disabledUncheckedBorderColor = Color.DarkGray,
//                        disabledIndeterminateBorderColor = Color.DarkGray
//                    )
//                )
//                Text("I have read and agree to the Terms and Conditions")
//            }

//            // Error message if terms are not agreed to.
//            if (termsError) {
//                Text(
//                    "You must agree to the Terms and Conditions before signing up",
//                    color = Color.Red,
//                    fontSize = 15.sp
//                )
//            }
//            // Reset the terms error if terms are agreed to.
//            if (hasAgreedToTerms) {
//                termsError = false
//            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sign-up button that validates input and triggers the signup process.
            Button(
                onClick = {
                    errorMessage = ""

                    // Had to do some debugging, signup was not working for me - Kaitlyn
                    Log.d("SignupDebug", "Email: $email")
                    Log.d("SignupDebug", "First Name: $firstName")
                    Log.d("SignupDebug", "Last Name: $lastName")
                    Log.d("SignupDebug", "Password Length: ${password.length}")
                    Log.d("SignupDebug", "Confirm Password: $confirmPassword")
                    Log.d("SignupDebug", "User Type: $userType")
                    // Log.d("SignupDebug", "Has Agreed to Terms: $hasAgreedToTerms")

                    // Validate fields before attempting signup
                    emailError = email.isEmpty()
                    firstNameError = firstName.isEmpty()
                    lastNameError = lastName.isEmpty()
                    userTypeError = (userType == null)
                    passwordError = password.isEmpty()
                    confirmPasswordError = confirmPassword.isEmpty()
                    passGreaterThan6Error = (password.length < 6)
                    passIncludesNumError = !(password.any { it.isDigit() })
                    passIncludesLowerError = !(password.any { it.isLowerCase() })
                    passMatchError = (password != confirmPassword) || password.isEmpty()
                    subjectError = ((userType == UserType.Tutor) && (tutorSubjects.isEmpty()))

                    // Validate tutor subjects.
                    tutorSubjectErrors = validateTutorSubjects(tutorSubjects)

                    // Proceed with signup if validation passes.
                    if (!emailError && !firstNameError && !lastNameError && !userTypeError &&
                        !passwordError && !confirmPasswordError && !subjectError &&
                        !passGreaterThan6Error && !passIncludesNumError && !passIncludesLowerError &&
                        !passMatchError && !(tutorSubjectErrors.any { it.subjectError || it.gradeError || it.specError || it.priceError || it.duplicateError })
                    ) {
                        authViewModel.signupWithEmail(email, password, firstName, lastName,
                            userType!!.name, tutorSubjects,
                            onUserReady = { user ->
                                certificationViewModel.uploadFiles(user.uid)
                                navController.navigate("termsAndCond")
                            },
                            onError = { errorMessage = it },
                            onEmailError = { invalidEmailError = true }
                        )
                        //authViewModel.signout() // Sign out the user after signup
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06C59C)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CONTINUE", color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

// Enum to represent user types.
enum class UserType {
    Student,
    Tutor
}

