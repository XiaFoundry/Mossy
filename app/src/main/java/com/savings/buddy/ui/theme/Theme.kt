package com.savings.buddy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.savings.buddy.data.PALETTE_LAVENDER
import com.savings.buddy.data.PALETTE_MINT
import com.savings.buddy.data.PALETTE_MONO
import com.savings.buddy.data.PALETTE_PINK
import com.savings.buddy.data.PALETTE_SYSTEM
import com.savings.buddy.data.PALETTE_TEAL
import com.savings.buddy.data.PALETTE_WATER
import com.savings.buddy.data.THEME_DARK
import com.savings.buddy.data.THEME_LIGHT

/** 一套主题色板：亮/暗两套方案 + 设置页预览色 */
data class PaletteSpec(
    val key: Int,
    val label: String,
    val light: ColorScheme,
    val dark: ColorScheme,
    val preview: List<Color>,
)

private fun tealLight() = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00352E),
    secondary = Color(0xFF8D6E00),
    secondaryContainer = Color(0xFFFFE082),
    tertiary = Color(0xFFB3591F),
    surface = Color(0xFFFCF8F4),
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFEFEAE4),
    onSurfaceVariant = Color(0xFF4D4639),
    background = Color(0xFFFCF8F4),
    onBackground = Color(0xFF1C1B1A),
    error = Color(0xFFBA1A1A),
)

private fun tealDark() = darkColorScheme(
    primary = Color(0xFF5CDAC6),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF77F8E2),
    secondary = Color(0xFFE6C24C),
    secondaryContainer = Color(0xFF584400),
    tertiary = Color(0xFFFFB68F),
    surface = Color(0xFF1C1B1A),
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF4D4639),
    onSurfaceVariant = Color(0xFFCFC5B9),
    background = Color(0xFF141312),
    onBackground = Color(0xFFEDE0D4),
    error = Color(0xFFFFB4AB),
)

object ThemePalettes {

    val TEAL = PaletteSpec(
        key = PALETTE_TEAL, label = "经典青绿",
        light = tealLight(), dark = tealDark(),
        preview = listOf(Color(0xFF00695C), Color(0xFFB2DFDB), Color(0xFFFFE082)),
    )
    val PINK = PaletteSpec(
        key = PALETTE_PINK, label = "粉色",
        light = lightColorScheme(
            primary = Color(0xFFBB3A66),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFD9E1),
            onPrimaryContainer = Color(0xFF3E001B),
            secondary = Color(0xFF8E4957),
            secondaryContainer = Color(0xFFFFDADF),
            surface = Color(0xFFFFF8F7),
            onSurface = Color(0xFF201A1B),
            surfaceVariant = Color(0xFFF2DDE1),
            onSurfaceVariant = Color(0xFF511D2A),
            background = Color(0xFFFFF8F7),
            onBackground = Color(0xFF201A1B),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB1C8),
            onPrimary = Color(0xFF5E1133),
            primaryContainer = Color(0xFF7B2949),
            onPrimaryContainer = Color(0xFFFFD9E1),
            secondary = Color(0xFFD9A7B3),
            secondaryContainer = Color(0xFF5C323E),
            surface = Color(0xFF1A0E11),
            onSurface = Color(0xFFEAE0E2),
            surfaceVariant = Color(0xFF511D2A),
            onSurfaceVariant = Color(0xFFDFC2C8),
            background = Color(0xFF1A0E11),
            onBackground = Color(0xFFEAE0E2),
        ),
        preview = listOf(Color(0xFFBB3A66), Color(0xFFFFD9E1), Color(0xFFFFDADF)),
    )
    val MINT = PaletteSpec(
        key = PALETTE_MINT, label = "薄荷绿",
        light = lightColorScheme(
            primary = Color(0xFF00875F),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFB9F2D9),
            onPrimaryContainer = Color(0xFF002517),
            secondary = Color(0xFF4C6358),
            secondaryContainer = Color(0xFFCEE9DB),
            surface = Color(0xFFF6FBF6),
            onSurface = Color(0xFF181D1A),
            surfaceVariant = Color(0xFFDBE5DD),
            onSurfaceVariant = Color(0xFF3F4942),
            background = Color(0xFFF6FBF6),
            onBackground = Color(0xFF181D1A),
        ),
        dark = darkColorScheme(
            primary = Color(0xFF7CDCAE),
            onPrimary = Color(0xFF003922),
            primaryContainer = Color(0xFF005232),
            onPrimaryContainer = Color(0xFF97F7C5),
            secondary = Color(0xFFB3CCBF),
            secondaryContainer = Color(0xFF354B41),
            surface = Color(0xFF0F1511),
            onSurface = Color(0xFFDEE4DE),
            surfaceVariant = Color(0xFF3F4942),
            onSurfaceVariant = Color(0xFFBFC9C0),
            background = Color(0xFF0F1511),
            onBackground = Color(0xFFDEE4DE),
        ),
        preview = listOf(Color(0xFF00875F), Color(0xFFB9F2D9), Color(0xFFCEE9DB)),
    )
    val WATER = PaletteSpec(
        key = PALETTE_WATER, label = "水蓝色",
        light = lightColorScheme(
            primary = Color(0xFF00639C),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCFE5FF),
            onPrimaryContainer = Color(0xFF001D34),
            secondary = Color(0xFF526069),
            secondaryContainer = Color(0xFFD6E4EF),
            surface = Color(0xFFF7F9FF),
            onSurface = Color(0xFF191C1F),
            surfaceVariant = Color(0xFFDDE3EA),
            onSurfaceVariant = Color(0xFF41474D),
            background = Color(0xFFF7F9FF),
            onBackground = Color(0xFF191C1F),
        ),
        dark = darkColorScheme(
            primary = Color(0xFF9ACBFF),
            onPrimary = Color(0xFF003355),
            primaryContainer = Color(0xFF004A79),
            onPrimaryContainer = Color(0xFFCFE5FF),
            secondary = Color(0xFFBAC8D3),
            secondaryContainer = Color(0xFF3B4851),
            surface = Color(0xFF111417),
            onSurface = Color(0xFFE1E2E6),
            surfaceVariant = Color(0xFF41474D),
            onSurfaceVariant = Color(0xFFC1C7CE),
            background = Color(0xFF111417),
            onBackground = Color(0xFFE1E2E6),
        ),
        preview = listOf(Color(0xFF00639C), Color(0xFFCFE5FF), Color(0xFFD6E4EF)),
    )
    val LAVENDER = PaletteSpec(
        key = PALETTE_LAVENDER, label = "浅紫罗兰",
        light = lightColorScheme(
            primary = Color(0xFF6D58B5),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE7DEFF),
            onPrimaryContainer = Color(0xFF22005D),
            secondary = Color(0xFF625B71),
            secondaryContainer = Color(0xFFE9DEF7),
            surface = Color(0xFFFDF7FF),
            onSurface = Color(0xFF1C1A22),
            surfaceVariant = Color(0xFFE6E0EE),
            onSurfaceVariant = Color(0xFF48454E),
            background = Color(0xFFFDF7FF),
            onBackground = Color(0xFF1C1A22),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFCBBEFF),
            onPrimary = Color(0xFF3A2385),
            primaryContainer = Color(0xFF533C9C),
            onPrimaryContainer = Color(0xFFE7DEFF),
            secondary = Color(0xFFCCC3DC),
            secondaryContainer = Color(0xFF4A4458),
            surface = Color(0xFF15131A),
            onSurface = Color(0xFFE7E1EA),
            surfaceVariant = Color(0xFF48454E),
            onSurfaceVariant = Color(0xFFCAC8D1),
            background = Color(0xFF15131A),
            onBackground = Color(0xFFE7E1EA),
        ),
        preview = listOf(Color(0xFF6D58B5), Color(0xFFE7DEFF), Color(0xFFE9DEF7)),
    )
    val MONO = PaletteSpec(
        key = PALETTE_MONO, label = "黑白色",
        light = lightColorScheme(
            primary = Color(0xFF1B1B1B),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE3E3E3),
            onPrimaryContainer = Color(0xFF111111),
            secondary = Color(0xFF555555),
            secondaryContainer = Color(0xFFE3E3E3),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1B1B1B),
            surfaceVariant = Color(0xFFEDEDED),
            onSurfaceVariant = Color(0xFF444444),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1B1B1B),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFE6E6E6),
            onPrimary = Color(0xFF1B1B1B),
            primaryContainer = Color(0xFF3A3A3A),
            onPrimaryContainer = Color(0xFFE6E6E6),
            secondary = Color(0xFFBDBDBD),
            secondaryContainer = Color(0xFF3A3A3A),
            surface = Color(0xFF0F0F0F),
            onSurface = Color(0xFFE6E6E6),
            surfaceVariant = Color(0xFF2A2A2A),
            onSurfaceVariant = Color(0xFFC9C9C9),
            background = Color(0xFF0F0F0F),
            onBackground = Color(0xFFE6E6E6),
        ),
        preview = listOf(Color(0xFF1B1B1B), Color(0xFFE3E3E3), Color(0xFF555555)),
    )

    val ALL: List<PaletteSpec> = listOf(TEAL, PINK, MINT, WATER, LAVENDER, MONO)

    fun byKey(key: Int): PaletteSpec = ALL.find { it.key == key } ?: TEAL
}

/**
 * themeMode: 0 跟随系统 / 1 浅色 / 2 深色
 * palette: PALETTE_SYSTEM（Android 12+ 动态取色，旧版本回退经典青绿）或固定色板
 */
@Composable
fun SavingTheme(themeMode: Int, palette: Int, content: @Composable () -> Unit) {
    val useDark = when (themeMode) {
        THEME_LIGHT -> false
        THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        palette == PALETTE_SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        palette == PALETTE_SYSTEM -> if (useDark) ThemePalettes.TEAL.dark else ThemePalettes.TEAL.light
        else -> {
            val spec = ThemePalettes.byKey(palette)
            if (useDark) spec.dark else spec.light
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
