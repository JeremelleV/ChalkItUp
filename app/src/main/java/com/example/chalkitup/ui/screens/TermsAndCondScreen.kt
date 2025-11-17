package com.example.chalkitup.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chalkitup.ui.viewmodel.AuthViewModel

@Composable
fun TermsAndCond(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    // Screen Gradient
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface // 95% White
        )
    )

    // Scroll states for the main form and the Terms & Conditions box.
    val scrollState = rememberScrollState() // Main form scroll state - entire screen
    val termsScrollState =
        rememberScrollState() // Terms & Conditions scroll state - inside Terms and Cond. box

    // State variables for Terms and Conditions agreement.
    var hasScrolledToBottom by remember { mutableStateOf(false) }
    var hasAgreedToTerms by remember { mutableStateOf(false) }

    // Effect to track scrolling of the Terms & Conditions box.
    LaunchedEffect(termsScrollState.value) {
        if (!hasScrolledToBottom && termsScrollState.value == termsScrollState.maxValue) {
            hasScrolledToBottom = true
        }
    }

    var termsError by remember { mutableStateOf(false) }        // Tracks Not Checked

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Terms and conditions section.
            Text("Terms and Conditions", style = MaterialTheme.typography.titleLarge)
            // Box to display the terms and conditions text with scroll.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(5.dp))
                    .padding(8.dp)
                    .verticalScroll(termsScrollState) // Separate scroll state
            ) {
                // Converted to an annotated string to individually format each line
                // Easier to edit individual lines + style them.
                Text(
                    buildAnnotatedString {
                        //MaterialTheme.typography.bodyMedium)
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Terms and Conditions\n\n")
                            append("Last Updated March 7, 2025\n\n")
                        }
                        append(
                            "Welcome to ChalkItUp! By signing up and using our platform," +
                                    " you agree to the following Terms and Conditions. Please read them carefully.\n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("1. Introduction\n")
                        }
                        append(
                            "1.1 ChalkItUp provides a platform that connects students " +
                                    "with tutors for educational sessions. By using our app, you " +
                                    "acknowledge that you have read, understood, and agreed to these terms.\n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("2. User Agreement\n")
                        }
                        append(
                            "2.1. By registering, you confirm that you are either (a) at least 18 years old, or (b) a minor with parental or legal guardian consent.\n" +
                                    "2.2. Parents/guardians must approve and monitor minors' use of the Platform.\n" +
                                    "2.3. Users must provide accurate, up-to-date information during registration.\n" +
                                    "2.4. We reserve the right to suspend or terminate accounts for violations of these terms.\n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("3. User Responsibilities\n")
                        }
                        append(
                            "3.1. Tutors must provide accurate qualifications and availability. Misrepresentation may result in account suspension\n" +
                                    "3.2. Students and parents must ensure that sessions are attended as scheduled.\n" +
                                    "3.3. Users must adhere to professional conduct and avoid inappropriate communication.\n" +
                                    "3.4. The Platform is not responsible for the academic performance of the student. \n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("4. Payments & Fees\n")
                        }
                        append(
                            "4.1. Tutors are paid based on completed session\n" +
                                    "4.2 Students are responsible to pay tutor within 24 hours of session completion through the method decided between the tutor and student\n\n"
                        )


                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("5. Privacy and Data Protection\n")
                        }
                        append(
                            "5.1. We comply with the Personal Information Protection and Electronic Documents Act (PIPEDA) and applicable provincial privacy laws.\n" +
                                    "5.2. By using the Platform, you consent to the collection, use, and storage of personal data as outlined in our Privacy Policy.\n" +
                                    "5.3.  Users may request access to or deletion of their personal information.\n" +
                                    "5.4. We implement security measures to protect user data but cannot guarantee complete security. \n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("6. Messaging & Communication\n")
                        }
                        append(
                            "6.1. Tutors and students may communicate only through Platform-approved channels.\n" +
                                    "6.2. Messaging content must remain professional and educational.\n" +
                                    "6.3. We reserve the right to monitor communications to ensure compliance with child protection laws.\n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("7. Intellectual Property\n")
                        }

                        append(
                            "7.1. All content provided on the Platform, including lesson materials and resources, is either owned by or licensed to the Platform.\n" +
                                    "7.2. Users may not reproduce, distribute, or share proprietary content without permission.\n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("8. Termination of Services\n")
                        }

                        append(
                            "8.1. We reserve the right to terminate or suspend access for users who violate these Terms.\n" +
                                    "8.2. Users may delete their accounts, but payment obligations must be settled before account closure.\n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("9. Dispute Resolution\n")
                        }
                        append(
                            "9.1. Any disputes should first be addressed through our Support Team.\n" +
                                    "9.2. If unresolved, disputes shall be governed by the laws of Alberta, Canada and subject to Canadian courts.\n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("10. Liability Disclaimer\n")
                        }
                        append(
                            "10.1. The Platform is a facilitator and is not responsible for any disputes, misconduct, or learning outcomes.\n" +
                                    "10.2. We disclaim liability for technical failures, loss of data, or unauthorized access.\n" +
                                    "10.3. Users assume full responsibility for their interactions, and we encourage parental supervision for minors.\n\n"
                        )

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("11. Amendments\n")
                        }
                        append(
                            "11.1. We may update these Terms from time to time, and users will be notified of significant changes.\n" +
                                    "11.2. Continued use of the Platform constitutes acceptance of revised Terms."
                        )
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Checkbox for agreeing to terms and conditions.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = hasAgreedToTerms,
                    onCheckedChange = { hasAgreedToTerms = it },
                    enabled = hasScrolledToBottom,
                    colors = CheckboxColors(
                        checkedCheckmarkColor = Color.White,
                        uncheckedCheckmarkColor = Color.DarkGray,
                        checkedBoxColor = Color(0xFF54A4FF),
                        uncheckedBoxColor = Color.White,
                        disabledCheckedBoxColor = Color.DarkGray,
                        disabledUncheckedBoxColor = Color.Gray,
                        disabledIndeterminateBoxColor = Color.DarkGray,
                        checkedBorderColor = Color(0xFF54A4FF),
                        uncheckedBorderColor = Color.DarkGray,
                        disabledBorderColor = Color.DarkGray,
                        disabledUncheckedBorderColor = Color.DarkGray,
                        disabledIndeterminateBorderColor = Color.DarkGray
                    )
                )
                Text("I have read and agree to the Terms and Conditions",
                    color = MaterialTheme.colorScheme.onSurface)
            }

            // Error message if terms are not agreed to.
            if (termsError) {
                Text(
                    "You must agree to the Terms and Conditions before signing up",
                    color = Color.Red,
                    fontSize = 15.sp
                )
            }
            // Reset the terms error if terms are agreed to.
            if (hasAgreedToTerms) {
                termsError = false
            }

            Button(
                onClick = {
                    Log.d("SignupDebug", "Has Agreed to Terms: $hasAgreedToTerms")

                    // Validate before continuing
                    termsError = !hasAgreedToTerms

                    // Proceed with signup if validation passes.
                    if (!termsError) {

                        authViewModel.agreeToTerms()

                        if (authViewModel.checkEmailVerified()) {
                            authViewModel.isAdminApproved(
                                onResult = {
                                    if (it == true) {
                                        navController.navigate("login")
                                    } else {
                                        navController.navigate("awaitingApproval")
                                    }
                                },
                                isAdmin = {
                                    if (it == true) {
                                        navController.navigate("adminHome")
                                    }
                                }
                            )
                            //navController.navigate("login")
                        } else {
                            navController.navigate("checkEmail/verify")
                        }

                        authViewModel.signout() // Sign out the user after signup
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06C59C)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("SIGN UP", color = Color.White, fontSize = 16.sp)
            }

        }
    }
}