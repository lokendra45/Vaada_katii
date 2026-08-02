package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.RentManagerTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    topRightLabel: String? = null,
    placeholder: String? = null,
    prefix: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier) {
        // Top Labels Row
        if (label != null || topRightLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label != null) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp)) // Maintain alignment if only topRightLabel is present
                }

                if (topRightLabel != null) {
                    Text(
                        text = topRightLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        val mergedTextStyle = textStyle.merge(
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        )
        
        // Text Field Box
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = mergedTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            readOnly = readOnly,
            enabled = enabled,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Leading Icon
                        if (leadingIcon != null) {
                            leadingIcon()
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        // Prefix
                        if (prefix != null) {
                            Text(
                                text = prefix,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Text input or placeholder
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty() && placeholder != null) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            }
                            innerTextField()
                        }

                        // Trailing Icon
                        if (trailingIcon != null) {
                            Spacer(modifier = Modifier.width(12.dp))
                            trailingIcon()
                        }
                    }
                    
                    // Thin horizontal divider below the input
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun AppTextFieldPreviewContent(darkTheme: Boolean) {
    RentManagerTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(
                    value = "",
                    onValueChange = {},
                    label = "Full Name",
                    placeholder = "e.g. Ram Bahadur Thapa"
                )
                AppTextField(
                    value = "9841234567",
                    onValueChange = {},
                    label = "Phone Number",
                    prefix = "+977"
                )
            }
        }
    }
}

@Preview
@Composable
private fun AppTextFieldLightPreview() {
    AppTextFieldPreviewContent(darkTheme = false)
}

@Preview
@Composable
private fun AppTextFieldDarkPreview() {
    AppTextFieldPreviewContent(darkTheme = true)
}
