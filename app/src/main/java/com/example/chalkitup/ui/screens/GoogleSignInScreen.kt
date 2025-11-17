package com.example.chalkitup.ui.screens

import com.example.chalkitup.auth.GoogleSignInActivity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chalkitup.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedButton

@Composable
fun GoogleSignInScreen() {
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            val signInIntent = Intent(context, GoogleSignInActivity::class.java)
            context.startActivity(signInIntent)
        },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google logo",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                fontSize = 16.sp,
                color = Color.Gray)
        }
    }
}


