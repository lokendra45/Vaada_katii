package com.gaatho.rent.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.RentManagerTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Colors used by [AppDropdown]. Create with [AppDropdownDefaults.colors], overriding only
 * what you need — the same pattern Material 3 uses for `OutlinedTextFieldDefaults.colors()`.
 */
@Immutable
data class AppDropdownColors(
    val containerColor: Color,
    val focusedContainerColor: Color,
    val disabledContainerColor: Color,
    val errorContainerColor: Color,
    val borderColor: Color,
    val errorBorderColor: Color,
    val textColor: Color,
    val placeholderColor: Color,
    val disabledTextColor: Color,
    val iconColor: Color,
    val labelColor: Color,
    val supportingTextColor: Color,
    val errorSupportingTextColor: Color,
    val selectedItemColor: Color,
    val selectedItemBackgroundColor: Color,
)

object AppDropdownDefaults {
    @Composable
    fun colors(
        containerColor: Color = Color.Transparent,
        focusedContainerColor: Color = Color.Transparent,
        disabledContainerColor: Color = Color.Transparent,
        errorContainerColor: Color = Color.Transparent,
        borderColor: Color = MaterialTheme.colorScheme.primary,
        errorBorderColor: Color = MaterialTheme.colorScheme.error,
        textColor: Color = MaterialTheme.colorScheme.onSurface,
        placeholderColor: Color = MaterialTheme.colorScheme.outline,
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        supportingTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        errorSupportingTextColor: Color = MaterialTheme.colorScheme.error,
        selectedItemColor: Color = MaterialTheme.colorScheme.primary,
        selectedItemBackgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    ): AppDropdownColors = AppDropdownColors(
        containerColor = containerColor,
        focusedContainerColor = focusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        errorContainerColor = errorContainerColor,
        borderColor = borderColor,
        errorBorderColor = errorBorderColor,
        textColor = textColor,
        placeholderColor = placeholderColor,
        disabledTextColor = disabledTextColor,
        iconColor = iconColor,
        labelColor = labelColor,
        supportingTextColor = supportingTextColor,
        errorSupportingTextColor = errorSupportingTextColor,
        selectedItemColor = selectedItemColor,
        selectedItemBackgroundColor = selectedItemBackgroundColor,
    )
}

/**
 * A high-fidelity, fully themeable Material 3 style dropdown.
 *
 * Highlights over a plain `DropdownMenu`:
 * - Animated border/container color transitions and a smoothly rotating chevron
 * - Optional error + supporting text, matching Material 3 text field conventions
 * - Optional clear ("x") action once a value is selected
 * - Optional in-menu search/filter for long option lists
 * - Selected item highlighted in the menu, with per-item leading icons or a full custom row
 * - Every color, shape, border width and size is overridable via [AppDropdownColors]
 *
 * @param T The type of the items in the dropdown.
 * @param options List of items to display.
 * @param selectedItem The currently selected item, or null for none.
 * @param onItemSelected Callback when an item is selected.
 * @param itemLabel Mapping from item to display string.
 * @param itemLeadingIcon Optional per-item leading icon, shown before [itemLabel]'s text.
 * @param itemContent Full override for a menu row's content; when provided, [itemLabel] and
 *   [itemLeadingIcon] are ignored for menu rows (the trigger still uses [itemLabel]).
 * @param label Optional top label, shown above the field.
 * @param placeholder Placeholder text shown when nothing is selected.
 * @param supportingText Optional helper text shown below the field.
 * @param errorText Optional text shown below the field instead of [supportingText] when [isError] is true.
 * @param leadingIcon Optional leading icon for the trigger field itself.
 * @param trailingIcon Optional override for the trailing icon; receives the current expanded state.
 *   Defaults to a chevron that rotates smoothly on open/close.
 * @param onClear When non-null and a value is selected, shows a small clear button that invokes this.
 * @param enabled Whether the dropdown can be opened.
 * @param isError Whether to render the field in its error state.
 * @param searchable When true, shows a search field at the top of the menu to filter options.
 * @param searchPlaceholder Placeholder for the search field when [searchable] is true.
 * @param shape Shape of the trigger field and menu container.
 * @param height Fixed height of the trigger field.
 * @param borderWidth Width of the trigger field's border.
 * @param menuMaxHeight Maximum height of the dropdown menu before it scrolls.
 * @param showDividers Whether to draw a divider between menu items.
 * @param colors Full color customization, see [AppDropdownDefaults.colors].
 */
@Composable
fun <T> AppDropdown(
    options: ImmutableList<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: (T) -> String = { it.toString() },
    itemLeadingIcon: (@Composable (T) -> Unit)? = null,
    itemContent: (@Composable RowScope.(item: T, isSelected: Boolean) -> Unit)? = null,
    label: String? = null,
    placeholder: String = "Select...",
    supportingText: String? = null,
    errorText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: (@Composable (expanded: Boolean) -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    searchable: Boolean = false,
    searchPlaceholder: String = "Search...",
    shape: Shape = RoundedCornerShape(AppDimensions.TextFieldCornerRadius),
    height: Dp = 48.dp,
    borderWidth: Dp = AppDimensions.TextFieldBorderWidth,
    menuMaxHeight: Dp = 320.dp,
    showDividers: Boolean = false,
    labelStyle: TextStyle? = null,
    fieldTextStyle: TextStyle? = null,
    colors: AppDropdownColors = AppDropdownDefaults.colors(),
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(expanded) {
        if (expanded && searchable) {
            focusRequester.requestFocus()
        }
    }

    val filteredOptions: List<T> = remember(options, searchQuery, searchable) {
        if (searchable && searchQuery.isNotBlank()) {
            options.filter { itemLabel(it).contains(searchQuery, ignoreCase = true) }
        } else {
            options
        }
    }

    val targetContainerColor = when {
        !enabled -> colors.disabledContainerColor
        isError -> colors.errorContainerColor
        expanded -> colors.focusedContainerColor
        else -> colors.containerColor
    }
    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(200),
        label = "AppDropdownContainerColor",
    )

    val targetBorderColor = when {
        isError -> colors.errorBorderColor
        expanded -> colors.borderColor
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(200),
        label = "AppDropdownBorderColor",
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "AppDropdownChevronRotation",
    )

    Column(modifier = modifier) {
        // Label is now passed into AppTextField

        Box {
            AppTextField(
                value = if (selectedItem != null) itemLabel(selectedItem) else "",
                onValueChange = {},
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onClear != null && selectedItem != null && enabled) {
                            IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear selection",
                                    tint = colors.iconColor,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                        }
        
                        if (trailingIcon != null) {
                            trailingIcon(expanded)
                        } else {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colors.iconColor,
                                modifier = Modifier.rotate(chevronRotation),
                            )
                        }
                    }
                },
                readOnly = true,
                enabled = enabled,
                labelStyle = labelStyle,
                fieldTextStyle = fieldTextStyle,
                shape = shape,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Transparent overlay to intercept clicks
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = enabled) { expanded = !expanded }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchQuery = ""
                },
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = menuMaxHeight)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            ) {
                if (searchable) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(searchPlaceholder) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .focusRequester(focusRequester),
                    )
                    HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))
                }

                if (filteredOptions.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        onClick = {},
                        enabled = false,
                    )
                } else {
                    filteredOptions.forEachIndexed { index, item ->
                        val isSelected = item == selectedItem

                        DropdownMenuItem(
                            text = {
                                if (itemContent != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        itemContent(item, isSelected)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (itemLeadingIcon != null) {
                                            itemLeadingIcon(item)
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = itemLabel(item),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) colors.selectedItemColor else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                                searchQuery = ""
                            },
                            modifier = if (isSelected) {
                                Modifier.background(colors.selectedItemBackgroundColor)
                            } else {
                                Modifier
                            },
                            colors = MenuDefaults.itemColors(),
                        )

                        if (showDividers && index < filteredOptions.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }

        val bottomText = if (isError) errorText else supportingText
        if (bottomText != null) {
            Text(
                text = bottomText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) colors.errorSupportingTextColor else colors.supportingTextColor,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}