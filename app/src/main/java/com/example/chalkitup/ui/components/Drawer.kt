package com.example.chalkitup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chalkitup.R
import com.example.chalkitup.ui.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

// Navigation drawer

@Composable
fun NavigationDrawer(
    navController: NavController,
    drawerState: DrawerState,
    themeViewModel: ThemeViewModel
) {
    val darkTheme by themeViewModel.isDarkTheme

    // Gradient brush for the screen's background.
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF06C59C), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface //95% white
        )
    )

    // Create a coroutine scope to manage state changes asynchronously
    val coroutineScope = rememberCoroutineScope()

    // ModalDrawerSheet is the main container for the drawer, with a background color set
    ModalDrawerSheet (
        drawerContainerColor = Color(0xFF06C59C), //MaterialTheme.colorScheme.primary, // Drawer background color
    ) {
        // Drawer Header Section
        //DrawerHeader()

        // Main content of the navigation drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush) // Set background color
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Row {
                    Box(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        coroutineScope.launch { drawerState.close() }
                    }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_forward),
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    label = {
                        Text(
                            "Study Timer",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = false,
                    onClick = {
                        // Close the drawer and navigate to the pomodoro screen when clicked
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("pomodoroTimer")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Each NavigationDrawerItem are the items that are clickable
                // and take you to another screen
                NavigationDrawerItem(
                    label = {
                        Text(
                            "Ask a Question",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = false,
                    onClick = {
                        // Close the drawer and navigate to the "home" screen when clicked
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("home/1")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    label = {
                        Text(
                            "View Profile",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate("profile/")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    label = {
                        Text(
                            if (darkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    selected = false,
                    onClick = {
                        themeViewModel.toggleTheme()
                    }
                )

            }
        }
    }
}
