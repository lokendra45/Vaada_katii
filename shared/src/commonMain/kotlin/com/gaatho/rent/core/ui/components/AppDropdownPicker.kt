package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> AppDropdownPicker(
    options: ImmutableList<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select...",
    itemLabel: (T) -> String = { it.toString() },
    leadingIcon: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0.dp) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier.onGloballyPositioned {
                anchorWidth = with(density) { it.size.width.toDp() }
            }
        ) {
            // Pill Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = if (selectedItem != null) itemLabel(selectedItem) else placeholder,
                    style = if (selectedItem != null) {
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // White squared icon button on the right
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val transitionState = remember { androidx.compose.animation.core.MutableTransitionState(false) }
            transitionState.targetState = expanded

            if (transitionState.currentState || transitionState.targetState) {
                androidx.compose.ui.window.Popup(
                    onDismissRequest = { expanded = false },
                    properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visibleState = transitionState,
                        enter = androidx.compose.animation.expandVertically(
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            ),
                            expandFrom = Alignment.Top
                        ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) + androidx.compose.animation.scaleIn(
                            initialScale = 0.95f,
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f),
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            )
                        ),
                        exit = androidx.compose.animation.shrinkVertically(
                            animationSpec = androidx.compose.animation.core.tween(200),
                            shrinkTowards = Alignment.Top
                        ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) + androidx.compose.animation.scaleOut(
                            targetScale = 0.95f,
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f),
                            animationSpec = androidx.compose.animation.core.tween(200)
                        )
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .width(if (anchorWidth > 0.dp) anchorWidth else 200.dp),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 8.dp,
                            color = MaterialTheme.colorScheme.surfaceContainerLowest
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                options.forEach { item ->
                                    val isSelected = item == selectedItem
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onItemSelected(item)
                                                expanded = false
                                            }
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                        } else {
                                            Spacer(modifier = Modifier.width(32.dp))
                                        }
                                        Text(
                                            text = itemLabel(item),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
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