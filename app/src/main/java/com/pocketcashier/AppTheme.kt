package com.pocketcashier

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val AppNavy = Color(0xFF0D1B2A)
val AppBlue = Color(0xFF1B263B)
val AppGold = Color(0xFFE0A96D)
val AppBackground = Color(0xFFF4F6F9)

val LightColors = lightColorScheme(
    primary = AppNavy,
    onPrimary = Color.White,
    secondary = AppGold,
    onSecondary = Color.Black,
    background = AppBackground,
    surface = Color.White
)

val DarkColors = darkColorScheme(
    primary = AppGold,
    onPrimary = Color.Black,
    secondary = AppBlue,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)
