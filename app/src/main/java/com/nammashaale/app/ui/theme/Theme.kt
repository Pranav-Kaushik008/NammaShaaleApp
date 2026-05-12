package com.nammashaale.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand Colors ──────────────────────────────────────────────────────────────
val PrimaryPurple   = Color(0xFF6C63FF)
val PrimaryDark     = Color(0xFF4A42CC)
val SurfaceDark     = Color(0xFF1A1A2E)
val BackgroundDark  = Color(0xFF0F0F23)
val CardDark        = Color(0xFF1E1E3A)

val GreenWorking    = Color(0xFF4CAF50)
val AmberCheck      = Color(0xFFFFA726)
val RedRepair       = Color(0xFFE53935)

private val DarkColors = darkColorScheme(
    primary           = PrimaryPurple,
    onPrimary         = Color.White,
    primaryContainer  = CardDark,
    onPrimaryContainer= Color(0xFFD0D0FF),
    secondary         = Color(0xFF9C8FFF),
    onSecondary       = Color.White,
    background        = BackgroundDark,
    onBackground      = Color(0xFFE0E0FF),
    surface           = SurfaceDark,
    onSurface         = Color(0xFFD0D0F0),
    surfaceVariant    = CardDark,
    onSurfaceVariant  = Color(0xFF9090C0),
    outline           = Color(0xFF5050A0),
    error             = RedRepair,
    onError           = Color.White
)

private val LightColors = lightColorScheme(
    primary           = PrimaryPurple,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFEEEDFF),
    onPrimaryContainer= Color(0xFF1A0066),
    secondary         = Color(0xFF625B71),
    background        = Color(0xFFF8F7FF),
    onBackground      = Color(0xFF1A1A2E),
    surface           = Color.White,
    onSurface         = Color(0xFF1A1A2E),
    error             = RedRepair
)

@Composable
fun NammaShaaleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography(),
        content     = content
    )
}
