package com.example.chalkitup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chalkitup.R
import com.example.chalkitup.network.ChatBot
import com.example.chalkitup.network.Message
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun ChatPopup(onClose: () -> Unit) {
    var isChatOpen by remember { mutableStateOf(true) }

    if (isChatOpen) {
        Dialog(onDismissRequest = { onClose() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(90.dp)) // Ensure rounded corners

            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(min = 500.dp, max = 800.dp)
                        .fillMaxHeight(0.8f)
                        .align(Alignment.Center)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF54A4FF), Color(0xFF9893FD)) // Gradient inside chat popup

                            )
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent, // Ensure transparency so the gradient shows
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ChatScreen(onClose = {
                            isChatOpen = false
                            onClose()
                        })
                    }
                }
            }
        }
    }
}


@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    val backgroundColor = if (isUser) Brush.horizontalGradient(
        listOf(Color(0xFF007AFF), Color(0xFF00A7FF))
    ) else SolidColor(Color.White)

    val textColor = if (isUser) Color.White else Color.Black
    val shape = if (isUser) RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            val chatIcon: Painter = painterResource(id = R.drawable.chaticon)
            Image(
                painter = chatIcon,
                contentDescription = "Chat Icon",
                modifier = Modifier.size(75.dp)
            )
        }

        Surface(
            shape = shape,
            modifier = Modifier.clip(shape),
            color = Color.Transparent,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .background(backgroundColor)
                    .padding(12.dp)
            ) {
                Text(text = message, color = textColor)
            }
        }
    }
}

@Composable
fun ChatScreen(onClose: () -> Unit) {
    var userInput by remember { mutableStateOf("") }
    val chatHistory = remember { mutableStateListOf<Message>() }
    val coroutineScope = rememberCoroutineScope()

    val backgroundColor = Brush.horizontalGradient(
        listOf(Color(0xFF007AFF), Color(0xFF00A7FF))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom // Ensure buttons stay at the bottom
    ) {
        Text("Chat", style = MaterialTheme.typography.headlineMedium)

        // Make the chat messages scrollable
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(chatHistory) { message ->
                ChatBubble(message = message.content, isUser = message.role == "user")
            }
        }

        // Spacer to push the input and buttons down
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            placeholder = { Text("Ask me a question...") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        chatHistory.add(Message("user", userInput))
                        coroutineScope.launch {
                            val response = ChatBot.sendMessage(chatHistory)
                            chatHistory.add(Message("assistant", response))
                        }
                        userInput = ""
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .background(backgroundColor, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Send", color = Color.White)
                }
            }

            Button(
                onClick = onClose,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .background(backgroundColor, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
