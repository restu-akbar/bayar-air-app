package org.com.bayarair.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.collectLatest
import org.com.bayarair.presentation.viewmodel.HomeViewModel

object HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm: HomeViewModel = koinScreenModel()
        val snackbarHost = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            vm.onErrorMessage.collectLatest { msg ->
                snackbarHost.showSnackbar(msg)
            }
        }
        val rootNavigator = remember(navigator) {
            generateSequence(navigator) { it.parent }.last()
        }
        LaunchedEffect(Unit) {
            vm.onLoggedOut.collectLatest { ok ->
                if (ok) rootNavigator.replaceAll(LoginScreen)
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHost) }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hello from Compose Multiplatform 👋")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { vm.logout() }) { Text("Logout") }
                }
            }
        }
    }
}
