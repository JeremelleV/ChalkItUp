import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chalkitup.ui.viewmodel.AuthViewModel
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.chalkitup.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background

// Custom Font
val AtkinsonFont = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_light, FontWeight.Light),
    Font(R.font.atkinson_bold, FontWeight.Bold),
    Font(R.font.atkinson_extrabold, FontWeight.ExtraBold)
)

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // Screen Gradient
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface // 95% White
        )
    )

    var errorMessage by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
            .padding(26.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LockReset,
                contentDescription = "Forgot Password Icon",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )

            //Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Forgot password?",
                fontSize = 36.sp,
                fontFamily = AtkinsonFont,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Subtitle
            Text(
                text = "No worries, we'll send you reset instructions.",
                fontSize = 16.sp,
                fontFamily = AtkinsonFont,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Email", fontSize = 14.sp, color = Color.Gray, fontFamily = AtkinsonFont)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter your email", fontFamily = AtkinsonFont) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Email Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Confirm Email", fontSize = 14.sp, color = Color.Gray, fontFamily = AtkinsonFont)
                OutlinedTextField(
                    value = confirmEmail,
                    onValueChange = { confirmEmail = it },
                    placeholder = { Text("Re-enter your email", fontFamily = AtkinsonFont) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontFamily = AtkinsonFont
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Reset Password Button
            Button(
                onClick = {
                    errorMessage = ""
                    when {
                        email.isEmpty() || confirmEmail.isEmpty() -> {
                            errorMessage = "Please fill out both email fields."
                        }
                        email != confirmEmail -> {
                            errorMessage = "Emails do not match."
                        }
                        else -> {
                            viewModel.resetPassword(
                                email,
                                onSuccess = { navController.navigate("checkEmail/reset") },
                                onError = { errorMessage = it }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Reset Password", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login
            Row(
                modifier = Modifier
                    .clickable { navController.navigate("login") }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Back to log in",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}