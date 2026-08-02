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
            displayLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                letterSpacing = (-0.25).sp
            ),
            displayMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 34.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp
            ),
            displaySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp
            ),
            headlineLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp
            ),
            titleMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.15.sp
            ),
            titleSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.1.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.25.sp
            ),
            bodySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            ),
            labelLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.1.sp
            ),
            labelMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            ),
            labelSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.5.sp
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
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.02).em
)
