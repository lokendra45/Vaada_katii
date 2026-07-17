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
 * Emerald Prestige Productivity Typography Scale
 * Follows Google Design Polished Look Theory with smaller text ratio & rounder, semi-bold weights.
 */
@Composable
fun rentManagerTypography(): Typography {
    val inter = interFontFamily()
    return remember(inter) {
        Typography(
            displayLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold, // 700
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.02).em
            ),
            displayMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold, // 700
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.02).em
            ),
            displaySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold, // 600
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.015).em
            ),
            headlineLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold, // 700
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.02).em
            ),
            headlineMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold, // 700
                fontSize = 20.sp,
                lineHeight = 26.sp,
                letterSpacing = (-0.015).em
            ),
            headlineSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold, // 600
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.01).em
            ),
            titleLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp, // Prominent App Bar headers
                lineHeight = 28.sp,
                letterSpacing = (-0.01).em
            ),
            titleMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp, // Standard Card/Section titles
                lineHeight = 22.sp,
                letterSpacing = 0.em
            ),
            titleSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.em
            ),
            bodyLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.em
            ),
            bodyMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp, // Primary Input (text-sm)
                lineHeight = 20.sp,
                letterSpacing = 0.em
            ),
            bodySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.em
            ),
            labelLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.01.em
            ),
            labelMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp, // Labels (text-xs)
                lineHeight = 16.sp,
                letterSpacing = 0.01.em
            ),
            labelSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.02.em
            )
        )
    }
}

/**
 * Accessor for the Mono Data style
 */
@Composable
fun monoDataTextStyle(): TextStyle {
    return TextStyle(
        fontFamily = interFontFamily(), // In a real app, you'd add fontFeatureSettings = "tnum"
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}
