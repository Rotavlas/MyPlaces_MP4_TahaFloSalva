package com.myplaces.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)
val Indigo100 = Color(0xFFE0E7FF)
val Emerald500 = Color(0xFF10B981)
val Emerald100 = Color(0xFFD1FAE5)
val Amber400  = Color(0xFFFBBF24)
val Coral     = Color(0xFFFF6B6B)
val Surface   = Color(0xFFF8FAFC)
val OnSurface = Color(0xFF1E293B)

private val AppColors = lightColorScheme(
    primary          = Indigo500,
    onPrimary        = Color.White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo600,
    secondary        = Emerald500,
    onSecondary      = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Color(0xFF065F46),
    tertiary         = Amber400,
    onTertiary       = Color(0xFF1C1917),
    error            = Coral,
    background       = Surface,
    onBackground     = OnSurface,
    surface          = Color.White,
    onSurface        = OnSurface,
    surfaceVariant   = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline          = Color(0xFFCBD5E1)
)

@Composable
fun MyPlacesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, content = content)
}
