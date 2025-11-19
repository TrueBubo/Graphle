package com.graphle.common.ui

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

/**
 * Light blue color used for primary elements.
 */
internal val Color.Companion.LightBlue: Color
    get() = Color(0xFF0086FC)

/**
 * Dark background color for text fields in dark theme.
 */
internal val Color.Companion.TextFieldDarkColor: Color
    get() = Color(0x1F1F1F).copy(alpha = 1f)

/**
 * Light background color for text fields in light theme.
 */
internal val Color.Companion.TextFieldLightColor: Color
    get() = Color(0xE0E0E0).copy(alpha = 1f)

/**
 * Dark theme color palette for the application.
 */
internal val DarkColorPalette = darkColors(
    primary = Color.LightBlue,
    primaryVariant = Color.TextFieldDarkColor,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * Light theme color palette for the application.
 */
internal val LightColorPalette = lightColors(
    primary = Color.Blue,
    primaryVariant = Color.TextFieldLightColor,
    background = Color.White,
    surface = Color.LightGray,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

