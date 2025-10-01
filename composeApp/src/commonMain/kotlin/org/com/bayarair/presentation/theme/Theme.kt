package org.com.bayarair.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    error = Error,
    onError = OnError,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,

    )

val ColorScheme.activeButton: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkActiveButton else ActiveButton

val ColorScheme.activeButtonText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkActiveButtonText else ActiveButtonText

val ColorScheme.inactiveButton: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkInactiveButton else InactiveButton

val ColorScheme.inactiveButtonText: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkInactiveButtonText else InactiveButtonText

@Composable
fun BayarAirTheme(
    content: @Composable () -> Unit
) {
    val colors = LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
