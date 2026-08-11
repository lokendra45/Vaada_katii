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
 * Premium Typography — Inter Variable
 *
 * Modified for extreme hierarchy. Hero numbers are massive (40sp+), 
 * while secondary labels remain small and soft.
 *
 * Token          | Slot           | Size  | Weight    | Line  | Tracking
 * ─────────────────────────────────────────────────────────────────────
 * hero-number    | displayLarge   | 40sp  | Bold      | 48sp  | -0.02em
 * screen-title   | displayMedium  | 32sp  | SemiBold  | 40sp  | -0.02em
 * section-header | headlineLarge  | 24sp  | SemiBold  | 32sp  | -0.01em
 * row-name       | titleMedium    | 14sp  | Medium    | 20sp  | 0
 * body           | bodyMedium     | 14sp  | Normal    | 20sp  | 0
 * caption        | bodySmall      | 12sp  | Normal    | 16sp  | 0
 * micro          | labelSmall     | 11sp  | Medium    | 14sp  | 0
 */
@Composable
fun rentManagerTypography(): Typography {
    val inter = interFontFamily()
    return remember(inter) {
        Typography(
            displayLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                letterSpacing = (-0.02).em
            ),
            displayMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.02).em
            ),
            displaySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.01).em
            ),
            headlineLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.01).em
            ),
            headlineMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            titleMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            ),
            titleSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            ),
            bodySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.sp
            ),
            labelLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            ),
            labelMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.sp
            ),
            labelSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.sp
            )
        )
    }
}

/**
 * Tabular-numeral style for financial data.
 * Use for Rs./$ amounts and large metric numbers only —
 * not for every number on screen, just the hero values.
 */
@Composable
fun monoDataTextStyle(): TextStyle = TextStyle(
    fontFamily = interFontFamily(),
    fontWeight = FontWeight.Bold,
    fontSize = 40.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.03).em
)