package org.com.bayarair

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.com.bayarair.presentation.navigation.BayarAirNav
import org.com.bayarair.presentation.theme.BayarAirTheme
import org.com.bayarair.core.AppEvents


@Composable
fun App(appEvents: AppEvents) {
    BayarAirTheme {
        BayarAirNav(appEvents)
    }
}
