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
    val font = Font(resource = Res.font.inter_variable)
    return FontFamily(font)
}

/**
 * Emerald Prestige Productivity Typography Scale
 */
@Composable
fun rentManagerTypography(): Typography {
    val inter = interFontFamily()
    return remember(inter) {
        Typography(
            displayLarge = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold, // 600
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.02).em
            ),
            displayMedium = TextStyle( // Used for display-lg-mobile
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold, // 600
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.02).em
            ),
            headlineMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold, // 600
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.01).em
            ),
            titleSmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Medium, // 500
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.em
            ),
            bodyMedium = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal, // 400
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.em
            ),
            bodySmall = TextStyle(
                fontFamily = inter,
                fontWeight = FontWeight.Normal, // 400
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.em
            ),
            labelSmall = TextStyle( // Used for label-caps
                fontFamily = inter,
                fontWeight = FontWeight.SemiBold, // 600
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.05.em
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
