package com.gaatho.rent.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SearchSuggestionItem(
    val title: String,
    val subtitle: String? = null,
    val category: String = "Suggestion"
)

/**
 * Material 3 Expressive Search Bar (`M3 Expressive` specifications).
 *
 * Features:
 * - Expressive Spring Physics: Ultra-fluid spring motion (`DampingRatioLowBouncy`) for elevation and container expansions.
 * - Dynamic Corner Morphing: Bottom corners gracefully morph from 28dp pill into 16dp docked corners when active with suggestions.
 * - Tonal Elevation & Surface Container: Uses rich tonal layering with 0dp border for a modern, emotionally resonant feel.
 * - Animated `<` Back Arrow & Compact Horizontal Suggestion Chips strip (`LazyRow`).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Search...",
    suggestions: List<SearchSuggestionItem> = emptyList(),
    onSuggestionSelected: (SearchSuggestionItem) -> Unit = { onQueryChange(it.title) },
    onSearchClicked: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val isActive = isFocused || query.isNotEmpty()
    val hasVisibleSuggestions = isFocused && suggestions.isNotEmpty()

    // Expressive Spring Animation for Elevation (0.dp -> 6.dp active)
    val elevation by animateDpAsState(
        targetValue = if (isActive) 6.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = 300f
        ),
        label = "expressive_elevation"
    )

    // M3 Expressive "Grow-Wider" Effect: side margin springs from 16.dp resting down to 2.dp when focused (`overview 01` video specification)
    val horizontalMargin by animateDpAsState(
        targetValue = if (isActive) 2.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = 300f
        ),
        label = "expressive_grow_wider"
    )

    val containerShape = RoundedCornerShape(24.dp)

    // Smooth color animation: soft surface tint when resting -> crisp clean white/surface when focused (prevents dark gray look)
    val containerColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(200),
        label = "expressive_container_color"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Main 48dp Material 3 Expressive Search Container (subtle and sleek)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalMargin)
                .height(48.dp)
                .shadow(
                    elevation = elevation,
                    shape = containerShape,
                    clip = false
                ),
            shape = containerShape,
            color = containerColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon (< Back Arrow vs Search Magnifier with M3 Expressive crossfade)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (isActive) {
                                focusManager.clearFocus()
                                isFocused = false
                                if (query.isNotEmpty()) {
                                    onQueryChange("")
                                }
                            } else {
                                onSearchClicked()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isActive,
                        transitionSpec = {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(180))
                        },
                        label = "expressive_leading_icon"
                    ) { active ->
                        if (active) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back or clear search focus",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text Input Field Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholderText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                isFocused = false
                                onSearchClicked()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                isFocused = state.isFocused
                            }
                    )
                }

                // Trailing Clear ('X') Icon with spring fade
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                onQueryChange("")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear query",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}
