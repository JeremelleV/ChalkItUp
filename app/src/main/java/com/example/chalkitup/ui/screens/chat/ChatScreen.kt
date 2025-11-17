package com.example.chalkitup.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chalkitup.ui.viewmodel.chat.ChatViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Message
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.ui.components.ProfilePictureIcon
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    conversationId: String?,
    selectedUserId: String,
    navController: NavController,
    chatViewModel: ChatViewModel
) {
    // Use the navigation parameter for conversation ID
    val convoId = conversationId?.takeUnless { it == "null" }

    val currentUser by chatViewModel.currentUser.collectAsState()
    val selectedUser by chatViewModel.selectedUser.collectAsState()
    val messages by chatViewModel.messages.collectAsState()

    var text by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    if (currentUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(convoId) {
        chatViewModel.loadSelectedUserProfile(selectedUserId)
        chatViewModel.setupMessageListener(convoId)
    }

    LaunchedEffect(messages) {
        if (messages is Response.Success) {
            val messageList = (messages as Response.Success).data
            if (messageList.isNotEmpty()) {
                scrollState.animateScrollToItem(messageList.size - 1)
            }
        }
    }

    val groupedMessages = remember(messages) {
        if (messages is Response.Success) {
            (messages as Response.Success<List<Message>>).data.groupBy { message ->
                val date = Calendar.getInstance().apply { timeInMillis = message.timestamp }
                "${date.get(Calendar.YEAR)}-" +
                "${date.get(Calendar.MONTH)}-" +
                "${date.get(Calendar.DAY_OF_MONTH)}"
            }.toSortedMap()
        } else emptyMap()
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF),
            androidx.compose.material3.MaterialTheme.colorScheme.surface, androidx.compose.material3.MaterialTheme.colorScheme.surface,
            androidx.compose.material3.MaterialTheme.colorScheme.surface, androidx.compose.material3.MaterialTheme.colorScheme.surface //95% white
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Scaffold(
            backgroundColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (selectedUser != null) {
                    ChatAppBar(
                        user = selectedUser!!,
                        navController = navController,
                    )
                } else {
                    TopAppBar(
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp, // Remove shadow
                        contentColor = MaterialTheme.colors.onSurface,
                        title = { Text("") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    )
                }
            }
        ) { innerPadding  ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding )
                    .padding(16.dp)
            ) {
                // Display chat messages
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Display messages and date stamp
                    groupedMessages.forEach { (_, messagesForDay) ->
                        val firstTimestamp = messagesForDay.minByOrNull { it.timestamp }?.timestamp ?: 0L
                        item {
                            Text(
                                text = formatDateHeader(firstTimestamp),
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.Center)
                                    .padding(vertical = 8.dp)
                            )
                        }
                        items(messagesForDay) { message ->
                            ChatBubble(
                                message = message.text,
                                isCurrentUser = message.senderId == currentUser?.id
                            )
                        }
                    }
                }
                ChatInputBar(
                    text = text,
                    onTextChange = { text = it },
                    onSend = {
                        if (text.isNotBlank()) {
                            val messageToSend = text
                            text = ""

                            coroutineScope.launch {
                                chatViewModel.sendMessage(messageToSend, convoId)
                                scrollState.animateScrollToItem(scrollState.layoutInfo.totalItemsCount)
                            }

                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: String,
    isCurrentUser: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp),
            color = if (isCurrentUser) Color(0xFF2196F3) else Color(0xFF06C59C),
            shape = RoundedCornerShape(8.dp)
        ) {
            // Text inside the bubble
            Text(
                text = message,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                color = if (isCurrentUser) Color.White else Color.White,
                style = MaterialTheme.typography.body1

            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault())  // "9:11 AM" format
        .format(Date(timestamp))
}

private fun formatDateHeader(timestamp: Long): String {
    val currentTime = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { timeInMillis = timestamp }
    val timeString = formatTime(timestamp)

    return when {
        currentTime.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                currentTime.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) ->
            "Today, $timeString"
        currentTime.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                currentTime.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1 ->
            "Yesterday, $timeString"
        else -> {
            val dateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
            "${dateFormatter.format(Date(timestamp))}, $timeString"
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        placeholder = {
            Text(
                text = "Message...",
                style = MaterialTheme.typography.body1
            )
        },
        textStyle = MaterialTheme.typography.body1,
        shape = RoundedCornerShape(24.dp), // Rounded corners
        trailingIcon = {
            IconButton(onClick = onSend) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    )
}

@Composable
fun ChatAppBar(
    user: User,
    navController: NavController,
) {
    val userPictureUrl = user.userProfilePictureUrl

    Box {
        TopAppBar(
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            contentColor = MaterialTheme.colors.onSurface,
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(end = (0).dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 0.dp)
                ) {
                    ProfilePictureIcon(profilePictureUrl = userPictureUrl,
                        modifier = Modifier.clickable(onClick = {
                            navController.navigate("profile/${user.id}")
                            }
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        color = MaterialTheme.colors.onSurface
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 0.dp)
        )
        Divider(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.Gray.copy(alpha = 0.5f),
            thickness = 1.dp
        )

    }

}