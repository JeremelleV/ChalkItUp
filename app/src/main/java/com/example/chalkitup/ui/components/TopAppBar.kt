package com.example.chalkitup.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.res.painterResource
import com.example.chalkitup.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.chalkitup.ui.viewmodel.AuthViewModel
import com.example.chalkitup.ui.viewmodel.OfflineDataManager
import com.example.chalkitup.ui.viewmodel.SettingsViewModel

// Top app bar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
    navController: NavController,
    onMenuClick: () -> Unit,
    profilePictureUrl: String?,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    offlineViewModel: OfflineDataManager,
) {
    var targetedProfileView by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    // Holds the value of the current screen the user is on
    var currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    if (currentRoute != null) {
        if (currentRoute.contains("/")) {
            val currentRouteSpecifier = currentRoute.substringAfter("/")
            if (currentRouteSpecifier != "{checkType}" && currentRouteSpecifier.isNotBlank()) {
                val specificKeyCheck = navController.currentBackStackEntry?.arguments?.getString("targetedUser")
                if (specificKeyCheck.isNullOrBlank()) {
                    targetedProfileView = false
                } else {
                    // The specifier is to view another users profile
                    targetedProfileView = true
                }
            } else {
                targetedProfileView = false
            }
        } else {
            targetedProfileView = false
        }
    }

    currentRoute = currentRoute?.substringBefore("/")

    // Use theme colors dynamically
    // Changes the background colour of the top bar based on what
    // screen the user is on
    val backgroundColor = when (currentRoute) {
        //"home" -> Color(0xFFFFFFFF)//MaterialTheme.colorScheme.primary
        "profile","tutorAvailability","booking","home","start","login","signup","forgotPassword","termsAndCond","adminHome",
            "messages", "newMessage", "chat" -> Color(0xFF54A4FF) // Fill top-screen white space
        "checkEmail","awaitingApproval", "editProfile","notifications" -> Color(0xFF06C59C) // Fill top-screen white space
        "pomodoroTimer" -> Color(0xFF121212)
        else -> Color.White
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
                                    settingsViewModel.getEmail() ?: ""
                                )
                            },
                            onError = {
                                errorMessage = "Failed to delete Account"
                                showDialog = false
                                navController.navigate("home/")
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
    val context = LocalContext.current
    LaunchedEffect(successMessage) {
        if (successMessage.isNotEmpty()) {
            Toast.makeText( context, successMessage, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText( context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // Change text dynamically
    // Changes the text at the top based on what screen the user is on
    CenterAlignedTopAppBar(
        modifier = Modifier.height(110.dp), // Increased height for icon, felt 80.dp looked the best
        title = {
            if (currentRoute == "home" || currentRoute == "editProfile" || currentRoute == "profile") {
                Image(
                    painter = painterResource(id = R.drawable.logo1),
                    contentDescription = "Chalk & Eraser",
                    modifier = Modifier.size(110.dp)
                )
            } else if (currentRoute == "booking") { //May switch images around
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chalk1),
                        contentDescription = "Chalk 1",
                        modifier = Modifier.size(80.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.chalk_eraser2),
                        contentDescription = "Chalk Eraser2",
                        modifier = Modifier.size(80.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.chalk2),
                        contentDescription = "Chalk 2",
                        modifier = Modifier.size(80.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.chalk_eraser1),
                        contentDescription = "Chalk Eraser1",
                        modifier = Modifier.size(80.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.chalk3),
                        contentDescription = "Chalk 3",
                        modifier = Modifier.size(80.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (currentRoute) {
                           // "profile" -> ""
                            "settings" -> ""
                            "messages" -> "Messages"
                            "notifications" -> "Notifications"
                            "newMessage" -> "New Chat"
                            "pomodoroTimer" -> "Pomodoro Timer"
                            "start", "login", "signup", "forgotPassword", "checkEmail", "tutorAvailability", "termsAndCond", "adminHome", "awaitingApproval",
                                 "chat"-> ""
                            else -> "ChalkItUp Tutors"
                        },
                        color = if (currentRoute == "pomodoroTimer") Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = backgroundColor
        ),

        // Change LEFT button based on current screen
        navigationIcon = {
            when (currentRoute) {
                // On the home page currently,
                // there is a menu button in the top left that opens
                // the navigation drawer
                "home" -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                "profile" -> {
                    if (!targetedProfileView) {
                        var expanded by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { expanded = true },
                                modifier = Modifier.size(65.dp)
                            ) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings",
                                    modifier = Modifier.size(35.dp),
                                    tint = Color(0xFF000080)
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = {
                                        expanded = false
                                        navController.navigate("profile/")
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        expanded = false
                                        authViewModel.signout()
                                        navController.navigate("start")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Account", color = Color.Red) },
                                    onClick = {
                                        expanded = false
                                        showDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                // On the login and signup page currently,          // Here are the alternative back buttons on login & signup screen
                // there is a back button in the top left
                "login","signup","awaitingApproval" -> {
                    IconButton(onClick = { navController.navigate("start") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(30.dp),
                            tint = Color.DarkGray)
                    }
                }
                // On unspecified pages there is no button in the top left
                else -> {
                    Unit
                }
            }
            // If viewing another user's profile, there is a back button
            if (targetedProfileView || currentRoute == "notifications") {
                IconButton(onClick = { navController.popBackStack() } ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(30.dp),
                        tint = Color.DarkGray)
                }
            }
        },
        // Change RIGHT button based on current screen
        actions = {
            when (currentRoute) {
                // On the home page currently,
                // there is a settings button in the top right

                 "home" -> {
                        var expanded by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { expanded = true },
                                modifier = Modifier.size(65.dp)
                            ) {
                                AsyncImage(
                                    model = profilePictureUrl ?: R.drawable.chalkitup,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(55.dp)
                                        .clip(CircleShape)
                                        .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = {
                                        expanded = false
                                        navController.navigate("profile/")
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        expanded = false
                                        authViewModel.signout()
                                        navController.navigate("start")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Account", color = Color.Red) },
                                    onClick = {
                                        expanded = false
                                        showDialog = true
                                    }
                                )
                            }
                        }
                    }

                "profile" -> {
                    if (!targetedProfileView) {
                        IconButton(onClick = { navController.navigate("editProfile") }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit Profile",
                                tint = Color(0xFF000080),
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
