package com.savings.buddy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.savings.buddy.data.THEME_DARK
import com.savings.buddy.data.THEME_LIGHT
import com.savings.buddy.data.THEME_SYSTEM

private val LightColors = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00352E),
    secondary = Color(0xFF8D6E00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE082),
    onSecondaryContainer = Color(0xFF2E2000),
    tertiary = Color(0xFFB3591F),
    surface = Color(0xFFFCF8F4),
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFEFEAE4),
    onSurfaceVariant = Color(0xFF4D4639),
    background = Color(0xFFFCF8F4),
    onBackground = Color(0xFF1C1B1A),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5CDAC6),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF77F8E2),
    secondary = Color(0xFFE6C24C),
    onSecondary = Color(0xFF3D2E00),
    secondaryContainer = Color(0xFF584400),
    onSecondaryContainer = Color(0xFFFFE08D),
    tertiary = Color(0xFFFFB68F),
    surface = Color(0xFF1C1B1A),
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF4D4639),
    onSurfaceVariant = Color(0xFFCFC5B9),
    background = Color(0xFF141312),
    onBackground = Color(0xFFEDE0D4),
    error = Color(0xFFFFB4AB),
)

/** themeMode: 0 跟随系统 / 1 浅色 / 2 深色 */
@Composable
fun SavingTheme(themeMode: Int, content: @Composable () -> Unit) {
    val useDark = when (themeMode) {
        THEME_LIGHT -> false
        THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        content = content,
    )
}
