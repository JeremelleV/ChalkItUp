package com.example.chalkitup.ui.screens

import androidx.compose.ui.graphics.painter.Painter
import android.util.Log
import androidx.compose.ui.res.painterResource
import com.example.chalkitup.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.lang.Exception
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import java.time.LocalDate
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chalkitup.Connection
import com.example.chalkitup.ui.viewmodel.BookingManager
import com.example.chalkitup.ui.viewmodel.HomeViewModel
import com.example.chalkitup.ui.viewmodel.WeatherViewModel
import androidx.compose.foundation.BorderStroke
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chalkitup.domain.model.Appointment

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    askQuestion: Int
) {
    val userType by homeViewModel.userType.collectAsState()
    var showTutorial by remember { mutableStateOf(false) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    val userName by homeViewModel.userName.collectAsState()

    val weatherState by weatherViewModel.weather
    val temperature = weatherState?.current?.tempC?.toInt()?.toString() ?: "--"
    val condition = weatherState?.current?.condition?.text ?: "Unknown"

    // Chat bubble state
    var isChatOpen by remember { mutableStateOf(false) }
    if (askQuestion == 1) {
        isChatOpen = true
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF),
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface
        )
    )

    if (showTutorial) {
        userType?.let { TutorialDialog(onDismiss = { showTutorial = false }, userType = it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.BottomEnd // Align chat bubble at the bottom right
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showTutorial = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tutorial",
                        tint = Color.White
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.chalk_confused),
                    contentDescription = "Confused Chalk",
                    modifier = Modifier.size(80.dp).offset(x = (-28).dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GreetingSection(userName!!)
                Spacer(modifier = Modifier.weight(4f))
                WeatherWidget(
                    temperature = temperature,
                    condition = condition,
                    modifier = Modifier
                        .size(110.dp)
                        .align(Alignment.Top)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
            CalendarScreen()
            UpcomingAppointments { appointment ->
                selectedAppointment = appointment
            }
        }

        //Chat Icon Button (Always visible, opens chat on click)
        val chatIcon: Painter = painterResource(id = R.drawable.chaticon3)

        Image(
            painter = chatIcon,
            contentDescription = "Chat Icon",
            modifier = Modifier
                .size(61.dp).offset(x = (-28).dp).offset(y = (-30).dp)
                .clickable { isChatOpen = true }
        )

// **Chat Popup** (Only visible when chat is opened)
        if (isChatOpen) {
            ChatPopup(onClose = { isChatOpen = false })
        }
    }

    // **Appointment Popup**
    selectedAppointment?.let { appointment ->
        AppointmentPopup(
            navController = navController,
            appointment = appointment,
            onDismiss = { selectedAppointment = null }
        )
    }
}



// User Greeting Above Calendar Based On Time of Day.
@Composable
fun GreetingSection(userName: String, modifier: Modifier = Modifier) {
    val greetingText = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(modifier = modifier) {
        Text(
            text = "$greetingText,",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
            )
    }
}

//Weather Widget, API Used: weatherapi.com
@Composable
fun WeatherWidget(temperature: String, condition: String, modifier: Modifier = Modifier) {
    val weatherIcon = when (condition.lowercase(Locale.ROOT)) {
        "cloudy" -> R.drawable.ic_cloudy
        "rainy" -> R.drawable.ic_rainy
        "sunny" -> R.drawable.ic_sunny
        "snow" -> R.drawable.ic_snow
        else -> R.drawable.ic_cloudy
    }

    Box(
        modifier = modifier
            .width(90.dp)
            .height(140.dp)
            .clip(
                RoundedCornerShape(
                    topEnd = 40.dp,
                    bottomStart = 40.dp
                )
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E2678), Color(0xFF756EF3)) // Deep blue to violet
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = weatherIcon),
                contentDescription = "Weather Icon",
                modifier = Modifier
                    .size(56.dp)
                    .padding(top = 2.dp)
            )
            Text(
                text = condition,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                //modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "$temperature°C",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

// Calendar Screen Shows Appointments For Both User Types
@Composable
fun CalendarScreen(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val bookedDates by homeViewModel.bookedDates.collectAsState()

    val currentMonth = remember { mutableStateOf(YearMonth.now()) }

    // Current Visibility Set Up Is 3 Months Prior and 3 Months Forward
    val calendarState = rememberCalendarState(
        startMonth = YearMonth.now().minusMonths(3),
        endMonth = YearMonth.now().plusMonths(3),
        firstVisibleMonth = YearMonth.now(),
    )
    LaunchedEffect(currentMonth.value) {
        calendarState.animateScrollToMonth(currentMonth.value)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Calendars UI Full Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFCAEBFF), Color(0xFFEDFFEF))
                        //og: Color(0xFFE3F2FD), Color(0xFFF1F8E9))
                        // not bad: (Color(0xFFA9D5FC), Color(0xFFA7FFEB)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Spacer(modifier = Modifier.height(6.dp))

                // Month Navigation with Back and Forward Arrows
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back Button (Previous Month)
                    IconButton(onClick = {
                        currentMonth.value = currentMonth.value.minusMonths(1)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    // Month Title Inside of Calendar
                    Text(
                        text = calendarState.firstVisibleMonth.yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )

                    // Forward Button (Next Month)
                    IconButton(onClick = {
                        currentMonth.value = currentMonth.value.plusMonths(1)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_forward),
                            contentDescription = "Next Month",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day Labels (n:2 = SU, MO, TU, etc.), Considered Sun, Mon etc, However Looked Crowded.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    daysOfWeek().forEach { day ->
                        Text(
                            text = day.name.take(2).uppercase(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                    }
                }

                // Calendar Grid Showcasing Booked Days Along With Current Day Outlined
                HorizontalCalendar(
                    state = calendarState,
                    dayContent = { day ->
                        val formattedDate = day.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val isBooked = bookedDates.contains(formattedDate)
                        val isToday = day.date == LocalDate.now()
                        val isCurrentMonth = YearMonth.from(day.date) == calendarState.firstVisibleMonth.yearMonth

                        // UI , Rounded Corners, Filled isBooked Appointments, Outlined Day
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isBooked -> Color(0xFF06C59C)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isToday) 2.dp else 0.dp,
                                    color = if (isToday) Color(0xFF06C59C) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.date.dayOfMonth.toString(),
                                color = when {
                                    isBooked -> Color.White
                                    isCurrentMonth -> Color.Black
                                    else -> Color.LightGray
                                },
                            )
                        }
                    }
                )
            }
        }
    }
}


// Display Of Upcoming Appointments in Database, The Current Goal Is To Remove Days That Have Passed,
// Displaying Days in Order From Soonest to Latest.
@Composable
fun UpcomingAppointments(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onAppointmentClick: (Appointment) -> Unit
) {


    //Start of UI for Upcoming Appointments
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Upcoming Appointments",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        val context = LocalContext.current
        val connection = Connection.getInstance(context)
        val isConnected by connection.connectionStatus.collectAsState(initial = false)
        if(isConnected) {
            val appointments by homeViewModel.appointments.collectAsState()
            BookingManager.clearBookings()
            appointments.forEach { appointment ->
                // Format the date properly using "MMM d"
                val formattedDate = try {
                    val date = LocalDate.parse(
                        appointment.date,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                    )
                    date.format(DateTimeFormatter.ofPattern("MMM d", Locale.US)) // Outputs "Mar 6"
                } catch (e: Exception) {
                    "Invalid Date"
                }

                UpcomingAppointmentItem(
                    title = appointment.subject,
                    date = formattedDate,
                    tutor = appointment.tutorName,
                    student = appointment.studentName,
                    mode = appointment.mode,
                    time = appointment.time,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    onClick = { onAppointmentClick(appointment) }
                )
                BookingManager.addBooking(appointment)
                Log.d("offlineApp", "App logged: $appointment.appointmentID")
            }
        }
        else{
            val appointments = BookingManager.readBookings()
            appointments.forEach { appointment ->
                    // Format the date properly using "MMM d"
                val formattedDate = try {
                    val date = LocalDate.parse(
                        appointment.date,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                    )
                    date.format(DateTimeFormatter.ofPattern("MMM d", Locale.US)) // Outputs "Mar 6"
                } catch (e: Exception) {
                    "Invalid Date"
                }

                UpcomingAppointmentItem(
                    title = appointment.subject,
                    date = formattedDate,
                    tutor = appointment.tutorName,
                    student = appointment.studentName,
                    mode = appointment.mode,
                    time = appointment.time,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    onClick = { onAppointmentClick(appointment) }
                )

                Log.d("offlineApp", "OfflineApp logged: $appointment.appointmentID")


            }
        }
    }
}

// UI for Upcoming Appointments
@Composable
fun UpcomingAppointmentItem(
    homeViewModel: HomeViewModel = hiltViewModel(),
    title: String,
    date: String,
    tutor: String,
    student: String,
    mode: String,
    time: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val userType by homeViewModel.userType.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(90.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Extracting Day and Month for UI
                val dateParts = date.split(" ")
                val month = dateParts.getOrNull(0)?.uppercase(Locale.ROOT) ?: "--"
                val day = dateParts.getOrNull(1) ?: "--"

                // Vertical Line Beside Appointment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    // Line Beside Date is Currently Green
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(70.dp)
                            .background(Color(0xFF06C59C), RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF06C59C)
                        )
                        Text(
                            text = month,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF06C59C)
                        )
                    }
                }

                // Appointment Details Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF54A4FF)
                    )
                    Text ( // Tutor's Name Below Subject,  ( May Remove, Looking Crowded)
                        text = if (userType == "Tutor") student
                        else tutor,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = mode, // Online/In-Person
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = time, // Time of Appointment
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                // Row to hold the arrow and image
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Conditionally load the image based on the mode
                    val imageRes = when (mode.lowercase()) {
                        "in person" -> R.drawable.chalk_eraser1
                        "online" -> R.drawable.chalk3
                        else -> R.drawable.chalk_cool
                    }

                    // Image for mode (either "inperson" or "online")
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Mode Image",
                        modifier = Modifier.size(65.dp)
                    )

                    // Right Arrow Icon
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "More",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom shadow effect
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(10.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f))
                    )
                )
        )
    }
}

@Composable
fun AppointmentPopup(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    appointment: Appointment,
    onDismiss: () -> Unit
) {
    val userType by homeViewModel.userType.collectAsState()

    val context = LocalContext.current
    val connection = Connection.getInstance(context)
    val isConnected by connection.connectionStatus.collectAsState(initial = false)
    // Track error message for network issues
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (userType != "Tutor") {
            homeViewModel.loadProfilePicture(appointment.tutorID)
        } else {
            homeViewModel.loadProfilePicture(appointment.studentID)
        }
    }

    val profilePictureUrl by homeViewModel.profilePic.collectAsState()

    AlertDialog(
        onDismissRequest = {
            errorMessage = null
            onDismiss()
        },
        title = {
            Text(
                text = "Appointment Details",
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (userType == "Tutor") {
                    Text(text = "Student:              ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                } else {
                    Text(text = "Tutor:              ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Profile Picture
                    AsyncImage(
                        model = profilePictureUrl ?: R.drawable.chalkitup,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape)
                            .clickable {
                                if (userType == "Tutor") {
                                    navController.navigate("profile/${appointment.studentID}")
                                } else {
                                    navController.navigate("profile/${appointment.tutorID}")
                                }
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (userType == "Tutor") {
                        Text(
                            text = appointment.studentName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = appointment.tutorName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Text(text = "Date:           ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                    Text(text = appointment.date)
                }
                Row {
                    Text(text = "Time:          ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                    Text(text = appointment.time)
                }
                Row {
                    Text(text = "Subject:      ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                    Text(text = appointment.subject)
                }
                Row {
                    Text(text = "Price:          ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                    Text(text = appointment.subjectObject["price"] as? String ?: "N/A")
                }
                Row {
                    Text(text = "Mode:         ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                    Text(text = appointment.mode)
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (appointment.comments.isNotEmpty()) {
                    Text(
                        text = "Comments:              ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = appointment.comments)
                }
                errorMessage?.let {
                    Text(text = it, color = Color.Red)
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rebook & Close in the same row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (userType == "Student") {
                        OutlinedButton(
                            onClick = {
                                if (isConnected) {
                                    homeViewModel.cancelAppointment(appointment) {
                                        onDismiss()
                                        navController.navigate("booking")
                                    }
                                } else {
                                    errorMessage = "Error: Try rebooking when you are back online!"
                                }
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(2.dp, Color(0xFF54A4FF))
                        ) {
                            Text("Rebook", color = Color(0xFF54A4FF))
                        }
                    }

                    OutlinedButton(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(2.dp, Color.Gray)
                    ) {
                        Text("Close", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Image on the left side of the button
                    Image(
                        painter = painterResource(id = R.drawable.eraser_x),
                        contentDescription = "Cancel Icon",
                        modifier = Modifier.size(55.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            if (isConnected) {
                                homeViewModel.cancelAppointment(appointment) {
                                    onDismiss()
                                    homeViewModel.loadAppointments()
                                }
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("Cancel Booking", color = Color.Red)
                    }

                    Image(
                        painter = painterResource(id = R.drawable.eraser_x),
                        contentDescription = "Cancel Icon",
                        modifier = Modifier.size(55.dp)
                    )
                }
            }
        }
    )
}

    @Composable
fun TutorialDialog(onDismiss: () -> Unit, userType: String) {
    val otherUserType = if (userType == "Tutor") "Student" else "Tutor"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .requiredWidth(380.dp)
                .padding(16.dp)
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Scrollable content
                Box(
                    modifier = Modifier
                        .weight(1f) // Allows the content to take up available space
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {

                        Text(
                            text = "Tutorial",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Welcome to ChalkItUp!",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("How to navigate and use ChalkItUp")

                        Spacer(modifier = Modifier.height(16.dp))

                        Row (
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            //Photo
                            Image(
                                painter = painterResource(id = R.drawable.t1),
                                contentDescription = "Cancel Icon",
                                modifier = Modifier.size(60.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column (
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    "Click on this icon in the home screen any time to view this tutorial again",
                                    fontSize = 15.sp,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Column (
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Top,
                        ) {
                            Text("This is your Home Page",
                                fontSize = 15.sp)
                            Text("Find your upcoming appointment dates, times, and details",
                                fontSize = 15.sp,)
                        }
                        //Photo
                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            painter = painterResource(id = R.drawable.t2),
                            contentDescription = "Cancel Icon",
                            modifier = Modifier.size(400.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                            Column (
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text("Tap on an upcoming appointment to find more details and view your $otherUserType's profile",
                                    fontSize = 15.sp,)
                            }
                        //Photo
                        Image(
                            painter = painterResource(id = R.drawable.t3),
                            contentDescription = "Cancel Icon",
                            modifier = Modifier.size(300.dp)
                        )

                        //Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            val secondIcon: String = if (userType == "Tutor") {
                                "Availability"
                            } else {
                                "Booking"
                            }
                            Text("Use the icons on the bottom of the screen to navigate between your Home Page, $secondIcon, Messages, and your Profile",
                                fontSize = 15.sp,)

                            // Photo
                            //IFELSE
                            if (userType == "Tutor") {
                                Image(
                                    painter = painterResource(id = R.drawable.t4),
                                    contentDescription = "Cancel Icon",
                                    modifier = Modifier.size(300.dp)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.t11),
                                    contentDescription = "Cancel Icon",
                                    modifier = Modifier.size(300.dp)
                                )
                            }
                        }

                        //Spacer(modifier = Modifier.height(16.dp))

                        if (userType == "Tutor") {

                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text("This is your availability page",
                                        fontSize = 15.sp,)
                                    Text("You can enter your availability for the current month and the next month and edit it whenever you please",
                                        fontSize = 15.sp,)
                                    Text("You'll be notified in the last week of the current month to remember to enter availability for the upcoming month",
                                        fontSize = 15.sp,)
                                }
                            Spacer(modifier = Modifier.height(16.dp))
                            //Photo
                            Image(
                                painter = painterResource(id = R.drawable.t5),
                                contentDescription = "Cancel Icon",
                                modifier = Modifier.size(400.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                                //Photo
                                Image(
                                    painter = painterResource(id = R.drawable.t6),
                                    contentDescription = "Cancel Icon",
                                    modifier = Modifier.size(400.dp)
                                )
                            Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text("Enter any availability you have for online appointments, in person appointments, or both!",
                                        fontSize = 15.sp,)
                                }
                            Spacer(modifier = Modifier.height(16.dp))
                            //Photo
                            Image(
                                painter = painterResource(id = R.drawable.t6),
                                contentDescription = "Cancel Icon",
                                modifier = Modifier.size(400.dp)
                            )
                        } else {
                                Column (
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text("This is your Booking page",
                                        fontSize = 15.sp,)
                                    Text("Book a new appointment with a tutor here!",
                                        fontSize = 15.sp,)
                                    Text("Pick a subject, price range, date, and time, and be automatically matched with a qualified tutor!",
                                        fontSize = 15.sp,)
                                }
                            Spacer(modifier = Modifier.height(16.dp))
                            //Photo
                            Image(
                                painter = painterResource(id = R.drawable.t12),
                                contentDescription = "Cancel Icon",
                                modifier = Modifier.size(400.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                            Column (
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text("This is your Messages page",
                                    fontSize = 15.sp,)
                                Text("Find all your new and old messages here!",
                                    fontSize = 15.sp,)
                            }
                        Spacer(modifier = Modifier.height(16.dp))
                        //Photo
                        Image(
                            painter = painterResource(id = R.drawable.t7),
                            contentDescription = "Cancel Icon",
                            modifier = Modifier.size(400.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                            Column (
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text("Search for a $otherUserType to start a new chat with them!",
                                    fontSize = 15.sp,)
                            }
                        Spacer(modifier = Modifier.height(16.dp))
                        //Photo
                        Image(
                            painter = painterResource(id = R.drawable.t8),
                            contentDescription = "Cancel Icon",
                            modifier = Modifier.size(400.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                            //Photo

                            Column (
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text("This is your Profile Page. This is how you'll appear to other users",
                                    fontSize = 15.sp,)
                                Text("Edit your profile here!",
                                    fontSize = 15.sp,)
                            }
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.t9),
                            contentDescription = "Cancel Icon",
                            modifier = Modifier.size(400.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                            //Photo

                            Column (
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text("Edit your interests, subjects, bio, and more!",
                                    fontSize = 15.sp,)
                                Text("Customize it to your liking!",
                                    fontSize = 15.sp,)
                            }
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.t10),
                            contentDescription = "Cancel Icon",
                            modifier = Modifier.size(400.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                            //Photo

                            Column (
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text("Logout or Delete your account through settings",
                                    fontSize = 15.sp,)
                            }


                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.t13),
                            contentDescription = "Cancel Icon",
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }
                // Fixed Close Button at the bottom
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(16.dp)
                        .width(160.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =  Color(0xFF06C59C)
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Close",
                        color = Color.White)
                }
            }
        }
    }
}