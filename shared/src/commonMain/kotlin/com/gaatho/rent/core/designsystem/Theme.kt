package com.gaatho.rent.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Light Color Scheme
// ─────────────────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary                 = LightPrimary,
    onPrimary               = LightOnPrimary,
    primaryContainer        = LightPrimaryContainer,
    onPrimaryContainer      = LightOnPrimaryContainer,
    inversePrimary          = LightInversePrimary,
    secondary               = LightSecondary,
    onSecondary             = LightOnSecondary,
    secondaryContainer      = LightSecondaryContainer,
    onSecondaryContainer    = LightOnSecondaryContainer,
    tertiary                = LightTertiary,
    onTertiary              = LightOnTertiary,
    tertiaryContainer       = LightTertiaryContainer,
    onTertiaryContainer     = LightOnTertiaryContainer,
    error                   = LightError,
    onError                 = LightOnError,
    errorContainer          = LightErrorContainer,
    onErrorContainer        = LightOnErrorContainer,
    background              = LightBackground,
    onBackground            = LightOnBackground,
    surface                 = LightSurface,
    onSurface               = LightOnSurface,
    surfaceVariant          = LightSurfaceVariant,
    onSurfaceVariant        = LightOnSurfaceVariant,
    inverseSurface          = LightInverseSurface,
    inverseOnSurface        = LightInverseOnSurface,
    outline                 = LightOutline,
    outlineVariant          = LightOutlineVariant,
    scrim                   = Color.Black,
    surfaceTint             = LightSurfaceTint,
    surfaceContainerLowest  = LightSurfaceContainerLowest,
    surfaceContainerLow     = LightSurfaceContainerLow,
    surfaceContainer        = LightSurfaceContainer,
    surfaceContainerHigh    = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
)

// ─────────────────────────────────────────────────────────────────────────────
// Dark Color Scheme
// ─────────────────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary                 = DarkPrimary,
    onPrimary               = DarkOnPrimary,
    primaryContainer        = DarkPrimaryContainer,
    onPrimaryContainer      = DarkOnPrimaryContainer,
    inversePrimary          = DarkInversePrimary,
    secondary               = DarkSecondary,
    onSecondary             = DarkOnSecondary,
    secondaryContainer      = DarkSecondaryContainer,
    onSecondaryContainer    = DarkOnSecondaryContainer,
    tertiary                = DarkTertiary,
    onTertiary              = DarkOnTertiary,
    tertiaryContainer       = DarkTertiaryContainer,
    onTertiaryContainer     = DarkOnTertiaryContainer,
    error                   = DarkError,
    onError                 = DarkOnError,
    errorContainer          = DarkErrorContainer,
    onErrorContainer        = DarkOnErrorContainer,
    background              = DarkBackground,
    onBackground            = DarkOnBackground,
    surface                 = DarkSurface,
    onSurface               = DarkOnSurface,
    surfaceVariant          = DarkSurfaceVariant,
    onSurfaceVariant        = DarkOnSurfaceVariant,
    inverseSurface          = DarkInverseSurface,
    inverseOnSurface        = DarkInverseOnSurface,
    outline                 = DarkOutline,
    outlineVariant          = DarkOutlineVariant,
    scrim                   = Color.Black,
    surfaceTint             = DarkSurfaceTint,
    surfaceContainerLowest  = DarkSurfaceContainerLowest,
    surfaceContainerLow     = DarkSurfaceContainerLow,
    surfaceContainer        = DarkSurfaceContainer,
    surfaceContainerHigh    = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)

// ─────────────────────────────────────────────────────────────────────────────
// Shapes
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun rentManagerShapes(): Shapes = Shapes(
    small  = RoundedCornerShape(Radius.Sm),
    medium = RoundedCornerShape(Radius.Md),
    large  = RoundedCornerShape(Radius.Lg)
)

// ─────────────────────────────────────────────────────────────────────────────
// Theme Entry Point
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RentManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = rentManagerTypography(),
        shapes      = rentManagerShapes(),
        content     = content
    )
}
