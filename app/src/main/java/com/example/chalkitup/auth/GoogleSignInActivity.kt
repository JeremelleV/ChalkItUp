package com.example.chalkitup.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chalkitup.ui.viewmodel.AuthViewModel
import androidx.navigation.compose.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.chalkitup.ui.screens.MainScreen
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleSignInActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    // Access the AuthViewModel
    private val authViewModel: AuthViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            account?.let {
                // Instead of directly handling Firebase authentication, call the ViewModel's loginWithGoogle function
                authViewModel.loginWithGoogle(it,
                    onSuccess = {
                        Log.d("GoogleSignIn", "Sign-in successful")
                        onGoogleSignInSuccess()
                    },
                    onError = { errorMessage ->
                        Log.e("GoogleSignIn", "Sign-in failed: $errorMessage")
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Sign-in failed", e)
        }
    }

    private var onGoogleSignInSuccess: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleSignInClient = getGoogleSignInClient(this)

        setContent {
            val navController = rememberNavController()

            // Define the composable functions and navigation
            NavHost(navController, startDestination = "login") {
                composable("login") {
                    val signInIntent = googleSignInClient.signInIntent
                    signInLauncher.launch(signInIntent)
                }
                composable("home") {
                    // Once logged in, navigate to the main screen, initially there were topapp/bottom bars issues. fixed
                    MainScreen()  // MainScreen will handle navigation
                }
            }

            // Define the onSignInSuccess lambda to navigate to home screen after successful login
            onGoogleSignInSuccess = {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }  // Prevent back navigation to login
                }
            }
        }
    }

    private fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("708905206257-ii8ejoledclpds756snrea0tm79dmu6n.apps.googleusercontent.com") // Web Client ID, initially caused alot of issues
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}
