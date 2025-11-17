package com.example.chalkitup.ui.nav

import ForgotPasswordScreen
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chalkitup.ui.screens.AwaitingApproval
import com.example.chalkitup.ui.screens.BookingScreen
import com.example.chalkitup.ui.screens.CheckEmailScreen
import com.example.chalkitup.ui.screens.EditProfileScreen
import com.example.chalkitup.ui.screens.HomeScreen
import com.example.chalkitup.ui.screens.LoginScreen
import com.example.chalkitup.ui.screens.chat.MessageListScreen
import com.example.chalkitup.ui.screens.chat.ChatScreen
import com.example.chalkitup.ui.screens.EnterTutorAvailability
import com.example.chalkitup.ui.screens.chat.NewMessageScreen
import com.example.chalkitup.ui.screens.NotificationScreen
import com.example.chalkitup.ui.screens.ProfileScreen
import com.example.chalkitup.ui.screens.SettingsScreen
import com.example.chalkitup.ui.screens.SignupScreen
import com.example.chalkitup.ui.screens.StartScreen
import com.example.chalkitup.ui.screens.TermsAndCond
import com.example.chalkitup.ui.screens.admin.AdminHome
import com.example.chalkitup.ui.viewmodel.AuthViewModel
import com.example.chalkitup.ui.viewmodel.BookingViewModel
import com.example.chalkitup.ui.viewmodel.CertificationViewModel
import com.example.chalkitup.ui.viewmodel.chat.ChatViewModel
import com.example.chalkitup.ui.viewmodel.EditProfileViewModel
import com.example.chalkitup.ui.viewmodel.chat.MessageListViewModel
import com.example.chalkitup.ui.viewmodel.NotificationViewModel
import com.example.chalkitup.ui.viewmodel.OfflineDataManager
import com.example.chalkitup.ui.viewmodel.ProfileViewModel
import com.example.chalkitup.ui.viewmodel.SettingsViewModel
import com.example.chalkitup.ui.viewmodel.TutorAvailabilityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.chalkitup.ui.viewmodel.admin.AdminHomeViewModel
import com.example.chalkitup.ui.screens.PomodoroScreen

// Navigation Center, NavHost with navController
// On app launch, opens startScreen

@Composable
fun NavGraph(navController: NavHostController) {


    NavHost(navController = navController, startDestination = "start") {

        // Start Screen
        composable("start") {
            StartScreen(navController = navController)
        }

        // Login Screen
        composable("login") {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                navController = navController,
                viewModel = authViewModel,
                offlineViewModel= OfflineDataManager
            )
        }

        // Signup Screen
        composable("signup") {
            val authViewModel: AuthViewModel = hiltViewModel()
            val certificationViewModel: CertificationViewModel = hiltViewModel()
            SignupScreen(
                navController = navController,
                certificationViewModel = certificationViewModel,
                authViewModel = authViewModel
            )
        }

        // TermsAndCond Screen
        composable("termsAndCond") {
            val authViewModel: AuthViewModel = hiltViewModel()
            TermsAndCond(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Home Screen
        composable("home/{askQuestion}") { backStackEntry ->
            val askQuestion = backStackEntry.arguments?.getString("askQuestion")?.takeIf { it.isNotEmpty() } ?: "0"
            HomeScreen(navController = navController, askQuestion = askQuestion.toInt())
        }

        // Notification Screen
        composable("notifications") {
            val user = FirebaseAuth.getInstance().currentUser
            val viewModel: NotificationViewModel = hiltViewModel()
            if (user != null) {
                NotificationScreen(
                    navController = navController,
                    viewModel = viewModel,
                    userId = user.uid
                )
            }
        }

        // Booking Screen
        composable("booking") {
            val viewModel: BookingViewModel = hiltViewModel()
            BookingScreen(
                viewModel = viewModel
            )
        }

        // Messages Screen
        composable("messages") {
            val messageListViewModel: MessageListViewModel = hiltViewModel()
            MessageListScreen(
                navController = navController,
                messageListViewModel
            )
        }

        // New Message Screen
        composable("newMessage") {
            val messageListViewModel: MessageListViewModel = hiltViewModel()
            NewMessageScreen(
                navController = navController,
                messageListViewModel)
        }

        // Chat Screen
        composable("chat/{conversationId}/{selectedUserId}") { backStackEntry ->
            val selectedUserId = backStackEntry.arguments?.getString("selectedUserId") ?: ""
            val conversationIdArg = backStackEntry.arguments?.getString("conversationId") ?: ""

            val conversationId = if (conversationIdArg == "null") null else conversationIdArg
            val chatViewModel: ChatViewModel = hiltViewModel()
            ChatScreen(
                conversationId = conversationId,
                selectedUserId = selectedUserId,
                navController = navController,
                chatViewModel
            )
        }

        // Profile Screen
        composable("profile/{targetedUser}") {backStackEntry ->
            val targetedUser = backStackEntry.arguments?.getString("targetedUser") ?: ""
            println("TARGETED USER $targetedUser")
            val certificationViewModel: CertificationViewModel = hiltViewModel()
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                navController = navController,
                certificationViewModel = certificationViewModel,
                profileViewModel = profileViewModel,
                targetedUser = targetedUser
            )
        }

        // Edit Profile Screen
        composable("editProfile") {
            val certificationViewModel: CertificationViewModel = hiltViewModel()
            val editProfileViewModel: EditProfileViewModel = hiltViewModel()
            EditProfileScreen(
                navController = navController,
                editProfileViewModel = editProfileViewModel,
                certificationViewModel = certificationViewModel)
        }

        // Settings Screen
        composable("settings") {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()
            SettingsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel,
                offlineViewModel = OfflineDataManager,
            )
        }

        // Check Email Screen
        composable("checkEmail/{checkType}") { backStackEntry ->
            val checkType = backStackEntry.arguments?.getString("checkType") ?: "verify"
            val authViewModel: AuthViewModel = hiltViewModel()
            CheckEmailScreen(
                navController = navController,
                checkType = checkType,
                viewModel = authViewModel
            )
        }

        // Forgot Password Screen
        composable("forgotPassword") {
            val authViewModel: AuthViewModel = hiltViewModel()
            ForgotPasswordScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        // Tutor Availability Screen
        composable("tutorAvailability") {
            val viewmodel: TutorAvailabilityViewModel = hiltViewModel()
            EnterTutorAvailability(
                viewModel = viewmodel
            )
        }

        //Pomodoro Timer
        composable("pomodoroTimer") {
            PomodoroScreen()
        }

        // -- Screen
        composable("awaitingApproval") {
            AwaitingApproval()
        }

        // -- Screen
        composable("adminHome") {
            val viewmodel: AdminHomeViewModel = hiltViewModel()
            val certViewModel: CertificationViewModel = hiltViewModel()
            AdminHome(
                navController = navController,
                viewModel = viewmodel,
                certificationViewModel = certViewModel
            )
        }
    }
}