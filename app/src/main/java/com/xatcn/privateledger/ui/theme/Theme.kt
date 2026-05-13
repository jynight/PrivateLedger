package com.xatcn.privateledger.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 克莱因蓝色彩定义
val KleinBlue = Color(0xFF002FA7)
val KleinBlueLight = Color(0xFF4D6FB5)
val KleinBlueDark = Color(0xFF001F75)

val IncomeGreen = Color(0xFF34C759)
val ExpenseRed = Color(0xFFFF3B30)

private val LightColorScheme = lightColorScheme(
    primary = KleinBlue,
    onPrimary = Color.White,
    primaryContainer = KleinBlueLight,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF5856D6),
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF8E8E93),
    error = ExpenseRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = KleinBlueLight,
    onPrimary = Color.White,
    primaryContainer = KleinBlue,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF5856D6),
    onSecondary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = Color(0xFF1C1C1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF8E8E93),
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun PrivateLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
