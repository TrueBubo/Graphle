package com.graphle

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

internal val Color.Companion.LightBlue: Color
    get() = Color(0xFF0086FC)
internal val DarkColorPalette = darkColors(
    primary = Color.LightBlue,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

internal val LightColorPalette = lightColors(
    primary = Color.Blue,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

