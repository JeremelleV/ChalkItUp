package com.example.chalkitup.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chalkitup.R
import com.example.chalkitup.lifecycle.AppLifecycleObserver
import com.example.chalkitup.ui.screens.CertificationGrid
import com.example.chalkitup.ui.viewmodel.CertificationViewModel
import com.example.chalkitup.ui.viewmodel.admin.AdminHomeViewModel
import com.example.chalkitup.ui.viewmodel.admin.Report
import com.example.chalkitup.ui.viewmodel.admin.User
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminHome(
    navController: NavController,
    viewModel: AdminHomeViewModel,
    certificationViewModel: CertificationViewModel,
) {

    // collect all flows
    val unapprovedTutors by viewModel.unapprovedTutors.collectAsState()
    val approvedTutors by viewModel.approvedTutors.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val usersWithReports by viewModel.usersWithReports.collectAsState()
    val profilePictures by viewModel.profilePictureUrls.collectAsState()

    // UI state
    val expandedTutorId = remember { mutableStateOf<String?>(null) }
    val expandUserReports = remember { mutableStateOf<String?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val showDenyDialog = remember { mutableStateOf(false) }
    val showDeactivateDialog = remember { mutableStateOf(false) }
    val tutorToApprove = remember { mutableStateOf<User?>(null) }
    val tutorToDeny = remember { mutableStateOf<User?>(null) }
    val tutorToDeactivate = remember { mutableStateOf<User?>(null) }
    var reason by remember { mutableStateOf("") }

    LaunchedEffect(approvedTutors, usersWithReports) {
        val allIds = (approvedTutors + usersWithReports)
            .map { it.id }
            .distinct()
        viewModel.loadProfilePictures(allIds)
    }

    // Gradient Background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF54A4FF), // 5% Blue
            MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    navController.navigate("start")
                    viewModel.signout()
                    //navController.navigate("start")
                }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_logout_24),
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "ChalkItUp Admin",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "New Tutors",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "${unapprovedTutors.size} Tutors need to be approved",
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier.heightIn(max = 700.dp)
                                .border(1.dp, Color(0xFF54A4FF), shape = RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                unapprovedTutors.forEach { tutor ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = Color.LightGray,
                                                disabledContentColor = Color.DarkGray
                                            ),
                                            onClick = {
                                                expandedTutorId.value =
                                                    if (expandedTutorId.value == tutor.id) {
                                                        null // Collapse the card if already expanded
                                                    } else {
                                                        tutor.id // Expand the clicked tutor's card
                                                    }
                                            }
                                        ) {
                                            Text(
                                                "${tutor.firstName} ${tutor.lastName}",
                                                modifier = Modifier.padding(
                                                    vertical = 22.dp,
                                                    horizontal = 20.dp
                                                ),
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                    if (expandedTutorId.value == tutor.id) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledContainerColor = Color.LightGray,
                                                    disabledContentColor = Color.DarkGray
                                                ),
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                ) {
                                                    Row {
                                                        Text(
                                                            "Email:    ",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp
                                                        )
                                                        Text(
                                                            tutor.email, color = Color(0xFF2196F3),
                                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                            fontSize = 18.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    Text(
                                                        "Subjects Offered",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    SubjectDisplay(tutor)

                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    Text(
                                                        "Certifications",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    CertificationDisplay(
                                                        tutor,
                                                        certificationViewModel
                                                    )

                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    Row(
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Button(
                                                            modifier = Modifier.weight(0.6f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFF06C59C),
                                                            ),
                                                            onClick = {
                                                                showDialog.value = true
                                                                tutorToApprove.value = tutor
                                                            }
                                                        ) {
                                                            Text("Approve", color = Color.White)
                                                        }

                                                        Spacer(modifier = Modifier.width(16.dp))

                                                        Button(
                                                            modifier = Modifier.weight(0.4f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color.Red,
                                                            ),
                                                            onClick = {
                                                                showDenyDialog.value = true
                                                                tutorToDeny.value = tutor
                                                            }
                                                        ) {
                                                            Text("Deny", color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        Text(
                            "Active Tutors",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "${approvedTutors.size} Tutors currently having sessions",
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier.heightIn(max = 700.dp)
                                .border(1.dp, Color(0xFF54A4FF), shape = RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                approvedTutors.forEach { tutor ->
                                    val tutorReports = reports.filter { it.userId == tutor.id }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = Color.LightGray,
                                                disabledContentColor = Color.DarkGray
                                            ),
                                            onClick = {
                                                expandedTutorId.value =
                                                    if (expandedTutorId.value == tutor.id) {
                                                        null // Collapse the card if already expanded
                                                    } else {
                                                        tutor.id // Expand the clicked tutor's card
                                                    }
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // profile picture
                                                Spacer(modifier = Modifier.width(16.dp))
                                                AsyncImage(
                                                    model = profilePictures[tutor.id]
                                                        ?: R.drawable.chalkitup,
                                                    contentDescription = "Profile Picture",
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .clip(CircleShape)
                                                        .border(2.dp, Color.Gray, CircleShape)
                                                        .clickable {
                                                            expandedTutorId.value =
                                                                if (expandedTutorId.value == tutor.id) {
                                                                    null // Collapse the card if already expanded
                                                                } else {
                                                                    tutor.id // Expand the clicked tutor's card
                                                                }
                                                        }
                                                )

                                                Text(
                                                    "${tutor.firstName} ${tutor.lastName}",
                                                    modifier = Modifier.padding(
                                                        vertical = 22.dp,
                                                        horizontal = 20.dp
                                                    ),
                                                    fontSize = 18.sp,
                                                )
                                                if (tutorReports.isNotEmpty()) {
                                                    Box(modifier = Modifier.weight(1f))
                                                    Text(
                                                        "${tutorReports.size}",
                                                        color = Color.Red,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp
                                                    )
                                                    IconButton(onClick = {}
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.baseline_report_gmailerrorred_24),
                                                            contentDescription = "Report icon",
                                                            tint = Color.Red,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (expandedTutorId.value == tutor.id && tutorReports.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 200.dp),
                                                colors = CardColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledContainerColor = Color.LightGray,
                                                    disabledContentColor = Color.DarkGray
                                                ),
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                ) {
                                                    Text(
                                                        "${tutorReports.size} Reports",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp
                                                    )
                                                    Column(
                                                        modifier = Modifier.verticalScroll(rememberScrollState()),
                                                    ) {
                                                        tutorReports.forEach { report ->
                                                            ReportItem(
                                                                report,
                                                                viewModel = viewModel
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (expandedTutorId.value == tutor.id) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledContainerColor = Color.LightGray,
                                                    disabledContentColor = Color.DarkGray
                                                ),
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                ) {
                                                    Row {
                                                        Text(
                                                            "Email:    ",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp
                                                        )
                                                        Text(
                                                            tutor.email, color = Color(0xFF2196F3),
                                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                            fontSize = 18.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    Row(
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Button(
                                                            modifier = Modifier.weight(0.6f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFF06C59C),
                                                            ),
                                                            onClick = {
                                                                // go to tutors profile
                                                                println("Tutor ID: ${tutor.id}")
                                                                navController.navigate("profile/${tutor.id}")
                                                            }
                                                        ) {
                                                            Text(
                                                                "View Profile",
                                                                color = Color.White
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(16.dp))

                                                        Button(
                                                            modifier = Modifier.weight(0.4f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color.Red,
                                                            ),
                                                            onClick = {
                                                                showDeactivateDialog.value = true
                                                                tutorToDeactivate.value = tutor
                                                            }
                                                        ) {
                                                            Text("Deactivate", color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        Spacer(modifier = Modifier.height(30.dp))

                        Text(
                            "Reported Users",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "${usersWithReports.size} Reported Users",
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier.heightIn(max = 700.dp)
                                .border(1.dp, Color(0xFF54A4FF), shape = RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // REPORTED USERS
                                usersWithReports.forEach { user ->
                                    val usersReports = reports.filter { it.userId == user.id }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = Color.LightGray,
                                                disabledContentColor = Color.DarkGray
                                            ),
                                            onClick = {
                                                expandUserReports.value =
                                                    if (expandUserReports.value == user.id) {
                                                        null // Collapse the card if already expanded
                                                    } else {
                                                        user.id // Expand the clicked tutor's card
                                                    }
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // profile picture
                                                Spacer(modifier = Modifier.width(16.dp))
                                                AsyncImage(
                                                    model = profilePictures[user.id]
                                                        ?: R.drawable.chalkitup,
                                                    contentDescription = "Profile Picture",
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .clip(CircleShape)
                                                        .border(2.dp, Color.Gray, CircleShape)
                                                )

                                                Text(
                                                    "${user.firstName} ${user.lastName}",
                                                    modifier = Modifier.padding(
                                                        vertical = 22.dp,
                                                        horizontal = 20.dp
                                                    ),
                                                    fontSize = 18.sp,
                                                )

                                                Box(modifier = Modifier.weight(1f))
                                                Text(
                                                    "${usersReports.size}",
                                                    color = Color.Red,
                                                    fontWeight = FontWeight.Bold, fontSize = 18.sp
                                                )
                                                IconButton(onClick = {
                                                    expandUserReports.value =
                                                        if (expandUserReports.value == user.id) {
                                                            null // Collapse the card if already expanded
                                                        } else {
                                                            user.id // Expand the clicked tutor's card
                                                        }
                                                }
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.baseline_report_gmailerrorred_24),
                                                        contentDescription = "Report icon",
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                            }
                                        }
                                    }
                                    if (expandUserReports.value == user.id) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 200.dp)
                                                ,
                                                colors = CardColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledContainerColor = Color.LightGray,
                                                    disabledContentColor = Color.DarkGray
                                                ),
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text(
                                                        "${usersReports.size} Reports",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp
                                                    )
                                                    Column(
                                                        modifier = Modifier.verticalScroll(rememberScrollState())
                                                            .background(shape = RoundedCornerShape(8.dp), color = Color.Transparent)
                                                    ) {
                                                        usersReports.forEach { report ->
                                                            ReportItem(
                                                                report,
                                                                viewModel = viewModel
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (expandUserReports.value == user.id) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp)
                                            ,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledContainerColor = Color.LightGray,
                                                    disabledContentColor = Color.DarkGray
                                                ),
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                ) {
                                                    Row {
                                                        Text(
                                                            "Email:    ",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp
                                                        )
                                                        Text(
                                                            user.email, color = Color(0xFF2196F3),
                                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                            fontSize = 18.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))

                                                    Row(
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Button(
                                                            modifier = Modifier.weight(0.6f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFF06C59C),
                                                            ),
                                                            onClick = {
                                                                // go to tutors profile
                                                                println("Tutor ID: ${user.id}")
                                                                navController.navigate("profile/${user.id}")
                                                            }
                                                        ) {
                                                            Text(
                                                                "View Profile",
                                                                color = Color.White
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(16.dp))

                                                        Button(
                                                            modifier = Modifier.weight(0.4f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color.Red,
                                                            ),
                                                            onClick = {
                                                                showDeactivateDialog.value = true
                                                                tutorToDeactivate.value =
                                                                    user
                                                            }
                                                        ) {
                                                            Text("Deactivate", color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            title = {
                Text("Confirm Approval")
            },
            text = {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Are you sure you want to approve this tutor? " +
                                "This tutor will have full access to ChalkItUp and will be matched to sessions."
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Message to Tutor") },
                        placeholder = { Text("Message to Tutor") }
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF06C59C),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        tutorToApprove.value?.let {
                            viewModel.approveTutor(it.id)
                        }
                        reason = ""
                        showDialog.value = false
                    }
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        showDialog.value = false
                        reason = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDenyDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDenyDialog.value = false
            },
            title = {
                Text("Confirm Denial")
            },
            text = {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Are you sure you want to deny this tutor? " +
                                "This tutor will be notified of their denial and prompted to delete their account. " +
                                "They will have access to ChalkItUp but not be actively matched to sessions."
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Denial") },
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        tutorToDeny.value?.let {
                            viewModel.denyTutor(it, reason, "deny")
                        }
                        reason = ""
                        showDenyDialog.value = false
                    }
                ) {
                    Text("Deny this Tutor")
                }
            },
            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        showDenyDialog.value = false
                        reason = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeactivateDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDeactivateDialog.value = false
            },
            title = {
                Text("Confirm Deactivation")
            },
            text = {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Are you sure you want to deactivate this user? " +
                                "This user will be notified of their account's deactivation and be prompted to delete their account. " +
                                "They will have access to ChalkItUp but cannot book sessions and wont be actively matched to sessions."
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Deactivation") },
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        tutorToDeactivate.value?.let {
                            viewModel.denyTutor(it, reason, "deactivate")
                        }
                        reason = ""
                        showDeactivateDialog.value = false
                    }
                ) {
                    Text("Deactivate")
                }
            },
            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        showDeactivateDialog.value = false
                        reason = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CertificationDisplay(tutor: User, certificationViewModel: CertificationViewModel) {
    val certifications by certificationViewModel.certifications.collectAsState()

    LaunchedEffect(Unit) {
        certificationViewModel.getCertifications(tutor.id)
    }

    // Context to access system resources, such as accessing file URIs.
    val context = LocalContext.current

    // File URI for the certification that the user intends to open.
    val fileUri by certificationViewModel.fileUri.observeAsState()

    // If no certifications found, display a message.
    if (certifications.isEmpty()) {
        Text("No certifications found.")
    } else {
        // Display certifications in a grid.
        CertificationGrid(certifications,
            onItemClick = { fileName ->
                certificationViewModel.downloadFileToCache(tutor.id) // Download selected certification file
            }
        )
    }

    // Register a lifecycle observer to reset file URI when app resumes.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = AppLifecycleObserver {
            // Reset the fileUri when app is resumed.
            certificationViewModel.resetFileUri()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Observe and launch file opening if a file URI is available.
    LaunchedEffect(fileUri) {
        fileUri?.let { uri ->
            certificationViewModel.openFile(context, uri) // Open the certification file
        }
    }
}

@Composable
fun SubjectDisplay(tutor: User) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tutor.subjects) { subject ->
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                // Temporary icons for subjects
                val subjectIcon = when (subject.subject) {
                    "Math" -> R.drawable.ic_math2
                    "Physics" -> R.drawable.ic_physics2
                    "Chemistry" -> R.drawable.ic_chemistry2
                    "Social" -> R.drawable.ic_social2
                    "English" -> R.drawable.ic_english2
                    "Science" -> R.drawable.ic_science2
                    "Biology" -> R.drawable.ic_biology
                    else -> R.drawable.chalkitup // havent chosen an icon yet.
                }

                Image(
                    painter = painterResource(id = subjectIcon),
                    contentDescription = subject.subject,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = subject.subject,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReportItem(report: Report, viewModel: AdminHomeViewModel) {
    var showResolveDialog by remember { mutableStateOf(false) }

    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = {
                showResolveDialog = false
            },
            title = {
                Text("Resolve Report")
            },
            text = {
                Text("Resolve this report?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Resolve the report
                        viewModel.resolveReport(report)
                        showResolveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF06C59C),
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Resolve")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showResolveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFd2e5fa), shape = RoundedCornerShape(4.dp))
            .padding(10.dp)
            .clickable {
                // Handle report item click
                showResolveDialog = true
            }
    ) {
        // Convert Firestore Timestamp to Date
        val formattedDate = report.timestamp?.let { timestamp ->
            val date = timestamp.toDate() // This should work if you're using Firebase Timestamp
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
        } ?: "No Date Available"

        Text(
            text = formattedDate,
            fontSize = 12.sp,
            color = Color.Black
        )

        Text(text = report.reportMessage, fontSize = 14.sp,
            color = Color.Black)
    }
}
