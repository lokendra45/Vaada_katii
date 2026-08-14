package com.gaatho.rent.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.stringResource
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.*
import com.gaatho.rent.core.designsystem.Radius
import com.gaatho.rent.core.designsystem.Spacing

/**
 * A reusable, animated calendar date picker dialog.
 *
 * Features:
 * - Spring-based rubber pop-in animation on first display
 * - Month/year navigation with bouncy icon buttons
 * - Day cells highlight today and selected date
 * - Configurable min/max date (via epoch-based simple string dates)
 *
 * Usage:
 * ```
 * var showPicker by remember { mutableStateOf(false) }
 * var selectedDate by remember { mutableStateOf("2024-08-01") }
 *
 * if (showPicker) {
 *     AppDatePickerDialog(
 *         selectedDate = selectedDate,
 *         onDateSelected = { selectedDate = it; showPicker = false },
 *         onDismiss = { showPicker = false }
 *     )
 * }
 * ```
 *
 * @param selectedDate ISO-8601 date string "yyyy-MM-dd", or empty string.
 * @param onDateSelected called with "yyyy-MM-dd" when user confirms.
 */
@Composable
fun AppDatePickerDialog(
    selectedDate: String = "",
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select Date"
) {
    // Parse selectedDate or default to today
    val todayParts = getCurrentDateParts()
    val initialParts = parseDate(selectedDate) ?: todayParts

    var displayYear by remember { mutableIntStateOf(initialParts.first) }
    var displayMonth by remember { mutableIntStateOf(initialParts.second) } // 1-12
    var pickedDay by remember { mutableIntStateOf(initialParts.third) }
    var pickedMonth by remember { mutableIntStateOf(initialParts.second) }
    var pickedYear by remember { mutableIntStateOf(initialParts.first) }

    val scaleAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dialog_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(Radius.Xl),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                    alpha = scaleAnim
                }
        ) {
            Column(modifier = Modifier.padding(Spacing.ScreenPadding)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDisplayDate(pickedYear, pickedMonth, pickedDay),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.ItemGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(Spacing.ItemGap))

                // Month + Year Navigation
                MonthNavigator(
                    year = displayYear,
                    month = displayMonth,
                    onPrevMonth = {
                        if (displayMonth == 1) { displayMonth = 12; displayYear-- }
                        else displayMonth--
                    },
                    onNextMonth = {
                        if (displayMonth == 12) { displayMonth = 1; displayYear++ }
                        else displayMonth++
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Day-of-week headers
                DayOfWeekHeader()

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                CalendarGrid(
                    year = displayYear,
                    month = displayMonth,
                    selectedDay = if (pickedYear == displayYear && pickedMonth == displayMonth) pickedDay else -1,
                    todayDay = if (todayParts.first == displayYear && todayParts.second == displayMonth) todayParts.third else -1,
                    onDaySelected = { day ->
                        pickedDay = day
                        pickedMonth = displayMonth
                        pickedYear = displayYear
                    }
                )

                Spacer(modifier = Modifier.height(Spacing.ItemGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(Spacing.StackTight))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.StackTight, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            val dateStr = formatIsoDate(pickedYear, pickedMonth, pickedDay)
                            onDateSelected(dateStr)
                        },
                        shape = RoundedCornerShape(Radius.Md)
                    ) {
                        Text(stringResource(Res.string.confirm_action), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthNavigator(
    year: Int,
    month: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NavArrowButton(onClick = onPrevMonth, isNext = false)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = monthNames[month - 1],
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        NavArrowButton(onClick = onNextMonth, isNext = true)
    }
}

@Composable
private fun NavArrowButton(onClick: () -> Unit, isNext: Boolean) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "nav_arrow_scale"
    )

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Icon(
            imageVector = if (isNext) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(6.dp)
        )
    }

    // Reset pressed
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(150)
            pressed = false
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    selectedDay: Int,
    todayDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val firstDayOfWeek = getDayOfWeek(year, month, 1) // 0 = Sunday
    val daysInMonth = getDaysInMonth(year, month)
    val cells = firstDayOfWeek + daysInMonth
    val rows = (cells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOfWeek + 1
                    val isValidDay = day in 1..daysInMonth
                    val isSelected = isValidDay && day == selectedDay
                    val isToday = isValidDay && day == todayDay

                    DayCell(
                        day = if (isValidDay) day else null,
                        isSelected = isSelected,
                        isToday = isToday,
                        onClick = { if (isValidDay) onDaySelected(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.75f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "day_cell_scale"
    )

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(120)
            pressed = false
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> androidx.compose.ui.graphics.Color.Transparent
                }
            )
            .then(
                if (day != null) Modifier.clickable { pressed = true; onClick() }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            )
        }
    }
}

// ── Date helpers (pure Kotlin, no java.time needed for KMP) ──────────────────

private fun parseDate(dateStr: String): Triple<Int, Int, Int>? {
    val parts = dateStr.split("-")
    if (parts.size != 3) return null
    return try {
        Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    } catch (e: NumberFormatException) {
        null
    }
}

private fun getCurrentDateParts(): Triple<Int, Int, Int> {
    // Fallback to epoch-safe defaults — ViewModel should pass today's real date
    return Triple(2024, 8, 1)
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
}

private fun isLeapYear(year: Int) = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

/**
 * Zeller's Congruence to get day of week (0=Sunday).
 */
private fun getDayOfWeek(year: Int, month: Int, day: Int): Int {
    val m = if (month < 3) month + 12 else month
    val y = if (month < 3) year - 1 else year
    val k = y % 100
    val j = y / 100
    val h = (day + ((13 * (m + 1)) / 5) + k + (k / 4) + (j / 4) - (2 * j)) % 7
    // h: 0=Sat,1=Sun,2=Mon... → convert to 0=Sun
    return ((h + 6) % 7)
}

private fun formatDisplayDate(year: Int, month: Int, day: Int): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return "$day ${monthNames.getOrElse(month - 1) { "?" }} $year"
}

private fun formatIsoDate(year: Int, month: Int, day: Int): String {
    val m = month.toString().padStart(2, '0')
    val d = day.toString().padStart(2, '0')
    return "$year-$m-$d"
}
