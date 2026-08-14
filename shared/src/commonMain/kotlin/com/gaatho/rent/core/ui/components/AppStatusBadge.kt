package com.gaatho.rent.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable pill-shaped status badge used across screens (Paid / Overdue / Vacant,
 * payment method chips, etc.). Colors and typography are passed by the caller so
 * each screen keeps its own palette while sharing the same visual structure.
 */
@Composable
fun AppStatusBadge(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    shape: Shape = CircleShape,
    fontSize: TextUnit = 9.5.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    letterSpacing: TextUnit = 0.sp,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 3.dp,
) {
    Box(
        modifier = modifier
            .background(containerColor, shape)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = fontWeight,
                fontSize = fontSize,
                letterSpacing = letterSpacing,
                color = contentColor
            ),
            maxLines = 1
        )
    }
}
