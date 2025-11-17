package com.example.chalkitup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chalkitup.ui.viewmodel.AuthViewModel

@Composable
fun AwaitingApproval(
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val gradientBrushEmail = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF06C59C), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface, Color(0xFF54A4FF) // 95% White
        )
    )

    LaunchedEffect(Unit) {
        authViewModel.signout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrushEmail),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Awaiting Approval",
                fontFamily = AtkinsonFontEmail,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "You're account is currently under review." +
                            "When an Admin has approved your account you will be able to login.",
                    fontFamily = AtkinsonFontEmail,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))


        }
    }
}