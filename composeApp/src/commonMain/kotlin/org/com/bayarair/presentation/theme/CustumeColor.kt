package org.com.bayarair.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val activeButton: Color,
    val activeButtonText: Color,
    val inactiveButton: Color,
    val inactiveButtonText: Color
)

val LightCustomColors = CustomColors(
    activeButton = Color(0xFFFFFFFF),
    activeButtonText = Color(0xFF000000),
    inactiveButton = Color(0xFFAAAAAA),
    inactiveButtonText = Color(0xFFFFFFFF)
)

val DarkCustomColors = CustomColors(
    activeButton = Color(0xFFFFFFFF),
    activeButtonText = Color(0xFFFFFFFF),
    inactiveButton = Color(0xFFAAAAAA),
    inactiveButtonText = Color(0xFFAAAAAA)
)

val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }
