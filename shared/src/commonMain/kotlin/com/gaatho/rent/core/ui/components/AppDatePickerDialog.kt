package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaatho.rent.core.utils.DateTimeUtil
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.cancel
import rentmanagerapp.shared.generated.resources.common_ok
import kotlin.time.Clock
import kotlin.time.Instant as KtInstant

/**
 * Clickable date field that opens [AppDatePickerDialog] via [onClick].
 *
 * Replaces the old [AppTextField]-based trigger, which silently swallowed the click:
 * the inner [androidx.compose.foundation.text.BasicTextField] consumed the pointer event
 * before the wrapping Surface's clickable could fire, so the picker never opened. Here the
 * whole surface is the click target, so the dialog reliably opens.
 */
@Composable
fun AppDateField(
    value: String,
    onClick: () -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    labelStyle: TextStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            onClick = onClick,
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = if (isError) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BodyText(
                    text = if (value.isBlank()) placeholder else DateTimeUtil.formatDisplayDate(value),
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (errorMessage != null) {
            BodySmallText(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select date"
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toEpochMillisOrToday())
    com.gaatho.rent.core.ui.components.AppDialog(
        icon = Icons.Default.CalendarToday,
        title = title,
        confirmText = stringResource(Res.string.common_ok),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = {
            val millis = state.selectedDateMillis
            if (millis != null) {
                onDateSelected(millis.toIsoDateString())
            }
            onDismiss()
        },
        onDismiss = onDismiss,
        bodyContent = {
            DatePicker(
                state = state,
                title = null,
                showModeToggle = false,
                colors = androidx.compose.material3.DatePickerDefaults.colors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    )
}

private fun localDateToMillis(date: LocalDate): Long =
    date.toEpochDays().toLong() * 86_400_000L

private fun String.toEpochMillisOrToday(): Long = try {
    val date = LocalDate.parse(this.substring(0, 10))
    localDateToMillis(date)
} catch (_: Exception) {
    todayMillis()
}

private fun todayMillis(): Long {
    val date = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    return localDateToMillis(date)
}

private fun Long.toIsoDateString(): String {
    val date = KtInstant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
    val m = date.month.number.toString().padStart(2, '0')
    val d = date.day.toString().padStart(2, '0')
    return "${date.year}-$m-$d"
}
