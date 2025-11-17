package com.example.chalkitup.ui.screens

import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.chalkitup.ui.components.BottomNavigationBar
import com.example.chalkitup.ui.components.MovingCharacterGif
import com.example.chalkitup.ui.components.MyTopBar
import com.example.chalkitup.ui.components.NavigationDrawer
import com.example.chalkitup.ui.nav.NavGraph
import com.example.chalkitup.ui.viewmodel.AuthViewModel
import com.example.chalkitup.ui.viewmodel.OfflineDataManager
import com.example.chalkitup.ui.viewmodel.ProfileViewModel
import com.example.chalkitup.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import com.example.chalkitup.ui.viewmodel.ThemeViewModel

// Main Screen acts as a base for other screens to be loaded into
// - ex. all screens loaded here will have a top app bar
// - call NavGraph to navigate through screens

/**
 * Composable function for the main screen of the app.
 *
 * This screen serves as the entry point for the app, managing navigation, UI layout,
 * and conditional visibility of top and bottom bars based on the current route.
 * It also observes the user's authentication state and navigates to the home screen if the user is logged in.
 */
@Composable
fun MainScreen() {
    //------------------------------VARIABLES----------------------------------------------

    // Initialize navigation controller for managing screen navigation.
    val navController = rememberNavController()

    // State for managing the drawer's open/closed state.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Coroutine scope for launching drawer animations.
    val coroutineScope = rememberCoroutineScope()

    // ViewModel for managing authentication-related data.
    val authViewModel: AuthViewModel = hiltViewModel()

    // ViewModel for managing profile photo
    val profileViewModel: ProfileViewModel = hiltViewModel() // Get ViewModel instance
    val profilePictureUrl by profileViewModel.profilePictureUrl.observeAsState()

    val settingsViewModel: SettingsViewModel = hiltViewModel()

    // State for tracking the current route to conditionally hide/show bars.
    var currentRoute by remember { mutableStateOf<String?>(null) }
    var targetedProfileView by remember { mutableStateOf(false) }

    // Observe route changes and update the current route when navigation destination changes.
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route

            if (currentRoute != null) {
                if (currentRoute!!.contains("/")) {
                    val currentRouteSpecifier = currentRoute!!.substringAfter("/")
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

            currentRoute = destination.route?.substringBefore("/")

        }
    }

    // List of routes where the bottom bar should be hidden.
    val hideBottomBarRoutes = listOf("start","login", "signup","forgotPassword","termsAndCond","adminHome")
    // Determine whether to show the bottom bar based on the current route.
    val showBottomBar = (currentRoute !in hideBottomBarRoutes) && !targetedProfileView

    // Observe the user's authentication state to determine whether they are logged in.
    val isUserLoggedIn by authViewModel.isUserLoggedIn.observeAsState(initial = false)
    val isGoogleUserLoggedIn by authViewModel.isGoogleUserLoggedIn.observeAsState(initial = false)

    // Send the user to the home screen if they are already logged in
    LaunchedEffect(isUserLoggedIn) {
        Log.e("MainScreen","checking approved status ${isUserLoggedIn}")
        if (isUserLoggedIn) {
            Log.e("MainScreen","checking approved status")
            authViewModel.isAdminApproved(
                onResult = {
                    if (it == true) {
                        println("approved")
                        navController.navigate("home/")
                    } else {
                        println("awaitingApproval")
                        navController.navigate("awaitingApproval")
                        authViewModel.signout()
                    }
                },
                isAdmin = {
                    if (it == true) {
                        println("admin here")
                        navController.navigate("adminHome")
                    }
                }
            )
        }
    }

    //------------------------------VARIABLES-END---------------------------------------------

    val themeViewModel: ThemeViewModel = hiltViewModel()

    // Modal navigation drawer that wraps the content of the screen.
    ModalNavigationDrawer(
        drawerState = drawerState, // Manage drawer state (open/closed)
        drawerContent = { NavigationDrawer(navController, drawerState, themeViewModel = themeViewModel) }, // Drawer content (menu)        drawerState = drawerState, // Manage drawer state (open/closed)
        gesturesEnabled = false // Disable swipe gestures to open the drawer (if enabled, drawer is accessible from any screen)
    ) {
        Scaffold(
            topBar = {
                // Conditionally show the top bar based on the current route.
                MyTopBar(
                    navController = navController,
                    onMenuClick = {
                        // Open the drawer when the menu button is clicked
                        coroutineScope.launch { drawerState.open() }
                        println("menu clicked ${drawerState.isClosed}")
                    },
                    profilePictureUrl = profilePictureUrl, // Observed LiveData
                    authViewModel = authViewModel, // Required for Logout
                    settingsViewModel = settingsViewModel, // Required for Delete Account
                    offlineViewModel = OfflineDataManager
                )
            },

            bottomBar = {
                // Conditionally show the bottom navigation bar based on the current route.
                if (showBottomBar) {
                    BottomNavigationBar(navController)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                // Display the screens based on the navigation graph.
                NavGraph(navController)

                // Moving GIF Character
                MovingCharacterGif()
            }
        }
    }
}