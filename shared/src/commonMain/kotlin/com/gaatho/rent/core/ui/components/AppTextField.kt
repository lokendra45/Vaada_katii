package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    errorMessage: String? = null,
    prefix: String? = null,
    prefixColor: Color? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    labelStyle: TextStyle? = null,
    fieldTextStyle: TextStyle? = null,
    shape: Shape = RoundedCornerShape(AppDimensions.TextFieldCornerRadius),
    onClick: (() -> Unit)? = null
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    
    // Synchronize internal state with external value if it changes
    LaunchedEffect(value) {
        if (value != textFieldValueState.text) {
            textFieldValueState = textFieldValueState.copy(text = value)
        }
    }

    AppTextField(
        value = textFieldValueState,
        onValueChange = { newValue ->
            textFieldValueState = newValue
            if (value != newValue.text) {
                onValueChange(newValue.text)
            }
        },
        modifier = modifier,
        label = label,
        topRightLabel = topRightLabel,
        placeholder = placeholder,
        errorMessage = errorMessage,
        prefix = prefix,
        prefixColor = prefixColor,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        readOnly = readOnly,
        enabled = enabled,
        textStyle = textStyle,
        labelStyle = labelStyle,
        fieldTextStyle = fieldTextStyle,
        shape = shape,
        onClick = onClick
    )
}

@Composable
fun AppTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    topRightLabel: String? = null,
    placeholder: String? = null,
    errorMessage: String? = null,
    prefix: String? = null,
    prefixColor: Color? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    labelStyle: TextStyle? = null,
    fieldTextStyle: TextStyle? = null,
    shape: Shape = RoundedCornerShape(AppDimensions.TextFieldCornerRadius),
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        errorMessage != null -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val effectiveLabelStyle = labelStyle ?: MaterialTheme.typography.labelLarge
    val effectiveFieldStyle = fieldTextStyle ?: MaterialTheme.typography.bodyLarge

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null || topRightLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppDimensions.FieldLabelGap),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = effectiveLabelStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (topRightLabel != null) {
                    Text(
                        text = topRightLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (singleLine) AppDimensions.TextFieldHeight else 90.dp)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(AppDimensions.TextFieldBorderWidth, borderColor)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppDimensions.TextFieldHorizontalPadding,
                        vertical = if (singleLine) 0.dp else 14.dp
                    ),
                textStyle = textStyle.merge(
                    effectiveFieldStyle.copy(color = MaterialTheme.colorScheme.onSurface)
                ),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                readOnly = readOnly,
                enabled = enabled,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (singleLine) AppDimensions.TextFieldHeight else 62.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (leadingIcon != null) {
                            leadingIcon()
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        if (prefix != null) {
                            Text(
                                text = prefix,
                                style = effectiveFieldStyle.copy(
                                    color = prefixColor ?: MaterialTheme.colorScheme.onSurface
                                ),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            if (value.text.isEmpty() && placeholder != null) {
                                Text(
                                    text = placeholder,
                                    style = effectiveFieldStyle.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            innerTextField()
                        }

                        if (trailingIcon != null) {
                            Spacer(modifier = Modifier.width(10.dp))
                            trailingIcon()
                        }
                    }
                }
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun AppTextFieldLightPreview() {
    RentManagerTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = "",
                onValueChange = {},
                label = "Property Name",
                placeholder = "Baluwatar House"
            )
            AppTextField(
                value = "Ward No. 4, Baluwatar",
                onValueChange = {},
                label = "Street Address"
            )
        }
    }
}
