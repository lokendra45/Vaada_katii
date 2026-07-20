package com.gaatho.rent.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.inter_variable

@Composable
fun interFontFamily(): FontFamily {
    return FontFamily(
        Font(resource = Res.font.inter_variable, weight = FontWeight.Normal),
        Font(resource = Res.font.inter_variable, weight = FontWeight.Medium),
        Font(resource = Res.font.inter_variable, weight = FontWeight.SemiBold),
        Font(resource = Res.font.inter_variable, weight = FontWeight.Bold)
    )
}

/**
 * Quiet Premium Typography — Inter Variable
 * Exact mapping from design-tokens.yaml → Material 3 slots.
 *
 * Token Name     | Slot           | Size  | Weight | Line  | Tracking
 * ───────────────────────────────────────────────────────────────────
 * display-lg     | displayLarge   | 32sp  | 700    | 40sp  | -0.02em
 * screen-title   | headlineLarge  | 28sp  | 600    | 36sp  | -0.01em
 * section-header | headlineMedium | 20sp  | 600    | 28sp  | 0
 * title          | titleLarge     | 18sp  | 500    | 24sp  | 0
 * body           | bodyLarge      | 16sp  | 400    | 24sp  | 0
 * caption        | bodyMedium     | 14sp  | 400    | 20sp  | 0
 * metadata       | labelSmall     | 12sp  | 500    | 16sp  | +0.01em
 *
 * Remaining M3 slots filled to maintain hierarchy consistency.
 */
@Composable
fun rentManagerTypography(): Typography {
    val inter = interFontFamily()
    return remember(inter) {
        Typography(
            // ── display-lg → 32/700 ──────────────────────────────────────
            displayLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.02).em
            ),
            // ── Monetary amounts, hero numbers ────────────────────────────
            displayMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.01).em
            ),
            displaySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.01).em
            ),
            // ── screen-title → 28/600 ─────────────────────────────────────
            headlineLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.01).em
            ),
            // ── section-header → 20/600 ───────────────────────────────────
            headlineMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.em
            ),
            // ── Sub-section header (fills M3 slot) ────────────────────────
            headlineSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                letterSpacing = 0.em
            ),
            // ── title → 18/500 ────────────────────────────────────────────
            titleLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.em
            ),
            // ── Prominent labels (fills M3 slot) ──────────────────────────
            titleMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.em
            ),
            // ── Small titles (fills M3 slot) ──────────────────────────────
            titleSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.em
            ),
            // ── body → 16/400 ─────────────────────────────────────────────
            bodyLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.em
            ),
            // ── caption → 14/400 ──────────────────────────────────────────
            bodyMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.em
            ),
            // ── Smallest body (fills M3 slot) ─────────────────────────────
            bodySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.em
            ),
            // ── Button / tag label ────────────────────────────────────────
            labelLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.005.em
            ),
            // ── Field labels ──────────────────────────────────────────────
            labelMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.01.em
            ),
            // ── metadata → 12/500 ─────────────────────────────────────────
            labelSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.01.em
            )
        )
    }
}

/**
 * Tabular-numeral style for financial data.
 * Use for Rs./$ amounts and large metric numbers.
 */
@Composable
fun monoDataTextStyle(): TextStyle = TextStyle(
    fontFamily = interFontFamily(),
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.01).em
)
