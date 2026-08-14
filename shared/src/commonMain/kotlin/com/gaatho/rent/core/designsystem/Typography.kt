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
import rentmanagerapp.shared.generated.resources.satoshi_variable

@Composable
fun satoshiFontFamily(): FontFamily {
    return FontFamily(
        Font(resource = Res.font.satoshi_variable, weight = FontWeight.Normal),
        Font(resource = Res.font.satoshi_variable, weight = FontWeight.Medium),
        Font(resource = Res.font.satoshi_variable, weight = FontWeight.SemiBold),
        Font(resource = Res.font.satoshi_variable, weight = FontWeight.Bold),
        Font(resource = Res.font.satoshi_variable, weight = FontWeight.ExtraBold)
    )
}

@Composable
fun rentManagerTypography(): Typography {
    val satoshi = satoshiFontFamily()
    return remember(satoshi) {
        Typography(
            displayMedium = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
            ),
            
            // Screen titles and compact section headers.
            headlineMedium = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            
            ),
            
            // Sub-Screen Titles
            titleLarge = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            
            ),

            titleMedium = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
            
            ),
            
            // Body Large
            bodyLarge = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
             
            ),
            
            // List Item Titles (from Figma "General": 12px Medium)
            bodyMedium = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
              
            ),
            
            // Small Body / Descriptions (from Figma: 10.29px Regular)
            bodySmall = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
               
            ),
            
            // Tabs / Highlight Labels (from Figma "Category": 12px SemiBold)
            labelLarge = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            
            ),
            
            // Small Metadata
            labelMedium = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.Medium,
                fontSize = 9.5.sp,
            
            ),
            
            // Tiny Labels
            labelSmall = TextStyle(
                fontFamily = satoshi,
                fontWeight = FontWeight.Normal,
                fontSize = 8.sp,
        
            )
        )
    }
}

/**
 * Tabular-numeral style for financial data.
 * Uses Satoshi ExtraBold ~21px.
 */
@Composable
fun monoDataTextStyle(): TextStyle = TextStyle(
    fontFamily = satoshiFontFamily(),
    fontWeight = FontWeight.ExtraBold,
    fontSize = 20.sp
)
