package org.com.bayarair.presentation.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.navigation.NavController
import org.com.bayarair.presentation.navigation.Routes
import org.com.bayarair.presentation.viewmodel.SplashViewModel
import org.com.bayarair.presentation.viewmodel.SplashState
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

import org.jetbrains.compose.resources.painterResource
import org.com.bayarair.Res
import org.com.bayarair.logo

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

    Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
               
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
    
                Spacer(modifier = Modifier.height(24.dp))
    
               
                CircularProgressIndicator(
                    color = Color(0xFF0A0171)
                )
            }
        }
}
