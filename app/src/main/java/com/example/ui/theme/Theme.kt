package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TrDialDarkColorScheme = darkColorScheme(
    primary = TrBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1D3B8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = TrCyan,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF0E4A5C),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = TrGreen,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = TrBackground,
    onBackground = TrTextPrimary,
    surface = TrSurface,
    onSurface = TrTextPrimary,
    surfaceVariant = TrCard,
    onSurfaceVariant = TrTextSecondary,
    surfaceContainerLowest = TrBackground,
    surfaceContainerLow = TrSurface,
    surfaceContainer = TrCard,
    surfaceContainerHigh = TrCardElevated,
    surfaceContainerHighest = Color(0xFF26344F),
    outline = TrBorder,
    outlineVariant = Color(0xFF1E293B),
    error = TrRed,
    onError = Color.White,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFEE2E2)
)

val TrDialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TrDialSipTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TrDialDarkColorScheme,
        typography = TrDialTypography,
        shapes = TrDialShapes,
        content = content
    )
}
