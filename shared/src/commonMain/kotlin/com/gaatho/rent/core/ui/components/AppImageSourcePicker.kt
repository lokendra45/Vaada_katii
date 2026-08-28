package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.component_camera
import rentmanagerapp.shared.generated.resources.component_gallery
import rentmanagerapp.shared.generated.resources.component_select_image_source

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
            CardTitle(
                text = stringResource(Res.string.component_select_image_source),
                modifier = Modifier.padding(bottom = 16.dp, start = 16.dp)
            )

            ListItem(
                headlineContent = { Text(stringResource(Res.string.component_gallery)) },
                leadingContent = {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    onGalleryClick()
                    onDismissRequest()
                }
            )

            ListItem(
                headlineContent = { Text(stringResource(Res.string.component_camera)) },
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
