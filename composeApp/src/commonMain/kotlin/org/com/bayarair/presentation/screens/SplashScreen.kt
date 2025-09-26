package org.com.bayarair.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.com.bayarair.Res
import org.com.bayarair.logo
import org.com.bayarair.presentation.navigation.TabContainer
import org.com.bayarair.presentation.viewmodel.SplashState
import org.com.bayarair.presentation.viewmodel.SplashViewModel
import org.jetbrains.compose.resources.painterResource

object SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm: SplashViewModel = koinScreenModel<SplashViewModel>()
        val state by vm.state.collectAsState()

        LaunchedEffect(state) {
            when (state) {
                SplashState.GoLogin -> navigator.replace(LoginScreen())
                SplashState.GoHome -> navigator.replace(TabContainer())
                SplashState.Loading -> Unit
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
