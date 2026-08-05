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
 *
 * 6 real sizes, weight actually carries hierarchy (not just size).
 * Inter is tight and modern — no positive letter-spacing on body/label
 * text; that was leftover Material 2 tracking and reads as "broken kerning"
 * on this font. Negative tracking only on the two largest sizes.
 *
 * Token          | Slot           | Size  | Weight    | Line  | Tracking
 * ─────────────────────────────────────────────────────────────────────
 * display        | displayLarge   | 22sp  | SemiBold  | 28sp  | -0.01em
 * screen-title   | headlineLarge  | 20sp  | SemiBold  | 26sp  | -0.01em
 * section-header | headlineMedium | 15sp  | Medium    | 20sp  | 0
 * body-strong    | titleMedium    | 13sp  | Medium    | 18sp  | 0
 * body           | bodyMedium     | 13sp  | Normal    | 18sp  | 0
 * caption        | bodySmall      | 11sp  | Normal    | 16sp  | 0
 * micro          | labelSmall     | 9.5sp | Normal    | 12sp  | 0.1sp
 *
 * Rule: pick from this table only. If nothing fits, round to nearest —
 * do not introduce a new size. Weight 500/600 is reserved for the ~4
 * most important elements per screen (hero number, section headers,
 * the one value in a row that matters). Everything else is Normal.
 */
@Composable
fun rentManagerTypography(): Typography {
    val inter = interFontFamily()
    return remember(inter) {
        Typography(
            displayLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.01).em
            ),
            displayMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                letterSpacing = (-0.01).em
            ),
            displaySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp
            ),
            headlineLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                letterSpacing = (-0.01).em
            ),
            headlineMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            ),
            titleMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp
            ),
            titleSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp
            ),
            bodySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.sp
            ),
            labelLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp
            ),
            labelMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.05.sp
            ),
            labelSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                letterSpacing = 0.1.sp
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
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.02).em
)