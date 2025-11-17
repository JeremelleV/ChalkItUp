package com.example.chalkitup.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.Conversation
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.ui.components.ProfilePictureIcon
import com.example.chalkitup.ui.viewmodel.chat.MessageListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MessageListScreen(
    navController: NavController,
    messageListViewModel: MessageListViewModel = hiltViewModel()
) {
    val scrollState = rememberLazyListState()
    val currentUserId by messageListViewModel.currentUserId.collectAsState()
    val currentUserType by messageListViewModel.currentUserType.collectAsState()
    val dataReady by messageListViewModel.dataReady.collectAsState()
    val searchQuery by messageListViewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        messageListViewModel.loadUsers()
        messageListViewModel.loadConversations()
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF),
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface,
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { messageListViewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { navController.navigate("newMessage") },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Message")
                }
            }

            when (dataReady) {
                is Response.Error -> Text("Error loading conversations",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp))
                Response.Loading -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }

                is Response.Success -> {
                    val filteredConversations by messageListViewModel.getFilteredConversations().collectAsState()

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (filteredConversations.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(Alignment.Center)
                                ) {
                                    Text("No conversations")
                                }
                            }
                        } else {
                            items(filteredConversations) { conversation ->
                                val otherUser = messageListViewModel.getUserInfo(conversation)
                                val isUnread = messageListViewModel.isConversationUnread(
                                    conversation,
                                    currentUserType
                                )

                                ConversationItem(
                                    conversation = conversation,
                                    user = otherUser,
                                    isUnread = isUnread,
                                    onClick = {
                                        currentUserType?.let { type ->
                                            messageListViewModel.markConversationAsRead(conversation.id)
                                        }

                                        navController.navigate(
                                            "chat/${conversation.id}/${
                                                if (conversation.studentId == currentUserId)
                                                    conversation.tutorId
                                                else conversation.studentId
                                            }"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ConversationItem(
    conversation: Conversation,
    user: User?,
    isUnread: Boolean,
    onClick: () -> Unit
) {

    val displayName = if (user != null) { "${user.firstName} ${user.lastName}" }
    else { "Deleted User" }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(Color.Transparent),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 7.dp, end = 14.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePictureIcon(
                profilePictureUrl = user?.userProfilePictureUrl,
                size = 60.dp
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTimestamp(conversation.timestamp),
                        style = MaterialTheme.typography.bodySmall.copy(

                        )
                    )
                }

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                            color = if (isUnread) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUnread) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .padding(end = 7.dp)
                                .size(10.dp)
                                .background(
                                    color = Color(0xFF1A73E8),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val difference = currentTime - timestamp
    return when {
        difference < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        difference < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(difference)}min"
        difference < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(difference)}h"
        difference < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
        difference < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(difference)} days ago"
        else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(timestamp))
    }
}