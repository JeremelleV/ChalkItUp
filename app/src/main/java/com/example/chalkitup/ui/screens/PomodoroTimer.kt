package com.example.chalkitup.ui.screens

import android.os.CountDownTimer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.minutes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun PomodoroScreen() {
    var timeLeft by remember { mutableStateOf(25.minutes.inWholeSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var timer: CountDownTimer? by remember { mutableStateOf(null) }
    var taskName by remember { mutableStateOf("Study for Math Test") }
    var isTaskEditing by remember { mutableStateOf(false) } // Separate state for task editing
    var isEditing by remember { mutableStateOf(false) } // Timer editing state
    var selectedTime by remember { mutableStateOf(25f) } // New state for time selection in minutes

    fun resetTimer() {
        isRunning = false
        timer?.cancel()
        timeLeft = (selectedTime * 60).toLong()
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            timer = object : CountDownTimer(timeLeft * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = millisUntilFinished / 1000
                }
                override fun onFinish() {
                    isRunning = false
                    timeLeft = (selectedTime * 60).toLong()
                }
            }.start()
        } else {
            timer?.cancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Task Name Row (Editable)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF6A4CA2))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isTaskEditing) { // Only show task editing when isTaskEditing is true
                BasicTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = "Task: $taskName",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            IconButton(onClick = { isTaskEditing = !isTaskEditing }) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Task",
                    tint = Color.White
                )
            }
        }

        // Decreased spacer height to move the task up
        Spacer(modifier = Modifier.height(50.dp))

        // Timer with Halo & Outer Circle (increased size)
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(300.dp)) {
                val progress = 1f - (timeLeft / (selectedTime * 60f)) // Converts time left to progress (1.0 to 0.0)

                // Halo Effect (Dramatic Glowing Arc)
                drawArc(
                    brush = SolidColor(Color(0x33A16AE3)), // More transparent purple for a dramatic effect
                    startAngle = -90f,
                    sweepAngle = progress * 360f, // Moves dynamically with the timer
                    useCenter = false,
                    style = Stroke(width = 80f, cap = StrokeCap.Round) // Increased width for more drama
                )

                // Outer Circle
                drawCircle(
                    color = Color(0xFF6A4CA2),
                    radius = size.minDimension / 2.0f,
                    style = Stroke(width = 10f)
                )
            }

            // Time Display with Gradient Inner Circle
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF450793), // Lighter purple
                                Color(0xFF6A4CA2)  // Darker purple
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Make the time text clickable to enter time editing mode
                Text(
                    text = formatTime(timeLeft),
                    fontSize = 48.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable {
                            // Toggle between editing mode and close time picker when clicked
                            isEditing = !isEditing
                        }
                )
            }
        }

        // If in editing mode, show the scrollable time picker
        if (isEditing) {
            TimePicker(selectedTime = selectedTime) { newTime ->
                selectedTime = newTime
                resetTimer() // Reset timer when the time changes
                isEditing = false // Close time picker after selection
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons Row: Play/Pause + Restart
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Play/Pause Button
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A4CA2))
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Restart Button
            Button(
                onClick = { resetTimer() },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A4CA2))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Restart",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

// Time Formatter
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}

// Time Picker Composable
@Composable
fun TimePicker(selectedTime: Float, onTimeChange: (Float) -> Unit) {
    val minutesList = List(26) { it.toFloat() } // List of minutes from 0 to 25

    LazyColumn(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(minutesList) { minute ->
            Text(
                text = "$minute min",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier
                    .clickable {
                        onTimeChange(minute)
                    }
                    .padding(10.dp)
            )
        }
    }
}
