package com.example.chalkitup.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.chalkitup.ui.viewmodel.AuthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import com.example.chalkitup.R
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp


val AtkinsonFontEmail = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_light, FontWeight.Light),
    Font(R.font.atkinson_bold, FontWeight.Bold),
    Font(R.font.atkinson_extrabold, FontWeight.ExtraBold)
)

// UI for the check email screen

@Composable
fun CheckEmailScreen(
    viewModel: AuthViewModel,
    checkType: String,  // "verify" or "reset"
    navController: NavController
) {
    val gradientBrushEmail = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF06C59C), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface, Color(0xFF54A4FF) // 95% White
        )
    )

    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrushEmail), //
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Check your email",
                fontFamily = AtkinsonFontEmail,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (checkType == "verify") { // The following is shown if it is email verification

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "  We've sent a confirmation email to your inbox.\n" +
                                "            Please click the link to verify your\n" +
                                "                          email address.",
                        fontFamily = AtkinsonFontEmail,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Didn't receive an email?",
                    fontFamily = AtkinsonFontEmail,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(onClick = {
                    successMessage = ""
                    errorMessage = ""
                    viewModel.resendVerificationEmail(
                        onSuccess = { successMessage = it },
                        onError = { errorMessage = it }
                    )
                }
                ) {
                    Text("Send another email",
                        color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = {
                    // signout the user bc they are not verified!
                    viewModel.signout()
                    navController.navigate("start")
                }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back to Home"
                    )
                    Text("Back to Home",
                        color = MaterialTheme.colorScheme.onSurface)
                }

                if (successMessage.isNotEmpty()) {
                    Text(successMessage)
                }

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red)
                }

            } else { // The following is shown if it is forgot password

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "  We've sent a password reset link to your inbox.\n" +
                                "      Please click the link in your email to reset\n" +
                                "                          your password.",
                        fontFamily = AtkinsonFontEmail,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = {
                    navController.navigate("start")
                }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back to Home"
                    )
                    Text("Back to Home",
                        color = MaterialTheme.colorScheme.onSurface)
                }

            }
        }
    }
}