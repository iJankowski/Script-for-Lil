package com.scriptforlil.kindroidhealthsync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Paper,
    primaryContainer = SeaGlass,
    onPrimaryContainer = DeepInk,
    secondary = Teal,
    onSecondary = Paper,
    secondaryContainer = Cloud,
    onSecondaryContainer = DeepInk,
    tertiary = CoolSlate,
    onTertiary = Paper,
    tertiaryContainer = PaleAqua,
    onTertiaryContainer = DeepInk,
    background = Paper,
    onBackground = DeepInk,
    surface = Paper,
    onSurface = DeepInk,
    surfaceVariant = Cloud,
    onSurfaceVariant = CoolSlate,
    outline = PaleAqua,
)

private val DarkColors = darkColorScheme(
    primary = Sand,
    onPrimary = Night,
    primaryContainer = Forest,
    onPrimaryContainer = Linen,
    secondary = Clay,
    onSecondary = Linen,
    secondaryContainer = Pine,
    onSecondaryContainer = Linen,
    tertiary = Foam,
    onTertiary = Night,
    tertiaryContainer = Teal,
    onTertiaryContainer = Linen,
    background = Night,
    onBackground = Linen,
    surface = Night,
    onSurface = Linen,
    surfaceVariant = Pine,
    onSurfaceVariant = Mist,
    outline = Slate,
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    )
)

@Composable
fun KindroidHealthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
