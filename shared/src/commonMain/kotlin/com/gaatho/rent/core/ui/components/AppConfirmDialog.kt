package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────────────────────
// AppDialog — Unified, highly-customisable dialog system
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Single, composable-first dialog primitive for Rent Manager.
 *
 * Covers every dialog pattern in the app:
 *
 * | [AppDialog.Variant]  | Icon accent  | Confirm button  | Dismiss button  |
 * |----------------------|-------------|-----------------|-----------------|
 * | [Informational]      | Primary     | Primary filled  | (optional)      |
 * | [Success]            | Green       | Primary filled  | (optional)      |
 * | [Warning]            | Amber       | Primary filled  | Text (optional) |
 * | [Destructive]        | Red         | Red filled      | Outline         |
 *
 * Two layout modes:
 * - [AppDialog.Layout.Center] — icon + title centred, body centred below.
 * - [AppDialog.Layout.Horizontal] — icon + title on one row (M3 "side-by-side"),
 *   a divider below, then body + buttons.
 *
 * ## Minimal usage — confirmation
 * ```kotlin
 * AppDialog(
 *     variant     = AppDialog.Variant.Destructive,
 *     icon        = Icons.Default.Delete,
 *     title       = "Delete Property?",
 *     body        = "This cannot be undone.",
 *     confirmText = "Delete",
 *     onConfirm   = { viewModel.onAction(MyAction.DeleteConfirmed) },
 *     onDismiss   = { viewModel.onAction(MyAction.DeleteDismissed) },
 * )
 * ```
 *
 * ## Custom content — payment summary
 * ```kotlin
 * AppDialog(
 *     variant     = AppDialog.Variant.Success,
 *     icon        = Icons.Default.CheckCircle,
 *     title       = "Payment Recorded",
 *     confirmText = "Done",
 *     onConfirm   = onDismiss,
 *     onDismiss   = onDismiss,
 *     dismissText = null,          // single-button mode
 *     bodyContent = {
 *         PaymentSummaryCard(payment)
 *     }
 * )
 * ```
 *
 * @param icon        Icon shown in the coloured circle. Use a meaningful, recognisable icon.
 * @param title       Short imperative title. Keep under 5 words.
 * @param body        Optional plain-text body. Ignored when [bodyContent] is provided.
 * @param bodyContent Optional composable slot — replaces [body] when set. Use for rich content
 *                    (summary cards, annotated strings, checklists).
 * @param confirmText Label on the affirmative / destructive button.
 * @param dismissText Label on the cancel button. Pass `null` for single-button dialogs.
 * @param onConfirm   Called when the user taps the confirm button.
 * @param onDismiss   Called when the user taps outside the dialog or the dismiss button.
 * @param onDismissAction Called when the user taps the dismiss **button** only.
 *                        Defaults to [onDismiss] when not provided.
 * @param variant     Visual style. See [AppDialog.Variant].
 * @param layout      Structural layout. See [AppDialog.Layout].
 * @param dismissible Whether tapping outside dismisses the dialog. Default `true`.
 */
@Composable
fun AppDialog(
    icon: ImageVector,
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    bodyContent: (@Composable ColumnScope.() -> Unit)? = null,
    dismissText: String? = "Cancel",
    onDismissAction: (() -> Unit)? = null,
    variant: AppDialog.Variant = AppDialog.Variant.Informational,
    layout: AppDialog.Layout = AppDialog.Layout.Center,
    dismissible: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = dismissible,
            dismissOnBackPress    = dismissible,
            usePlatformDefaultWidth = false,
        )
    ) {
        AppDialogContent(
            icon            = icon,
            title           = title,
            confirmText     = confirmText,
            onConfirm       = onConfirm,
            dismissText     = dismissText,
            onDismissAction = onDismissAction ?: onDismiss,
            variant         = variant,
            layout          = layout,
            body            = body,
            bodyContent     = bodyContent,
            modifier        = modifier,
        )
    }
}

/**
 * The visual card content of [AppDialog], extracted so it can be previewed
 * independently without a real Dialog window (which the Preview panel cannot render).
 *
 * Use [AppDialog] in production code. Use [AppDialogContent] only in `@Preview` functions.
 */
@Composable
internal fun AppDialogContent(
    icon: ImageVector,
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    bodyContent: (@Composable ColumnScope.() -> Unit)? = null,
    dismissText: String? = "Cancel",
    onDismissAction: () -> Unit = {},
    variant: AppDialog.Variant = AppDialog.Variant.Informational,
    layout: AppDialog.Layout = AppDialog.Layout.Center,
) {
    val colors = AppDialog.variantColors(variant)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest, // pure white, no tonal tint
        tonalElevation = 0.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            when (layout) {
                AppDialog.Layout.Center -> CenteredHeader(icon, title, colors)
                AppDialog.Layout.Horizontal -> {
                    HorizontalHeader(icon, title, colors)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }

            // ── Body ─────────────────────────────────────────────────────
            if (bodyContent != null) {
                Column { bodyContent() }
            } else if (!body.isNullOrBlank()) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = when (layout) {
                        AppDialog.Layout.Center     -> androidx.compose.ui.text.style.TextAlign.Center
                        AppDialog.Layout.Horizontal -> androidx.compose.ui.text.style.TextAlign.Start
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (layout == AppDialog.Layout.Center)
                                Modifier.padding(bottom = 4.dp)
                            else Modifier
                        )
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Buttons ──────────────────────────────────────────────────
            when {
                // Single button (no dismiss)
                dismissText == null -> {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.buttonColor,
                            contentColor   = Color.White
                        )
                    ) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Two buttons — horizontal layout uses end-aligned row
                layout == AppDialog.Layout.Horizontal -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = onDismissAction,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = dismissText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonColor,
                                contentColor   = Color.White
                            )
                        ) {
                            Text(confirmText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Two buttons — center layout uses full-width column stack
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonColor,
                                contentColor   = Color.White
                            )
                        ) {
                            Text(confirmText, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = onDismissAction,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(dismissText, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ─── Header variants ──────────────────────────────────────────────────────────

@Composable
private fun CenteredHeader(
    icon: ImageVector,
    title: String,
    colors: AppDialog.VariantColors
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colors.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HorizontalHeader(
    icon: ImageVector,
    title: String,
    colors: AppDialog.VariantColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── Namespace + variant config ───────────────────────────────────────────────

/**
 * Namespace for [AppDialog] enums and helpers.
 */
object AppDialog {

    /**
     * Visual variant controlling icon accent and confirm button colour.
     *
     * | Variant         | When to use                                          |
     * |-----------------|------------------------------------------------------|
     * | [Informational] | Neutral announcements, tips, new features            |
     * | [Success]       | Payment recorded, property saved, action completed   |
     * | [Warning]       | Late payment notice, quota warnings                  |
     * | [Destructive]   | Delete property, archive, revoke access, sign out    |
     */
    enum class Variant { Informational, Success, Warning, Destructive }

    /**
     * Layout mode.
     *
     * | Layout       | Icon position     | Text alignment | Button alignment |
     * |--------------|-------------------|----------------|------------------|
     * | [Center]     | Centred above title | Centre       | Full-width stack |
     * | [Horizontal] | Beside title      | Start          | End-aligned row  |
     */
    enum class Layout { Center, Horizontal }

    /** Internal colour bundle resolved from [Variant] + MaterialTheme. */
    data class VariantColors(
        val iconBg: Color,
        val iconTint: Color,
        val buttonColor: Color,
    )

    /**
     * Resolves [VariantColors] for the given [variant] against the current
     * MaterialTheme. Must be called inside a composable.
     */
    @Composable
    fun variantColors(variant: Variant): VariantColors = when (variant) {
        Variant.Informational -> VariantColors(
            iconBg      = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            iconTint    = MaterialTheme.colorScheme.primary,
            buttonColor = MaterialTheme.colorScheme.primary,
        )
        Variant.Success -> VariantColors(
            iconBg      = Color(0xFFDCFCE7),
            iconTint    = Color(0xFF166534),
            buttonColor = MaterialTheme.colorScheme.primary,
        )
        Variant.Warning -> VariantColors(
            iconBg      = Color(0xFFFFEDD5),
            iconTint    = Color(0xFF9A3412),
            buttonColor = MaterialTheme.colorScheme.primary,
        )
        Variant.Destructive -> VariantColors(
            iconBg      = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
            iconTint    = MaterialTheme.colorScheme.error,
            buttonColor = MaterialTheme.colorScheme.error,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AppConfirmDialog — backward-compat thin wrapper over AppDialog
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Convenience wrapper that maps the old [AppConfirmDialog.Variant] to [AppDialog].
 * Prefer calling [AppDialog] directly for new code.
 */
@Composable
fun AppConfirmDialog(
    icon: ImageVector,
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Cancel",
    variant: AppConfirmDialog.Variant = AppConfirmDialog.Variant.Danger,
) {
    AppDialog(
        icon        = icon,
        title       = title,
        body        = body,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm   = onConfirm,
        onDismiss   = onDismiss,
        variant     = when (variant) {
            AppConfirmDialog.Variant.Danger  -> AppDialog.Variant.Destructive
            AppConfirmDialog.Variant.Neutral -> AppDialog.Variant.Informational
        },
        layout = AppDialog.Layout.Center
    )
}

/**
 * Legacy namespace — kept for backward compatibility with existing call sites.
 */
object AppConfirmDialog {
    enum class Variant { Danger, Neutral }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews — one per variant/layout combination
// ─────────────────────────────────────────────────────────────────────────────


@androidx.compose.ui.tooling.preview.Preview(
    name = "AppDialog — Informational (single button)",
    showBackground = true,
    backgroundColor = 0xFF464555,
    widthDp = 360,
)
@Composable
private fun PreviewInformational() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AppDialogContent(
                variant     = AppDialog.Variant.Informational,
                layout      = AppDialog.Layout.Center,
                icon        = Icons.Filled.Lightbulb,
                title       = "New Feature Available",
                body        = "You can now automate rent reminders for all tenants. Set it up once and let the system handle the rest.",
                confirmText = "Learn More",
                dismissText = null,
                onConfirm   = {},
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "AppDialog — Success (custom body slot)",
    showBackground = true,
    backgroundColor = 0xFF464555,
    widthDp = 360,
)
@Composable
private fun PreviewSuccess() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AppDialogContent(
                variant     = AppDialog.Variant.Success,
                layout      = AppDialog.Layout.Center,
                icon        = Icons.Filled.CheckCircle,
                title       = "Payment Recorded",
                confirmText = "Done",
                dismissText = null,
                onConfirm   = {},
                bodyContent = {
                    // Payment summary card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border   = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentSummaryRow(label = "Amount",   value = "NPR 14,500")
                            PaymentSummaryRow(label = "Property", value = "Unit 4B, The Grand")
                            PaymentSummaryRow(label = "Date",     value = "Aug 02, 2026")
                        }
                    }
                }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "AppDialog — Warning (horizontal layout)",
    showBackground = true,
    backgroundColor = 0xFF464555,
    widthDp = 360,
)
@Composable
private fun PreviewWarning() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AppDialogContent(
                variant     = AppDialog.Variant.Warning,
                layout      = AppDialog.Layout.Horizontal,
                icon        = Icons.Filled.Warning,
                title       = "Late Payment Notice",
                body        = "Tenant at 124 Maple St is currently 3 days past the grace period. Late fees will automatically apply tomorrow.",
                confirmText = "Send Reminder",
                dismissText = "Dismiss",
                onConfirm   = {},
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "AppDialog — Destructive (stacked buttons)",
    showBackground = true,
    backgroundColor = 0xFF464555,
    widthDp = 360,
)
@Composable
private fun PreviewDestructive() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AppDialogContent(
                variant     = AppDialog.Variant.Destructive,
                layout      = AppDialog.Layout.Center,
                icon        = Icons.Filled.Delete,
                title       = "Archive Property?",
                body        = "Are you sure you want to archive Sunset Villas? This will remove it from active views and reports. Historical data will be retained.",
                confirmText = "Archive Property",
                dismissText = "Cancel",
                onConfirm   = {},
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "AppDialog — Destructive (sign out)",
    showBackground = true,
    backgroundColor = 0xFF464555,
    widthDp = 360,
)
@Composable
private fun PreviewSignOut() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AppDialogContent(
                variant     = AppDialog.Variant.Destructive,
                layout      = AppDialog.Layout.Center,
                icon        = Icons.AutoMirrored.Outlined.Logout,
                title       = "Log Out?",
                body        = "You will be returned to the login screen. Your local data remains safe.",
                confirmText = "Log Out",
                dismissText = "Cancel",
                onConfirm   = {},
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "AppConfirmDialog — backward compat (Danger)",
    showBackground = true,
    backgroundColor = 0xFF464555,
    widthDp = 360,
)
@Composable
private fun PreviewAppConfirmDialogLegacy() {
    com.gaatho.rent.core.designsystem.RentManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Uses AppDialogContent directly (same reason — Dialog{} = black preview)
            AppDialogContent(
                variant     = AppDialog.Variant.Destructive,
                layout      = AppDialog.Layout.Center,
                icon        = Icons.Filled.DeleteForever,
                title       = "Delete Account?",
                body        = "This permanently removes all your data. This action cannot be undone.",
                confirmText = "Delete Forever",
                dismissText = "Cancel",
                onConfirm   = {},
            )
        }
    }
}

// ─── Preview helper ────────────────────────────────────────────────────────────

@Composable
private fun PaymentSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}
