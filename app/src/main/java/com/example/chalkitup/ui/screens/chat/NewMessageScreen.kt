package com.example.chalkitup.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chalkitup.domain.Response
import com.example.chalkitup.domain.model.User
import com.example.chalkitup.ui.components.ProfilePictureIcon
import com.example.chalkitup.ui.viewmodel.chat.MessageListViewModel
import kotlinx.coroutines.launch


@Composable
fun NewMessageScreen(
    navController: NavController,
    messageListViewModel: MessageListViewModel,
) {
    val scrollState = rememberLazyListState()
    val loadingState by messageListViewModel.users.collectAsState()
    val searchQuery by messageListViewModel.searchQuery.collectAsState()
    val error by messageListViewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        messageListViewModel.loadUsers()
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF), // 5% Blue
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { messageListViewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.weight(1f)
                )
            }
            when (loadingState) {
                is Response.Error -> Text(
                    "Error loading users",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                Response.Loading -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
                is Response.Success -> {
                    val filteredUsers by messageListViewModel.getFilteredUsers().collectAsState()

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (filteredUsers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(Alignment.Center)
                                ) {
                                    Text("No users")
                                }
                            }
                        } else {
                            items(filteredUsers) { user ->
                                UserItem(
                                    user = user,
                                    onClick = {
                                        coroutineScope.launch {
                                            val convoId = messageListViewModel.fetchConversationId(user.id)

                                            if (!convoId.isNullOrBlank()) {
                                                messageListViewModel.markConversationAsRead(convoId)
                                            }

                                            navController.navigate("chat/${convoId ?: "null"}/${user.id}")
                                        }
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


// UI Component for displaying each user
@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    val userProfilePictureUrl = user.userProfilePictureUrl

    Card(
        onClick = onClick,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(Color.Transparent),
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
            // Display profile picture
            ProfilePictureIcon(
                profilePictureUrl = userProfilePictureUrl,
                size = 60.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}