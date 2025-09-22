package org.com.bayarair

import androidx.compose.runtime.Composable
import org.com.bayarair.core.AppEvents
import org.com.bayarair.presentation.navigation.BayarAirNav
import org.com.bayarair.presentation.theme.BayarAirTheme


@Composable
fun App(appEvents: AppEvents) {
    BayarAirTheme {
        BayarAirNav(appEvents)
    }
}
