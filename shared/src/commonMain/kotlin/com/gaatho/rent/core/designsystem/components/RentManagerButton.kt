package com.gaatho.rent.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.AppDimensions
import com.gaatho.rent.core.designsystem.AppShadow.figmaButtonShadow

@Composable
fun RentManagerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppDimensions.ActionButtonRadius)
    Button(
        onClick = onClick,
        modifier = modifier
            .height(AppDimensions.ButtonHeightMedium)
            .figmaButtonShadow(shape = shape, prominent = true),
        enabled = enabled,
        shape = shape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = ButtonDefaults.ContentPadding,
        content = content
    )
}


@Composable
fun RentManagerPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    RentManagerButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            com.gaatho.rent.core.ui.components.AppExpressiveLoadingIndicator(
                modifier = Modifier.size(26.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                contained = true,
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}


/**
 * Figma-style outlined secondary button (e.g. "Edit Property"): white container,
 * primary-colored border and label, 44dp height / 12dp radius from design tokens.
 */
@Composable
fun RentManagerOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    val shape = RoundedCornerShape(AppDimensions.ActionButtonRadius)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(AppDimensions.ButtonHeightMedium)
            .figmaButtonShadow(shape = shape),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(color = contentColor)
        )
    }
}
