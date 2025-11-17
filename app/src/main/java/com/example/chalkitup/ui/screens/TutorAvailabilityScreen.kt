package com.example.chalkitup.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chalkitup.R
import com.example.chalkitup.ui.viewmodel.TutorAvailabilityViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Displays the tutor's availability selection screen.
 *
 * This function presents a calendar interface where tutors can select available
 * time slots for specific days. Tutors can click on a day to
 * display available time slots from <9 AM to 9 PM> in 30-minute increments.
 * Selected time slots are highlighted and stored, while deselected slots are removed.
 * Tutors can save or cancel their selections using the provided buttons.
 *
 * @param viewModel The ViewModel that manages tutor availability data and logic.
 */

@Composable
fun EnterTutorAvailability(
    viewModel: TutorAvailabilityViewModel
) {
    //------------------------------VARIABLES----------------------------------------------

    // Scroll state for the entire screen
    val scrollState = rememberScrollState()

    // Collecting states from ViewModel
    val tutorAvailability by viewModel.tutorAvailabilityList.collectAsState() // List of tutor's available time slots
    val selectedDay by viewModel.selectedDay.collectAsState() // Currently selected day
    val selectedTimeSlots by viewModel.selectedTimeSlots.collectAsState() // Selected time slots for the chosen day
    val isEditing by viewModel.isEditing.collectAsState() // Boolean flag indicating edit mode

    // Get the current month and store necessary date formatters
    val currentMonth = remember { YearMonth.now() }// Stores the current month
    val daysOfWeek = remember { daysOfWeek() } // Gets a list of days in a week (Mon-Sun)
    val dateFormatter =
        remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") } // Formatter for database storage

    // Calendar state for handling month navigation
    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(0),
        endMonth = currentMonth.plusMonths(1),
        firstVisibleMonth = currentMonth,
    )

    var isCurrentMonth by remember { mutableStateOf(true) }

    LaunchedEffect (isCurrentMonth) {
        println("isCurrentMonth: $isCurrentMonth")
        if (isCurrentMonth) {
            viewModel.fetchAvailabilityFromFirestore()
        } else {
            viewModel.fetchAvailabilityFromFirestore(currentMonth.plusMonths(1))
        }
    }

    LaunchedEffect(calendarState.firstVisibleMonth) {
        isCurrentMonth = calendarState.firstVisibleMonth.yearMonth == currentMonth
        viewModel.clearSelectedDay()
    }

    //------------------------------VARIABLES-END---------------------------------------------

    LaunchedEffect(Unit) {
        viewModel.fetchAvailabilityFromFirestore()
    }

    // Gradient Background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    "Your Availability",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFE3F2FD), Color(0xFFF1F8E9))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Display the current month and year
                        Text(
                            text = calendarState.firstVisibleMonth.yearMonth.format(
                                DateTimeFormatter.ofPattern(
                                    "MMMM yyyy"
                                )
                            ),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display the days of the week headers (Mon-Sun)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            daysOfWeek.forEach { day ->
                                Text(
                                    text = day.name.take(2),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        HorizontalCalendar(
                            state = calendarState,
                            dayContent = { day ->
                                val formattedDate = day.date.format(dateFormatter)
                                val hasAvailability =
                                    tutorAvailability.any { it.day == formattedDate } // Check if day has availability
                                val isSelected =
                                    selectedDay == formattedDate // Check if day is selected
                                val isToday = day.date == LocalDate.now()
                                val isPastDay = day.date < LocalDate.now()

                                val dayWithinCurrentMonth = YearMonth.from(day.date) == calendarState.firstVisibleMonth.yearMonth

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isSelected -> Color.DarkGray // Gray for selected day
                                                hasAvailability && isPastDay -> Color(0xFF398272) // Dark green for past days with availability
                                                hasAvailability -> Color(0xFF06C59C) // Green if availability exists
                                                isPastDay -> Color(0x50000000) // Light gray for past days
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 2.dp else 0.dp,
                                            color = if (isToday) Color.DarkGray else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (dayWithinCurrentMonth) {
                                                viewModel.selectDay(formattedDate) // Select day on click
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.date.dayOfMonth.toString(),
                                        color = when {
                                            isSelected || hasAvailability -> Color.White
                                            !dayWithinCurrentMonth -> Color.LightGray
                                            else -> Color.Black
                                        },
                                    )
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Action buttons (Edit, Save, Cancel)
                    if (isEditing) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Display Save and Cancel buttons in edit mode
                                Button(
                                    onClick = { viewModel.saveAvailability() },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.width(140.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF06C59C
                                        )
                                    )
                                ) {
                                    Text(text = "Save", color = Color.White)
                                }
                                Box(modifier = Modifier.width(20.dp))
                                Button(
                                    onClick = { viewModel.cancelEdit() },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.width(140.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF06C59C
                                        )
                                    )
                                ) {
                                    Text(text = "Cancel", color = Color.White)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(modifier = Modifier.width(70.dp))
                                // Buttons for Online
                                IconButton(onClick = { viewModel.selectAllOnline() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_check_box_24),
                                        contentDescription = "Select all online",
                                        tint = Color.LightGray,
                                        modifier = Modifier
                                            .size(25.dp),
                                    )
                                }
                                IconButton(onClick = { viewModel.clearAllOnline() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_cancel_24),
                                        contentDescription = "Clear all online",
                                        tint = Color.LightGray,
                                        modifier = Modifier
                                            .size(25.dp),
                                    )
                                }

                                Box(modifier = Modifier.width(30.dp))

                                IconButton(onClick = { viewModel.selectAllInPerson() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_check_box_24),
                                        contentDescription = "Select all in Person",
                                        tint = Color.LightGray,
                                        modifier = Modifier
                                            .size(25.dp),
                                    )
                                }
                                IconButton(onClick = { viewModel.clearAllInPerson() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_cancel_24),
                                        contentDescription = "Clear all in Person",
                                        tint = Color.LightGray,
                                        modifier = Modifier
                                            .size(25.dp),
                                    )
                                }
                            }
                        }

                    } else if (((selectedDay?.toLocalDate() ?: LocalDate.now()) >= LocalDate.now()) && (selectedDay != null))
                    { // Cannot edit past days
                        Text(
                            text = "View and Edit your Availability",
                            fontSize = 14.sp,
                        )

                        Box(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit Profile",
                                tint = Color(0xFF54A4FF),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier
                    .height(600.dp)
                    .border(
                        width = 2.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(viewModel.timeIntervals) { time ->
                            // Find the corresponding TimeSlot object (if it exists)
                            val timeSlot = selectedTimeSlots.find { it.time == time }

                            // Check if the time slot is booked
                            val isBooked = timeSlot?.booked ?: false

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Time slot label
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(50.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(text = time)
                                }

                                // Online selection box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .background(
                                            if (isBooked) Color(0xfffad96e) // Pink if booked
                                            else if (timeSlot?.online == true && isEditing) Color(
                                                0xFF54A4FF
                                            ) // Blue if online is selected
                                            else if (timeSlot?.online == true && !isEditing) Color(
                                                0xFF06C59C
                                            )
                                            else Color(0xFFd2e5fa) // unselected
                                            , shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable(enabled = isEditing && !isBooked) {
                                            viewModel.toggleTimeSlotSelection(time, "online")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBooked) "Booked" else "Online", color = Color.White, fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // In-Person selection box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .background(
                                            if (isBooked) Color(0xfffad96e) // Pink if booked
                                            else if (timeSlot?.inPerson == true && isEditing) Color(
                                                0xFF54A4FF
                                            ) // Blue if inPerson is selected
                                            else if (timeSlot?.inPerson == true && !isEditing) Color(
                                                0xFF06C59C
                                            )
                                            else Color(0xFFd2e5fa) // unselected
                                            , shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable(enabled = isEditing && !isBooked) {
                                            viewModel.toggleTimeSlotSelection(time, "inPerson")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBooked) "Booked" else "In Person", color = Color.White, fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Separator Row
                            Row {
                                Box(
                                    modifier = Modifier
                                        .height(1.dp)
                                        .width(50.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .height(1.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

        }
    }
}

fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
}