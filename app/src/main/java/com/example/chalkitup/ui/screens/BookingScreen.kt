package com.example.chalkitup.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chalkitup.R
import com.example.chalkitup.ui.components.validateTutorSubjects
import com.example.chalkitup.ui.components.SubjectGradeItemNoPrice
import com.example.chalkitup.ui.components.TutorSubject
import com.example.chalkitup.ui.components.TutorSubjectError
import com.example.chalkitup.ui.viewmodel.BookingViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun BookingScreen(
    viewModel: BookingViewModel
) {
    //------------------------------VARIABLES----------------------------------------------

    var isActive by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        viewModel.getCurrentUserActiveStatus { activeStatus ->
            isActive = activeStatus
        }
    }


    // Scroll state for the entire screen
    val scrollState = rememberScrollState()

    // Lists for subject and grade level selections.
    val availableSubjects = listOf("Math", "Science", "English", "Social", "Biology", "Physics", "Chemistry")
    val availableGradeLevels = listOf("7", "8", "9", "10", "20", "30")
    val availableGradeLevelsBPC = listOf("20", "30")
    val grade10Specs = listOf("- 1", "- 2", "Honours")
    val grade1112Specs = listOf("- 1", "- 2", "AP", "IB")

    var userSubjects by remember {
        mutableStateOf(
            listOf(TutorSubject("", "", "", "inBooking")) // Default list with one item
        )
    } // To store selected subject

    // State to track errors in tutor subject selections.
    var userSubjectErrors by remember { mutableStateOf<List<TutorSubjectError>>(emptyList()) }

    var priceRange by remember { mutableStateOf(20f..60f) } // Default price range

    // State for week navigation and day selection
    val isCurrentMonth by viewModel.isCurrentMonth.collectAsState()

    var currentMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var selectedWeekStart by remember { mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))) }

    // Calculate the first and last day of the current month
    var firstDayOfMonth by remember { mutableStateOf(viewModel.getFirstDayOfMonth(LocalDate.now())) }
    var lastDayOfMonth by remember { mutableStateOf(viewModel.getLastDayOfMonth(LocalDate.now()))}

    LaunchedEffect(isCurrentMonth) {
        if (isCurrentMonth) {
            currentMonth = LocalDate.now().monthValue
            selectedWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            viewModel.resetDay()

            firstDayOfMonth = viewModel.getFirstDayOfMonth(LocalDate.now())
            lastDayOfMonth = viewModel.getLastDayOfMonth(LocalDate.now())
        } else {
            currentMonth = LocalDate.now().plusMonths(1).monthValue
            selectedWeekStart = LocalDate.now().plusMonths(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            viewModel.resetDay()

            firstDayOfMonth = viewModel.getFirstDayOfMonth(LocalDate.now().plusMonths(1))
            lastDayOfMonth = viewModel.getLastDayOfMonth(LocalDate.now().plusMonths(1))
        }
        println("Current Month: $currentMonth")
        println("selectedWeekStart: $selectedWeekStart")
        println("firstDayOfMonth: $firstDayOfMonth")
        println("lastDayOfMonth: $lastDayOfMonth")
    }

    val daysInWeek = (0..6).map { selectedWeekStart.plusDays(it.toLong()) }

    // ViewModel States
    val selectedDay by viewModel.selectedDay.collectAsState()
    val selectedStartTime by viewModel.selectedStartTime.collectAsState()
    val selectedEndTime by viewModel.selectedEndTime.collectAsState()
    val availability by viewModel.availability.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val error by viewModel.error.collectAsState()

    var comments by remember { mutableStateOf("") }
    var sessionType by remember { mutableStateOf("inPerson") } // Default to In-Person

    var continueSuccess by remember { mutableStateOf(false) }

    var showSuccessDialog by remember { mutableStateOf(false) }

    // Track the previous subject, grade, and specialization
    var previousSubject by remember { mutableStateOf(userSubjects[0].subject) }
    var previousGrade by remember { mutableStateOf(userSubjects[0].grade) }
    var previousSpec by remember { mutableStateOf(userSubjects[0].specialization) }

    // Use LaunchedEffect to reset showTimeSelection when subject, grade, or specialization changes
    LaunchedEffect(userSubjects[0].subject, userSubjects[0].grade, userSubjects[0].specialization) {
        if (userSubjects[0].subject != previousSubject ||
            userSubjects[0].grade != previousGrade ||
            userSubjects[0].specialization != previousSpec
        ) {
            continueSuccess = false // Reset when subject, grade, or specialization changes
            previousSubject = userSubjects[0].subject
            previousGrade = userSubjects[0].grade
            previousSpec = userSubjects[0].specialization
        }
    }

    // Function to reset all fields
    fun resetAllFields() {
        userSubjects = listOf(TutorSubject("", "", "", "inBooking"))
        userSubjectErrors = emptyList()
        priceRange = 20f..60f
        selectedWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        comments = ""
        sessionType = "In-Person"
        viewModel.resetState() // Reset ViewModel state
        continueSuccess = false
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
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (showSuccessDialog) {
                SuccessAlertDialog(onDismiss = { showSuccessDialog = false })
            }

            Spacer(modifier = Modifier.height(40.dp))

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Book an Appointment",
                modifier = Modifier.padding(16.dp),
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {

                Spacer(modifier = Modifier.height(15.dp))

                // Session Type Selection
                Text(
                    text = "Pick a Mode",
                    modifier = Modifier.padding(16.dp)
                )

                Divider(
                    color = Color.Gray, // Color of the divider
                    thickness = 2.dp, // Thickness of the divider
                    modifier = Modifier.padding(horizontal = 16.dp) // Add padding around the divider
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            sessionType = "inPerson"
                            continueSuccess = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sessionType == "inPerson") Color(0xFF06C59C)
                            else Color(0xFFd2e5fa)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(160.dp)
                    ) { Text("In Person", color = Color.White) }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            sessionType = "online"
                            continueSuccess = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sessionType == "online") Color(0xFF06C59C)
                            else Color(0xFFd2e5fa)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(160.dp)
                    ) { Text("Online", color = Color.White) }

                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                // horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Text(
                    text = "Select your Subject",
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Divider(
                    color = Color.Gray, // Color of the divider
                    thickness = 2.dp, // Thickness of the divider
                )

                // Display list of selected subjects and their grade levels.
                Box(modifier = Modifier.heightIn(20.dp, 500.dp)) {
                    LazyColumn {
                        itemsIndexed(userSubjects) { index, tutorSubject ->
                            // Display each tutor subject item and its details.
                            SubjectGradeItemNoPrice(
                                tutorSubject = tutorSubject,
                                availableSubjects = availableSubjects,
                                availableGradeLevels = availableGradeLevels,
                                availableGradeLevelsBPC = availableGradeLevelsBPC,
                                grade10Specs = grade10Specs,
                                grade1112Specs = grade1112Specs,
                                onSubjectChange = { newSubject ->
                                    userSubjects = userSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(subject = newSubject)
                                    }
                                },
                                onGradeChange = { newGrade ->
                                    userSubjects = userSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(grade = newGrade)
                                    }
                                },
                                onSpecChange = { newSpec ->
                                    userSubjects = userSubjects.toMutableList().apply {
                                        this[index] = this[index].copy(specialization = newSpec)
                                    }
                                },
                                subjectError = userSubjectErrors.getOrNull(index)?.subjectError
                                    ?: false,
                                gradeError = userSubjectErrors.getOrNull(index)?.gradeError
                                    ?: false,
                                specError = userSubjectErrors.getOrNull(index)?.specError
                                    ?: false,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                // horizontalAlignment = Alignment.CenterHorizontally

            ) {
                // Price Range Slider
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Hourly Rate          ",
                    )
                    Text(
                        text = "\$${priceRange.start.toInt()} - \$${priceRange.endInclusive.toInt()}",
                        fontSize = 20.sp
                    )
                }

                Divider(
                    color = Color.Gray, // Color of the divider
                    thickness = 2.dp, // Thickness of the divider
                )

                CustomRangeSlider(
                    priceRange = priceRange,
                    onValueChange = { newRange ->
                        priceRange = newRange
                        continueSuccess = false
                    } // Hide time selection on price range change
                )

                // Min/Max labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "\$20")
                    Text(text = "\$120")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
            ) {
                // Comments Box
                Text(
                    text = "Add notes for your Tutor",
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Divider(
                    color = Color.Gray, // Color of the divider
                    thickness = 2.dp, // Thickness of the divider
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(100.dp)
                        .background(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent
                        ),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Optional") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    // Validate the selected subject
                    val errors = validateTutorSubjects(userSubjects)
                    userSubjectErrors = errors

                    // Check if there are any errors
                    val hasErrors = errors.any { it.subjectError || it.gradeError || it.specError }

                    if (!hasErrors) {
                        // If no errors, set the subject in the ViewModel
                        val selectedSubject = userSubjects.firstOrNull()
                        if (selectedSubject != null) {
                            viewModel.setSubject(selectedSubject, priceRange, sessionType)
                        }
                        continueSuccess = true
                    }
                },
                modifier = Modifier
                    .width(150.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06C59C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = if (isActive) !continueSuccess else false
            ) {
                Text("Continue", fontSize = 16.sp, modifier = Modifier.padding(4.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (!continueSuccess) {
                viewModel.resetDay()
                viewModel.resetMonth()
            }

            if (continueSuccess) {

                val dayText = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Row (
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button (
                            onClick = {
                                if (!isCurrentMonth) {
                                    viewModel.toggleIsCurrentMonth() // Set to current month if not already
                                    viewModel.resetDay()
                                    val selectedSubject = userSubjects.firstOrNull()
                                    if (selectedSubject != null) {
                                        viewModel.setSubject(selectedSubject, priceRange, sessionType)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentMonth) Color(0xFF06C59C)
                                else Color(0xFFd2e5fa)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(160.dp)
                        ) { Text("${LocalDate.now().month}", color = Color.White) }

                        Button (
                            onClick = {
                                if (isCurrentMonth) {
                                    viewModel.toggleIsCurrentMonth() // Set to next month if not already
                                    viewModel.resetDay()
                                    val selectedSubject = userSubjects.firstOrNull()
                                    if (selectedSubject != null) {
                                        viewModel.setSubject(selectedSubject, priceRange, sessionType)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isCurrentMonth) Color(0xFF06C59C)
                                else Color(0xFFd2e5fa)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(160.dp)
                        ) { Text("${LocalDate.now().plusMonths(1).month}", color = Color.White) }

                    }

                    Text(
                        "What day works best for you?",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val previousWeekStart = selectedWeekStart.minusWeeks(1)
                            // Check if the previous week is still within the current month
                            val previousWeekEnd = previousWeekStart.plusDays(6)
                            if (previousWeekEnd.isAfter(firstDayOfMonth) ||
                                previousWeekStart.monthValue == currentMonth ||
                                previousWeekEnd == firstDayOfMonth
                            ) {
                                println("previousWeekStart: $previousWeekStart")
                                println("previousWeekEnd: $previousWeekEnd")
                                println("firstDayOfMonth: $firstDayOfMonth")
                                println("lastDayOfMonth: $lastDayOfMonth")
                                println("currentMonth: $currentMonth")
                                selectedWeekStart = previousWeekStart
                            }
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        val backArrow: Painter = painterResource(id = R.drawable.ic_arrow_back)
                        Icon(
                            painter = backArrow,
                            contentDescription = "Previous Week",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .shadow(
                                elevation = 3.dp, // Add elevation to the outer Box
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Week Navigation and Day Selection
                            Row(
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                dayText.forEachIndexed { index, day ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFFd2e5fa),
                                                shape = when (index) {
                                                    0 -> RoundedCornerShape(
                                                        topStart = 16.dp, // Rounded top-left for Sun
                                                        topEnd = 0.dp,
                                                        bottomStart = 0.dp,
                                                        bottomEnd = 0.dp
                                                    )

                                                    dayText.lastIndex -> RoundedCornerShape(
                                                        topStart = 0.dp,
                                                        topEnd = 16.dp, // Rounded top-right for Sat
                                                        bottomStart = 0.dp,
                                                        bottomEnd = 0.dp
                                                    )

                                                    else -> RoundedCornerShape(0.dp) // No rounding for middle items
                                                }
                                            )
                                            .width(48.dp)
                                            .height(55.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(day,
                                            color = Color.Black,)
                                    }
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                daysInWeek.forEachIndexed { index, day ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (day < LocalDate.now()) Color.LightGray
                                                else if (day.monthValue != currentMonth) Color.LightGray
                                                else if (day == selectedDay) Color(0xFF06C59C)
                                                else Color.White,
                                                shape = when (index) {
                                                    0 -> RoundedCornerShape(
                                                        topStart = 0.dp,
                                                        topEnd = 0.dp,
                                                        bottomStart = 16.dp, // Rounded bottom-left for first day
                                                        bottomEnd = 0.dp
                                                    )

                                                    daysInWeek.lastIndex -> RoundedCornerShape(
                                                        topStart = 0.dp,
                                                        topEnd = 0.dp,
                                                        bottomStart = 0.dp,
                                                        bottomEnd = 16.dp // Rounded bottom-right for last day
                                                    )

                                                    else -> RoundedCornerShape(0.dp) // No rounding for middle items
                                                }
                                            )
                                            .width(48.dp)
                                            .height(65.dp)
                                            .clickable {
                                                if (day >= LocalDate.now() && day.monthValue == currentMonth) {
                                                    viewModel.selectDay(day)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column (
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = day.dayOfMonth.toString(),
                                                color = if (day == selectedDay) Color.White else Color.Black,
                                            )
                                            Text(
                                                text = day.month.toString().take(3).uppercase(),
                                                color = if (day == selectedDay) Color.White else Color.Black,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val nextWeekStart = selectedWeekStart.plusWeeks(1)
                            // Check if the next week is still within the current month
                            if (nextWeekStart.monthValue == currentMonth ||
                                nextWeekStart.isBefore(lastDayOfMonth) ||
                                nextWeekStart == lastDayOfMonth
                            ) {
                                selectedWeekStart = nextWeekStart
                            }
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        val nextArrow: Painter = painterResource(id = R.drawable.ic_arrow_forward)
                        Icon(
                            painter = nextArrow,
                            contentDescription = "Next Week",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Display Available Start Times
                selectedDay?.let { day ->
                    val availableTimes = availability[day] ?: emptyList()
                    println("Available times for $day: $availableTimes")

                    if (availableTimes.isNotEmpty()) {
                        Box(modifier = Modifier.heightIn(20.dp, 275.dp)) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Select a Start time",
                                    modifier = Modifier.padding(16.dp),
                                )

                                Divider(
                                    color = Color.Gray, // Color of the divider
                                    thickness = 2.dp, // Thickness of the divider
                                    modifier = Modifier.padding(horizontal = 16.dp) // Add padding around the divider
                                )

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3), // 3 columns
                                    contentPadding = PaddingValues(16.dp), // Padding around the grid
                                    verticalArrangement = Arrangement.spacedBy(12.dp), // Vertical spacing between rows
                                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Horizontal spacing between items
                                ) {
                                    items(availableTimes) { time ->
                                        TimeChip(
                                            time = time, // Pass LocalTime directly
                                            isSelected = time == selectedStartTime,
                                            onClick = { viewModel.selectStartTime(time) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No availability for this day",
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                if (selectedDay == null) {
                    Text(
                        text = "Select a day",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Display Available End Times
                selectedStartTime?.let { startTime ->
                    val validEndTimes =
                        viewModel.getValidEndTimes(
                            startTime,
                            availability[selectedDay] ?: emptyList()
                        )
                    println("Valid end times for $startTime: $validEndTimes")

                    if (validEndTimes.isNotEmpty()) {
                        Box(modifier = Modifier.heightIn(20.dp, 275.dp)) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Select an End time",
                                    modifier = Modifier.padding(16.dp),
                                )
                                Divider(
                                    color = Color.Gray, // Color of the divider
                                    thickness = 2.dp, // Thickness of the divider
                                    modifier = Modifier.padding(horizontal = 16.dp) // Add padding around the divider
                                )
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3), // 3 columns
                                    contentPadding = PaddingValues(16.dp), // Padding around the grid
                                    verticalArrangement = Arrangement.spacedBy(12.dp), // Vertical spacing between rows
                                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Horizontal spacing between items
                                ) {
                                    items(validEndTimes) { time ->
                                        TimeChip(
                                            time = time, // Pass LocalTime directly
                                            isSelected = time == selectedEndTime,
                                            onClick = { viewModel.selectEndTime(time) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No valid end times available",
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {

                        if (selectedDay != null && selectedStartTime != null && selectedEndTime != null) { // and subject != null
                            viewModel.matchTutorForTimeRange(
                                selectedDay!!,
                                selectedStartTime!!, selectedEndTime!!
                            ) { matchedTutorId ->
                                if (matchedTutorId != null) {
                                    // Proceed with booking
                                    val selectedSubject = userSubjects.firstOrNull()
                                    viewModel.submitBooking(
                                        matchedTutorId,
                                        comments,
                                        sessionType,
                                        selectedSubject!!,
                                        onSuccess = {
                                            showSuccessDialog = true
                                            resetAllFields()
                                        } // Reset all fields after successful submission
                                    ) // SUCCESS DIALOG
                                } // handle no match error DIALOG
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(40.dp)
                        .width(200.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF06C59C),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = selectedDay != null && selectedStartTime != null && selectedEndTime != null
                ) {
                    Text("Submit Booking", fontSize = 16.sp, modifier = Modifier.padding(4.dp))
                }

                Spacer(modifier = Modifier.height(150.dp))

                // Loading and Error Handling
                if (isLoading) {
                    CircularProgressIndicator()
                }
                error?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessAlertDialog(
    onDismiss: () -> Unit // Callback to dismiss the dialog
) {
    Dialog(
        onDismissRequest = onDismiss // Dismiss the dialog when clicked outside
    ) {
        // Dialog content
        Box(
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(16.dp))
        ) {
            Box {
                Column {
                    Row (
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(modifier = Modifier.weight(1f))
                        Image(
                            painter = painterResource(id = R.drawable.happy_chalk2),
                            contentDescription = "Success Icon",
                            modifier = Modifier.size(130.dp)
                        )
                    }
                    Box(modifier = Modifier.size(120.dp))

                    Image(
                        painter = painterResource(id = R.drawable.happy_eraser1),
                        contentDescription = "Success Icon",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Title
                    Text(
                        text = "Success!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06C59C),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Message
                Text(
                    text = "Your appointment has been booked!",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Check your Email or your Home page for reminders about Appointment details",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Dismiss button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF06C59C) // Green color for the button
                        ),
                        modifier = Modifier
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeChip(time: LocalTime, isSelected: Boolean, onClick: () -> Unit) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val formattedTime = time.format(timeFormatter) // Format the time
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(
                elevation = 2.dp, // Add elevation
                shape = RoundedCornerShape(8.dp),
                clip = false
            )
            .background(
                if (isSelected) Color(0xFF06C59C) else Color(0xFFd2e5fa),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = formattedTime,
            color = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.padding(10.dp))
    }
}

@Composable
fun CustomRangeSlider(
    priceRange: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // RangeSlider
        RangeSlider(
            value = priceRange,
            onValueChange = { newRange ->
                val roundedStart = (newRange.start / 5).roundToInt() * 5f
                val roundedEnd = (newRange.endInclusive / 5).roundToInt() * 5f
                onValueChange(roundedStart..roundedEnd)
            },
            valueRange = 20f..120f,
            steps = (120 / 5) - 1,
            onValueChangeFinished = {
                // Handle price range selection if needed
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp), // Add padding to make space for the custom icons/text
            colors = SliderDefaults.colors(
                activeTrackColor = Color(0xFF06C59C), // Color of the active track
                inactiveTrackColor = Color(0xFFd2e5fa), // Color of the inactive track
                thumbColor = Color(0xFF06C59C), // Color of the thumbs
                activeTickColor = Color(0xFF06C59C), // Color of active ticks (if any)
                inactiveTickColor = Color(0xFFd2e5fa) // Color of inactive ticks (if any)
            ),
        )
    }
}
