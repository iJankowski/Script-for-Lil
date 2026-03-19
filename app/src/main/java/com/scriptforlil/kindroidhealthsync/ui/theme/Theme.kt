package com.scriptforlil.kindroidhealthsync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Sea,
    onPrimary = Cloud,
    primaryContainer = Sand,
    onPrimaryContainer = Ink,
    secondary = Coral,
    onSecondary = Cloud,
    secondaryContainer = Mist,
    onSecondaryContainer = Ink,
    tertiary = Sea,
    onTertiary = Cloud,
    tertiaryContainer = Mist,
    onTertiaryContainer = Ink,
    background = Cloud,
    onBackground = Ink,
    surface = Cloud,
    onSurface = Ink
)

private val DarkColors = darkColorScheme(
    primary = Sand,
    onPrimary = Ink,
    primaryContainer = Sea,
    onPrimaryContainer = Cloud,
    secondary = Coral,
    onSecondary = Ink,
    secondaryContainer = Ink,
    onSecondaryContainer = Cloud,
    tertiary = Sand,
    onTertiary = Ink,
    tertiaryContainer = Sea,
    onTertiaryContainer = Cloud,
    background = Ink,
    onBackground = Cloud,
    surface = Ink,
    onSurface = Cloud
)

@Composable
fun KindroidHealthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
