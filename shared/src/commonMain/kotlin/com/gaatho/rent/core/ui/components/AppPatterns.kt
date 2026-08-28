package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable, standardized UI patterns.
 *
 * These exist to remove the duplicated "section header", "label → value row", and
 * "amount + caption" layouts that were re-implemented in Dashboard / Property / Tenant /
 * Payment / Settings screens. Using these keeps every screen on the same typography and
 * spacing rules (see [AppText]).
 */

/**
 * Section header used at the top of a group of content.
 * Replaces the duplicated DashboardSectionHeader / SettingsGroup / PaymentListScreen.SectionHeader.
 */
@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    SectionTitle(
        text = title,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

/**
 * A "label → value" row. Used in detail screens and info sheets.
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CaptionText(text = label, modifier = Modifier.weight(1f))
        BodyText(text = value, color = valueColor, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

/**
 * A financial figure with an optional caption underneath.
 * Used in Dashboard / Tenant / Payment rows so money always reads identically.
 */
@Composable
fun AmountRow(
    amount: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    amountColor: Color = Color.Unspecified
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.End
    ) {
        AmountText(text = amount, color = amountColor)
        if (subtitle != null) {
            CaptionText(text = subtitle)
        }
    }
}
