package com.gaatho.rent.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaatho.rent.core.designsystem.RentManagerTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Snackbar variant type. Each has distinct icon + color token.
 */
enum class AppSnackbarVariant {
    SUCCESS, ERROR, WARNING, INFO
}

/**
 * Persistent state holder for the reusable [AppSnackbarHost].
 * Use [rememberAppSnackbarState] to create and hoist it.
 */
@Stable
class AppSnackbarState {
    var isVisible by mutableStateOf(false)
        private set
    var message by mutableStateOf("")
        private set
    var variant by mutableStateOf(AppSnackbarVariant.INFO)
        private set
    var actionLabel by mutableStateOf<String?>(null)
        private set
    private var onAction: (() -> Unit)? = null

    suspend fun show(
        message: String,
        variant: AppSnackbarVariant = AppSnackbarVariant.INFO,
        durationMs: Long = 3000L,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        this.message = message
        this.variant = variant
        this.actionLabel = actionLabel
        this.onAction = onAction
        isVisible = true
        delay(durationMs.milliseconds)
        dismiss()
    }

    fun dismiss() {
        isVisible = false
        onAction = null
    }

    fun performAction() {
        onAction?.invoke()
        dismiss()
    }
}

@Composable
fun rememberAppSnackbarState(): AppSnackbarState = remember { AppSnackbarState() }

/**
 * Drop this at the bottom of any Scaffold or Box.
 * It renders an animated, rubber-spring snackbar pill above navigation bar.
 *
 * Usage:
 * ```
 * val snackbarState = rememberAppSnackbarState()
 *
 * Box {
 *     // Your content
 *     AppSnackbarHost(state = snackbarState)
 * }
 *
 * // Trigger from a coroutine:
 * scope.launch { snackbarState.show("Payment saved!", AppSnackbarVariant.SUCCESS) }
 * ```
 */
@Composable
fun AppSnackbarHost(
    state: AppSnackbarState,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomCenter
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = alignment
    ) {
        AnimatedVisibility(
            visible = state.isVisible,
            enter = if (alignment == Alignment.TopCenter) {
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            } else {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            },
            exit = if (alignment == Alignment.TopCenter) {
                slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeOut() + scaleOut(targetScale = 0.9f)
            } else {
                slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                ) + fadeOut() + scaleOut(targetScale = 0.9f)
            }
        ) {
            AppSnackbarContent(
                message = state.message,
                variant = state.variant,
                actionLabel = state.actionLabel,
                onDismiss = state::dismiss
            )
        }
    }
}

@Composable
private fun AppSnackbarContent(
    message: String,
    variant: AppSnackbarVariant,
    actionLabel: String?,
    onDismiss: () -> Unit
) {
    val isDarkTheme = com.gaatho.rent.core.environment.LocalAppTheme.current

    val (backgroundColor, contentColor, iconColor, icon) = when (variant) {
        AppSnackbarVariant.SUCCESS -> SnackbarColors(
            bg = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Success.copy(alpha = 0.2f) else com.gaatho.rent.core.designsystem.AppColors.SuccessContainer,
            content = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Success.copy(alpha = 0.9f) else com.gaatho.rent.core.designsystem.AppColors.Success,
            iconColor = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Success.copy(alpha = 0.9f) else com.gaatho.rent.core.designsystem.AppColors.Success,
            icon = Icons.Default.CheckCircle
        )
        AppSnackbarVariant.ERROR -> SnackbarColors(
            bg = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Error.copy(alpha = 0.2f) else com.gaatho.rent.core.designsystem.AppColors.ErrorContainer,
            content = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Error.copy(alpha = 0.9f) else com.gaatho.rent.core.designsystem.AppColors.Error,
            iconColor = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Error.copy(alpha = 0.9f) else com.gaatho.rent.core.designsystem.AppColors.Error,
            icon = Icons.Default.Error
        )
        AppSnackbarVariant.WARNING -> SnackbarColors(
            bg = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Warning.copy(alpha = 0.2f) else com.gaatho.rent.core.designsystem.AppColors.WarningContainer,
            content = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Warning.copy(alpha = 0.9f) else com.gaatho.rent.core.designsystem.AppColors.Warning,
            iconColor = if (isDarkTheme) com.gaatho.rent.core.designsystem.AppColors.Warning.copy(alpha = 0.9f) else com.gaatho.rent.core.designsystem.AppColors.Warning,
            icon = Icons.Default.Warning
        )
        AppSnackbarVariant.INFO -> SnackbarColors(
            bg = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            iconColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Info
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = 8.dp,
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon badge
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                ),
                modifier = Modifier.weight(1f)
            )

            // Optional action
            if (actionLabel != null) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    ),
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Dismiss button
            Surface(
                onClick = onDismiss,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.dismiss),
                    tint = contentColor,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

private data class SnackbarColors(
    val bg: Color,
    val content: Color,
    val iconColor: Color,
    val icon: ImageVector
)

@Preview
@Composable
private fun AppSnackbarSuccessPreview() {
    RentManagerTheme {
        Box(Modifier.padding(16.dp)) {
            AppSnackbarContent(
                message = "Property created successfully!",
                variant = AppSnackbarVariant.SUCCESS,
                actionLabel = "UNDO",
                onDismiss = {}
            )
        }
    }
}

@Preview
@Composable
private fun AppSnackbarErrorPreview() {
    RentManagerTheme {
        Box(Modifier.padding(16.dp)) {
            AppSnackbarContent(
                message = "Failed to save payment details.",
                variant = AppSnackbarVariant.ERROR,
                actionLabel = "RETRY",
                onDismiss = {}
            )
        }
    }
}

@Preview
@Composable
private fun AppSnackbarWarningPreview() {
    RentManagerTheme {
        Box(Modifier.padding(16.dp)) {
            AppSnackbarContent(
                message = "Your subscription expires in 2 days.",
                variant = AppSnackbarVariant.WARNING,
                actionLabel = "RENEW",
                onDismiss = {}
            )
        }
    }
}

@Preview
@Composable
private fun AppSnackbarInfoPreview() {
    RentManagerTheme {
        Box(Modifier.padding(16.dp)) {
            AppSnackbarContent(
                message = "New feature: Export to PDF is now available.",
                variant = AppSnackbarVariant.INFO,
                actionLabel = "LEARN MORE",
                onDismiss = {}
            )
        }
    }
}
