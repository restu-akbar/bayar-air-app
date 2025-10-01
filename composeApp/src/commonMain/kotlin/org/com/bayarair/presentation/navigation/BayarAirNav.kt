package org.com.bayarair.presentation.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.platform.SystemBackHandler
import org.com.bayarair.presentation.screens.LoginScreen
import org.com.bayarair.presentation.screens.SplashScreen

@Composable
fun BayarAirNav(appEvents: AppEvents) {
    val snackbarHostState = remember { SnackbarHostState() }
    Navigator(SplashScreen) { navigator ->
        LaunchedEffect(Unit) {
            appEvents.events.collect { event ->
                when (event) {
                    is AppEvent.Logout -> {
                        navigator.replaceAll(LoginScreen())
                        event.message?.let { snackbarHostState.showSnackbar(it) }
                    }

                    is AppEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
            CurrentScreen()
        }
    }
}
