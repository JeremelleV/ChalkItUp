package com.example.chalkitup.ui.screens

//import androidx.privacysandbox.tools.core.generator.build
//import com.google.api.client.json.GsonFactory
//first create a service account for google
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.content.ClipboardManager
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import com.example.chalkitup.ui.viewmodel.AuthViewModel
import com.example.chalkitup.ui.viewmodel.BookingManager
import com.example.chalkitup.ui.viewmodel.OfflineDataManager
import com.example.chalkitup.ui.viewmodel.SettingsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.AclRule
import com.google.api.services.calendar.model.AclRule.Scope
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import com.google.api.services.calendar.model.Calendar as GoogleCalendar
import java.util.Calendar as Cal
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext

//TODO THIS SCREEN IS NO LONGER IN USE
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    offlineViewModel: OfflineDataManager,
) {
    val context = LocalContext.current
    val account = GoogleSignIn.getLastSignedInAccount(context)

    var showDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val email = auth.currentUser?.email ?: ""
    //var userProfile by remember { mutableStateOf(UserProfile()) }
    //val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
    val calendarId = remember { CalendarDetailsSingleton.calendarId }
    LaunchedEffect(Unit) {
        CalendarDetailsSingleton.ensureCalendarExists(context)
    }

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        SubscriptionButton(context)

        Button(onClick = {
            authViewModel.signout()
            navController.navigate("start")}
        ) {
            Text("Logout")
        }

        // Show alert dialog when "Delete Account" button clicked
        OutlinedButton(onClick = { showDialog = true })
        {
            Text("Delete Account")
        }

        if (showDialog) {
            AlertDialog(
                title = { Text(text = "Delete this account?") },
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            settingsViewModel.deleteAccount(
                                onSuccess = {
                                    successMessage = "Account Deleted!"
                                    showDialog = false
                                    navController.navigate("start")
                                    offlineViewModel.removeUser(
                                        email
                                    )
                                },
                                onError = {
                                    errorMessage = "Failed to delete Account"
                                    showDialog = false
                                    navController.navigate("home")
                                }
                            )
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    // Show toast message when account deleted
    LaunchedEffect(successMessage) {
        if (successMessage.isNotEmpty()) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // Show toast message when account deletion failed
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

//----------------------- google calender API
fun authenticateServiceAccount(context: Context) {
    try {
        // Open the service account credentials file and automatically close it after use
        context.assets.open("chalkitup-bdceba61ebce.json").use { serviceAccountKeyFile ->
            // Load the service account credentials from the input stream
            val credentials = ServiceAccountCredentials.fromStream(serviceAccountKeyFile)

            // Initialize the HTTP transport and JSON factory
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

            // Create the HTTP request factory
            val requestFactory: HttpRequestFactory = httpTransport.createRequestFactory { credentials }

            // Define the API endpoint URL
            val url = GenericUrl("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            val request: HttpRequest = requestFactory.buildGetRequest(url)

            // Execute the request and handle the response
            val response = request.execute()

            // Check if the response is successful
            if (response.statusCode == 200) {
                println("Response: ${response.content}")
            } else {
                println("Error: ${response.statusCode}, ${response.statusMessage}")
            }
        }
    } catch (e: Exception) {
        // Log the error for debugging purposes
        e.printStackTrace()
    }
}

//add the service account to the calendar
//fun createCalendarAndAddServiceAccount(context: Context) {
//    try {
//        // Use 'use' block to automatically close the input stream when done
//        context.assets.open("chalkitup-bdceba61ebce.json").use { serviceAccountKeyFile ->
//            // Create GoogleCredentials from the input stream and scope it
//            val googleCredentials = GoogleCredentials.fromStream(serviceAccountKeyFile)
//                .createScoped(listOf(CalendarScopes.CALENDAR))
//
//            // Create HTTP transport and JSON factory
//            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
//            val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
//
//            // Create an HttpRequestInitializer that ensures credentials are refreshed before each request
//            val httpRequestInitializer = HttpRequestInitializer { request: HttpRequest ->
//                googleCredentials.refreshIfExpired()  // Ensure the credentials are fresh before making the request
//                // No need for googleCredentials.initialize(request) here
//            }
//
//            // Build the Calendar service using the credentials and HttpRequestInitializer
//            val calendarService =
//                Calendar.Builder(httpTransport, jsonFactory, httpRequestInitializer)
//                    .setApplicationName("ChalkItUp")
//                    .build()
//
//            // Create a new calendar
//            val calendar = GoogleCalendar().apply {
//                summary = "ChalkItUp Calendar"
//                timeZone = "America/Edmonton"
//            }
//
//            // Insert the calendar into Google Calendar
//            val createdCalendar = calendarService.calendars().insert(calendar).execute()
//            val calendarId = createdCalendar.id
//
//            // Store the calendar ID in the Singleton for later use
//            CalendarDetailsSingleton.calendarId = calendarId
//            println("Created and stored Calendar ID: $calendarId")
//        }
//
//    } catch (e: Exception) {
//        // Log the error for debugging purposes
//        e.printStackTrace()
//    }
//}

@OptIn(DelicateCoroutinesApi::class)
fun createCalendarAndAddServiceAccount(context: Context) {
    // Launch coroutine in a lifecycle-aware scope or use Dispatchers.IO for network operations
    GlobalScope.launch(Dispatchers.Main) {
        try {
            // Use 'use' block to automatically close the input stream when done
            context.assets.open("chalkitup-bdceba61ebce.json").use { serviceAccountKeyFile ->
                // Create GoogleCredentials from the input stream and scope it
                val googleCredentials = GoogleCredentials.fromStream(serviceAccountKeyFile)
                    .createScoped(listOf(CalendarScopes.CALENDAR))

                // Create HTTP transport and JSON factory
                val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
                val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

                // Create an HttpRequestInitializer that ensures credentials are refreshed before each request
                val httpRequestInitializer = HttpRequestInitializer { request: HttpRequest ->
                    googleCredentials.refreshIfExpired()  // Ensure the credentials are fresh before making the request
                }

                // Switch to a background thread for network operations
                withContext(Dispatchers.IO) {
                    // Build the Calendar service using the credentials and HttpRequestInitializer
                    val calendarService = Calendar.Builder(httpTransport, jsonFactory, httpRequestInitializer)
                        .setApplicationName("ChalkItUp")
                        .build()

                    // Create a new calendar
                    val calendar = GoogleCalendar().apply {
                        summary = "ChalkItUp Calendar"
                        timeZone = "America/Edmonton"
                    }

                    // Insert the calendar into Google Calendar
                    val createdCalendar = calendarService.calendars().insert(calendar).execute()
                    val calendarId = createdCalendar.id

                    // Switch back to the main thread for UI updates
                    withContext(Dispatchers.Main) {
                        // Store the calendar ID in the Singleton for later use
                        CalendarDetailsSingleton.calendarId = calendarId
                        println("Created and stored Calendar ID: $calendarId")
                    }
                }
            }
        } catch (e: Exception) {
            // Log the error for debugging purposes
            e.printStackTrace()
        }
    }
}










fun addServiceAccountToCalendar(calendarService: Calendar, calendarId: String, credentials: ServiceAccountCredentials) {
    try {
        // Define the service account email
        val serviceAccountEmail = credentials.clientEmail

        // Create the ACL rule for the service account
        val aclRule = AclRule().apply {
            scope = Scope().setType("user").setValue(serviceAccountEmail)
            role = "owner"  // You can set the role to "reader" or "writer" as per your needs
        }

        // Insert the ACL rule to give permissions to the service account
        calendarService.acl().insert(calendarId, aclRule).execute()

        println("Service account $serviceAccountEmail has been added to the calendar with owner permissions.")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


// Data class to store the calendar ID
object CalendarDetailsSingleton {
    var calendarId: String = ""

    fun ensureCalendarExists(context: Context) {
        if (calendarId.isEmpty()) {
            println("No calendar found. Creating a new one...")
            createCalendarAndAddServiceAccount(context)
        } else {
            println("Calendar already exists: $calendarId")
        }
    }
}

fun addAppointmentsToCalendar(context: Context, calendarId: String) {
    try {
        val calendarService = getCalendarService(context)

        // Retrieve appointments from your BookingManager (assuming it's correctly implemented)
        val appointments = BookingManager.readBookings()

        for (appointment in appointments) {
            val event = Event().apply {
                summary = "Appointment with ${appointment.studentName}"
                description = "Subject: ${appointment.subject}\nMode: ${appointment.mode}\nComments: ${appointment.comments}"
                location = "Online or location TBD"
            }

            // Set start and end times for the event
            val startDateTime = Cal.getInstance().apply {
                val startParts = appointment.date.split("-")
                val timeParts = appointment.time.split(":")
                set(startParts[0].toInt(), startParts[1].toInt() - 1, startParts[2].toInt(), timeParts[0].toInt(), timeParts[1].toInt())
            }

            val start = EventDateTime().apply {
                setDateTime(DateTime(startDateTime.time))
                setTimeZone("America/Edmonton")
            }

            val endDateTime = Cal.getInstance().apply {
                set(startDateTime[Cal.YEAR], startDateTime[Cal.MONTH], startDateTime[Cal.DAY_OF_MONTH], startDateTime[Cal.HOUR_OF_DAY], startDateTime[Cal.MINUTE] + 30)
            }

            val end = EventDateTime().apply {
                setDateTime(DateTime(endDateTime.time))
                setTimeZone("America/Edmonton")
            }

            event.start = start
            event.end = end

            // Insert the event into the calendar
            val createdEvent = calendarService.events().insert(calendarId, event).execute()

            println("Event created: ${createdEvent.htmlLink}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun getCalendarService(context: Context): Calendar {
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()

    // Open the service account credentials file
    val serviceAccountKeyFile = context.assets.open("chalkitup-bdceba61ebce.json")
    try {
        val credentials = GoogleCredentials.fromStream(serviceAccountKeyFile)
            .createScoped(CalendarScopes.CALENDAR)

        val requestInitializer = credentials as HttpRequestInitializer

        // Return the calendar service
        return Calendar.Builder(httpTransport, jsonFactory, requestInitializer)
            .setApplicationName("ChalkItUp")
            .build()
    } finally {
        // Make sure to close the stream
        serviceAccountKeyFile.close()
    }
}


fun getCalendarSubscriptionLink(context: Context, calendarId: String): String {
    try {
        val calendarService = getCalendarService(context) // Assume this is your existing function to get the Calendar service
        val calendar = calendarService.calendars().get(calendarId).execute()

        // Retrieve the public iCal URL from the calendar details
        val icalLink = calendar.get("iCalUID") as String
        println("iCalendar Link: $icalLink")

        return icalLink
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}


class CalendarHelper(private val mService: Calendar) {
    @Throws(IOException::class)
    fun makeCalendarPublic(calendarId: String?) {
        val rule = AclRule().apply {
            setRole("reader")
            setScope(Scope().setType("default"))
        }

        try {
            val createdRule = mService.acl().insert(calendarId, rule).execute()
            println("Calendar made public: ${createdRule.id}")
        } catch (e: IOException) {
            println("Error making calendar public: ${e.message}")
            throw e
        }
    }

    fun generateSubscriptionLink(calendarId: String): String {
        return "https://calendar.google.com/calendar/ical/$calendarId/public/basic.ics"
    }
}

//create a subscription button that copies subscription link and opens google calender subscriptions
@Composable
fun SubscriptionButton(context: Context) {
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = {
        coroutineScope.launch {
            // Ensure a calendar exists
            CalendarDetailsSingleton.ensureCalendarExists(context)

            val calendarId = CalendarDetailsSingleton.calendarId
            if (calendarId.isNotEmpty()) {
                println("Adding appointments to calendar: $calendarId")
                addAppointmentsToCalendar(context, calendarId)

                // Generate the subscription link
                val calendarHelper = CalendarHelper(getCalendarService(context))
                calendarHelper.makeCalendarPublic(calendarId)
                val subscriptionLink = calendarHelper.generateSubscriptionLink(calendarId)

                // Copy to clipboard
                copyToClipboard(subscriptionLink, context)
                Toast.makeText(context, "Subscription link copied!", Toast.LENGTH_SHORT).show()

                // Open Google Calendar subscription page
                openGoogleCalendar(context)
            } else {
                Toast.makeText(context, "Failed to create calendar", Toast.LENGTH_SHORT).show()
            }
        }
    }) {
        Text(text = "Subscribe to Calendar")
    }
}


fun copyToClipboard(subscriptionLink: String, context: Context) {
    val clipboard = getSystemService(context, ClipboardManager::class.java)
    val clip = ClipData.newPlainText("Calendar Subscription", subscriptionLink)
    clipboard?.setPrimaryClip(clip)
}

fun openGoogleCalendar(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com/calendar/r/subscriptions"))
    context.startActivity(intent)
}







//------------------------

