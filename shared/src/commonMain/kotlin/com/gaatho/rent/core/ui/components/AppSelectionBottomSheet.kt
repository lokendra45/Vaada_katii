package com.gaatho.rent.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.Radius
import com.gaatho.rent.core.designsystem.Spacing

/**
 * A generic, reusable, searchable selection bottom sheet.
 *
 * Features:
 * - Spring-animated item selection with rubber bounce
 * - Optional search bar to filter items
 * - Optional subtitle per item
 * - Checkmark on selected item
 *
 * Usage:
 * ```
 * AppSelectionBottomSheet(
 *     title = "Select Property",
 *     items = properties.map { AppSelectionItem(it.id, it.name, it.address) },
 *     selectedId = selectedPropertyId,
 *     onItemSelected = { id -> onPropertySelected(id) },
 *     onDismiss = { showSheet = false }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppSelectionBottomSheet(
    title: String,
    items: List<AppSelectionItem<T>>,
    selectedId: T?,
    onItemSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    searchable: Boolean = items.size > 5,
    searchPlaceholder: String = "Search...",
    emptyText: String = "No items found"
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isBlank()) items
        else items.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Title
            SectionTitle(
                text = title,
                modifier = Modifier.padding(horizontal = Spacing.ScreenPadding, vertical = Spacing.StackTight)
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.ScreenPadding),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Optional search bar
            if (searchable) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(searchPlaceholder) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(Radius.Md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.ScreenPadding, vertical = Spacing.StackTight)
                )
            } else {
                Spacer(modifier = Modifier.height(Spacing.StackTight))
            }

            // Items list
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BodyText(
                        text = emptyText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = Spacing.ScreenPadding,
                        vertical = Spacing.StackTight
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredItems, key = { it.id.toString() }) { item ->
                        SelectionItemRow(
                            item = item,
                            isSelected = item.id == selectedId,
                            onSelected = {
                                onItemSelected(item.id)
                                onDismiss()
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun <T> SelectionItemRow(
    item: AppSelectionItem<T>,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "item_scale"
    )

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(100)
            pressed = false
        }
    }

    Surface(
        onClick = { pressed = true; onSelected() },
        shape = RoundedCornerShape(Radius.Md),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else
            Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                CardTitle(
                    text = item.title,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (item.subtitle != null) {
                    BodySmallText(
                        text = item.subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Data model for a selectable item in [AppSelectionBottomSheet].
 *
 * @param id Unique identifier (can be any type T).
 * @param title Primary display text.
 * @param subtitle Optional secondary text shown below the title.
 */
data class AppSelectionItem<T>(
    val id: T,
    val title: String,
    val subtitle: String? = null
)
