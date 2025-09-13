package org.com.bayarair.presentation.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import org.com.bayarair.presentation.screens.SplashScreen

@Composable
fun BayarAirNav() {
    Navigator(SplashScreen) { navigator ->
        navigator.lastItem.Content()
    }
}
