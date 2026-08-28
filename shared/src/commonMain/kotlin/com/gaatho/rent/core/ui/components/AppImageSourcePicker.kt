package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppImageSourcePicker(
    onDismissRequest: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "Select Image Source",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ListItem(
                headlineContent = { Text("Gallery") },
                leadingContent = {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    onGalleryClick()
                    onDismissRequest()
                }
            )

            ListItem(
                headlineContent = { Text("Camera") },
                leadingContent = {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    onCameraClick()
                    onDismissRequest()
                }
            )
        }
    }
}

// Extension to make it easier to use with clickable
@Composable
fun Modifier.clickable(onClick: () -> Unit): Modifier = this.then(
    androidx.compose.foundation.clickable(onClick = onClick)
)
