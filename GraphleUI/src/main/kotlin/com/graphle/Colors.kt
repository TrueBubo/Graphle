package com.graphle

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

internal val Color.Companion.LightBlue: Color
    get() = Color(0xFF0086FC)
internal val Color.Companion.TextFieldDarkColor: Color
    get() = Color(0x1F1F1F).copy(alpha = 1f)
internal val Color.Companion.TextFieldLightColor: Color
    get() = Color(0xE0E0E0).copy(alpha = 1f)
internal val DarkColorPalette = darkColors(
    primary = Color.LightBlue,
    primaryVariant = Color.TextFieldDarkColor,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

internal val LightColorPalette = lightColors(
    primary = Color.Blue,
    primaryVariant = Color.TextFieldLightColor,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

