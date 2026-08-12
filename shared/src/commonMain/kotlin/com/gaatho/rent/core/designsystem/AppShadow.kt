package com.gaatho.rent.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

/**
 * Ambient Shadows based on Figma Spec
 */
object AppShadow {
    /** 
     * Screen Cards (40px blur, 0 offset). 
     * Compose handles elevation differently, but we emulate it with a higher elevation and ambient color if needed.
     */
    fun Modifier.ambientShadow(
        elevation: androidx.compose.ui.unit.Dp = 16.dp, // High elevation for wide blur
        shape: androidx.compose.ui.graphics.Shape
    ) = this.shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = AppColors.ShadowAmbient,
        spotColor = AppColors.ShadowSpot
    )

    /** 
     * Inner Cards (12px blur, 0 offset)
     */
    fun Modifier.innerShadow(
        elevation: androidx.compose.ui.unit.Dp = 4.dp, // Lower elevation for tighter blur
        shape: androidx.compose.ui.graphics.Shape
    ) = this.shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = AppColors.ShadowAmbient,
        spotColor = AppColors.ShadowSpot
    )

    fun Modifier.figmaHeroShadow(
        shape: androidx.compose.ui.graphics.Shape
    ) = this.shadow(
        elevation = 18.dp,
        shape = shape,
        clip = false,
        ambientColor = AppColors.HeroGlow,
        spotColor = AppColors.ShadowSpot
    )

    fun Modifier.figmaCardShadow(
        shape: androidx.compose.ui.graphics.Shape
    ) = this.shadow(
        elevation = 9.dp,
        shape = shape,
        clip = false,
        ambientColor = AppColors.ShadowAmbient,
        spotColor = AppColors.ShadowSpot
    )

    fun Modifier.figmaButtonShadow(
        shape: androidx.compose.ui.graphics.Shape,
        prominent: Boolean = false
    ) = this.shadow(
        elevation = if (prominent) 14.dp else 8.dp,
        shape = shape,
        clip = false,
        ambientColor = AppColors.ShadowAmbient,
        spotColor = AppColors.ShadowSpot
    )
}
