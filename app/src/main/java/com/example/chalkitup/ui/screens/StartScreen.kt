package com.example.chalkitup.ui.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chalkitup.R
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ButtonDefaults
import com.example.chalkitup.ui.theme.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.graphics.Brush

// Title, then description.
data class TextEntry(val title: String, val description: String)

@Composable
fun StartScreen(navController: NavController) {
    // Screen Gradient
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface // 95% White
        )
    )

    var currentImageIndex by remember { mutableStateOf(0) }

    val images = listOf(
        R.drawable.login1,
        R.drawable.login2,
        R.drawable.login3,
        R.drawable.login4
    )

    val textEntries = listOf(
        TextEntry("Choose Learning Style", "1-on-1 tutoring sessions (online or in-person)"),
        TextEntry("Flexible Scheduling", "Choose dates and times that fit your schedule"),
        TextEntry("Stay Connected", "Communicate effortlessly with your tutor"),
        TextEntry("Track Your Progress", "Upload grades, view lessons and achievements")
    )

    val AtkinsonFont = FontFamily(
        Font(R.font.atkinson_regular, FontWeight.Normal),
        Font(R.font.atkinson_light, FontWeight.Light),
        Font(R.font.atkinson_bold, FontWeight.Bold),
        Font(R.font.atkinson_extrabold, FontWeight.ExtraBold)
    )
/*
    // Muli/Mulish Font
    val mulishFont = FontFamily(
        Font(R.font.mulish_font, FontWeight.Normal),
        Font(R.font.mulish_bold, FontWeight.Bold),
        Font(R.font.mulish_extrabold, FontWeight.ExtraBold)
    )

 */

    // Auto-Rotate the Images & Text On 4-second Delay
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            currentImageIndex = (currentImageIndex + 1) % images.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .padding(35.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp)) // Adjusts the height of image, higher=lower

            // Image & Background To Image (circle)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.image_background),
                    contentDescription = "Background",
                    modifier = Modifier.size(320.dp)
                )
                Image(
                    painter = painterResource(id = images[currentImageIndex]),
                    contentDescription = "Rotating Image",
                    modifier = Modifier.size(300.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Dynamic Changing Text With Image
            Text(
                text = textEntries[currentImageIndex].title,
                fontSize = 29.4.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = AtkinsonFont
            )
            Text(
                text = textEntries[currentImageIndex].description,
                fontSize = 15.sp,
                color = Color.Gray,
                fontFamily = AtkinsonFont
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Indicator Bar
            IndicatorBar(currentIndex = currentImageIndex) { index ->
                currentImageIndex = index
            }
            // Moves login/signup Down The Higher dp
            Spacer(modifier = Modifier.height(60.dp))

            // Login Button & Text
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "LOGIN",
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Signup Text
            Row {
                Text(
                    text = "Don't have an account? ",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontFamily = AtkinsonFont
                )
                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = AtkinsonFont,
                    modifier = Modifier.clickable { navController.navigate("signup") }
                )
            }
        }
    }
}

// Progress Indicator Bar, Added Clickable Function
@Composable
fun IndicatorBar(currentIndex: Int, onIndicatorClick: (Int) -> Unit) {
    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0..3) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .clickable { onIndicatorClick(i) }
                    .background(if (i == currentIndex) Color.DarkGray else Color.LightGray)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
