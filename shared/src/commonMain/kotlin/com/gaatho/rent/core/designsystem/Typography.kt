package com.gaatho.rent.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.figtree_variable

@Composable
fun figtreeFontFamily(): FontFamily {
    return FontFamily(
        Font(resource = Res.font.figtree_variable, weight = FontWeight.Normal),
        Font(resource = Res.font.figtree_variable, weight = FontWeight.Medium),
        Font(resource = Res.font.figtree_variable, weight = FontWeight.SemiBold),
        Font(resource = Res.font.figtree_variable, weight = FontWeight.Bold),
        Font(resource = Res.font.figtree_variable, weight = FontWeight.ExtraBold)
    )
}

@Composable
fun rentManagerTypography(): Typography {
    val figtree = figtreeFontFamily()
    return remember(figtree) {
        Typography(
            // Hero financial figure only (balances, amounts). Used sparingly.
            displayLarge = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
            ),
            // Screen titles — just a touch above the original 18sp.
            displayMedium = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            ),
            displaySmall = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
            ),

            // Section headers
            headlineLarge = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 22.sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 21.sp,
            ),

            // Card / list-item titles
            titleLarge = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 21.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            titleSmall = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),

            // Body
            bodyLarge = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),

            // Labels / captions
            labelLarge = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = figtree,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            ),
        )
    }
}

/**
 * Tabular-numeral style for financial data (amounts, balances).
 */
@Composable
fun monoDataTextStyle(): TextStyle = TextStyle(
    fontFamily = figtreeFontFamily(),
    fontWeight = FontWeight.ExtraBold,
    fontSize = 28.sp,
    lineHeight = 36.sp
)
