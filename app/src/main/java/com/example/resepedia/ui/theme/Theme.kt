package com.example.resepedia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = PrimaryOrangeDark,
    secondary = AccentGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = AccentGreen,
    background = CreamBackground,
    onBackground = TextPrimaryLight,
    surface = WarmSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF7F5F0),
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrangeLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF5D2800),
    onPrimaryContainer = Color(0xFFFFCC80),
    secondary = AccentGreenLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFA5D6A7),
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF252525),
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark
)

@Composable
fun ResepediaTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    // Sesuai aturan: TANPA dynamic color agar desain konsisten di semua perangkat
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
