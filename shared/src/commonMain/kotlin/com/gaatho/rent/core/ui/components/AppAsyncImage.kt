package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import org.jetbrains.compose.resources.painterResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.placeholder_avatar
import rentmanagerapp.shared.generated.resources.placeholder_image

enum class PlaceholderType {
    AVATAR,
    IMAGE,
    NONE
}

@Composable
fun AppAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderType: PlaceholderType = PlaceholderType.IMAGE,
) {
    var isLoading by remember { mutableStateOf(false) }

    val placeholderPainter = when (placeholderType) {
        PlaceholderType.AVATAR -> painterResource(Res.drawable.placeholder_avatar)
        PlaceholderType.IMAGE -> painterResource(Res.drawable.placeholder_image)
        PlaceholderType.NONE -> null
    }

    Box(modifier = modifier) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale,
            error = placeholderPainter,
            fallback = placeholderPainter,
            placeholder = placeholderPainter,
            onLoading = {
                isLoading = true
            },
            onSuccess = {
                isLoading = false
            },
            onError = {
                isLoading = false
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
