package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val JulianDarkColorScheme = darkColorScheme(
    primary = ImperialGold,
    onPrimary = ObsidianVoid,
    primaryContainer = ImperialGoldContainer,
    onPrimaryContainer = ImperialGoldBright,
    secondary = QuantumCyan,
    onSecondary = ObsidianVoid,
    secondaryContainer = QuantumCyanContainer,
    onSecondaryContainer = QuantumCyan,
    tertiary = EmeraldTelemetry,
    onTertiary = ObsidianVoid,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldTelemetry,
    error = CrimsonAlert,
    onError = TextPlatinum,
    errorContainer = CrimsonContainer,
    onErrorContainer = CrimsonAlert,
    background = ObsidianVoid,
    onBackground = TextPlatinum,
    surface = CarbonGlass,
    onSurface = TextPlatinum,
    surfaceVariant = ModuleSurface,
    onSurfaceVariant = TextMutedSilver,
    outline = ElevatedBorder
)

private val JulianLightColorScheme = lightColorScheme(
    primary = LightGoldPrimary,
    onPrimary = LightSurface,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = LightGoldPrimary,
    secondary = LightCyanSecondary,
    onSecondary = LightSurface,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = LightCyanSecondary,
    tertiary = Color(0xFF059669),
    onTertiary = LightSurface,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    error = CrimsonAlert,
    onError = LightSurface,
    background = LightVoid,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightModuleSurface,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFCBD5E1)
)

val JulianShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JulianDarkColorScheme else JulianLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = JulianShapes,
        content = content
    )
}
