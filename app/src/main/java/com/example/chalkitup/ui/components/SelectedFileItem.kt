package com.example.chalkitup.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Composable function to display a selected file in a card view with an optional image preview.
 *
 * This function displays the file name and an optional image preview if the selected file is an image.
 * It also includes a delete button to remove the selected file.
 *
 * Current usage: EditProfileScreen, SignupScreen
 *
 * @param fileName The name of the selected file to display.
 * @param fileUri The URI of the selected file used to load the file or image.
 * @param onRemove A callback function that is triggered when the delete button is pressed to remove the selected file.
 */

@Composable
fun SelectedFileItem(fileName: String, fileUri: Uri, onRemove: () -> Unit) {
    // Get the current context and content resolver to retrieve file metadata
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    // Retrieve the MIME type of the selected file using its URI
    val mimeType = contentResolver.getType(fileUri)
    val isImage = mimeType?.startsWith("image/") == true

    // Card displaying the file information and actions
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isImage) 150.dp else 50.dp) // make height smaller if the file is not an image
            .padding(vertical = 3.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardColors(
            containerColor = Color(0xFF54A4FF),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF54A4FF),
            disabledContentColor = Color.White
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = fileName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // IconButton for removing the file
                IconButton(onClick = { onRemove() }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove File")
                }
            }

            // Check if the selected file is an image
            if (isImage) {
                // Display image preview if the file is an image
                AsyncImage(
                    model = fileUri, // Load the image from the URI
                    contentDescription = "Selected Image", // Description for the image
                    modifier = Modifier
                        .fillMaxWidth()  // Ensures the image takes up the full width
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop // Ensures the image fills width and crops excess
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}