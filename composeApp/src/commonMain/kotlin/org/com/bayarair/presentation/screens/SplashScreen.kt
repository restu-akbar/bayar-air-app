package org.com.bayarair.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.com.bayarair.presentation.navigation.Routes
import org.com.bayarair.presentation.viewmodel.SplashViewModel
import org.com.bayarair.presentation.viewmodel.SplashState
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler
import org.koin.compose.koinInject

@Composable
fun SplashScreen(nav: NavController) {
    val tokenStore = koinInject<TokenHandler>()
    val authRepo = koinInject<AuthRepository>()
    val vm = remember { SplashViewModel(tokenStore, authRepo) }

    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            SplashState.GoLogin -> {
                nav.navigate(Routes.Login) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            }
            SplashState.GoHome -> {
                nav.navigate(Routes.Home) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            }
            SplashState.Loading -> Unit
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
