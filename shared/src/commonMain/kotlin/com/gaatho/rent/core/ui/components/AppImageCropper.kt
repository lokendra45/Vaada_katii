package com.gaatho.rent.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.attafitamim.krop.core.crop.ImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog

/**
 * A reusable wrapper for the Krop ImageCropperDialog with a premium UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppImageCropper(
    imageCropper: ImageCropper,
    modifier: Modifier = Modifier,
    title: String = "Adjust Image"
) {
    val cropState = imageCropper.cropState
    if (cropState != null) {
        ImageCropperDialog(
            state = cropState,
            topBar = { state ->
                // Elegant slide-down entrance for the TopAppBar
                val visibleState = remember { 
                    MutableTransitionState(false).apply { targetState = true } 
                }
                
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = fadeIn(animationSpec = tween(400)) + 
                            slideInVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f)) { -it }
                ) {
                    val doneTint by animateColorAsState(
                        targetValue = if (!state.accepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(300),
                        label = "done_tint_animation"
                    )

                    TopAppBar(
                        title = { 
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { state.done(accept = false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel")
                            }
                        },
                        actions = {
                            IconButton(onClick = { state.reset() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset")
                            }
                            IconButton(
                                onClick = { state.done(accept = true) }, 
                                enabled = !state.accepted
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check, 
                                    contentDescription = "Done",
                                    tint = doneTint
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        )
    }
}
