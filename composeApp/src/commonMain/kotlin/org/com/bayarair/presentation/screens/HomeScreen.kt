package org.com.bayarair.presentation.screens

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import kotlinx.coroutines.flow.collectLatest
import cafe.adriel.voyager.koin.koinScreenModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import org.com.bayarair.presentation.viewmodel.AuthViewModel
import org.com.bayarair.presentation.viewmodel.AuthEvent

object HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authVm: AuthViewModel = koinScreenModel()
        val snackbarHost = remember { SnackbarHostState() }
        val rootNavigator = remember(navigator) {
            generateSequence(navigator) { it.parent }.last()
        }

        LaunchedEffect(Unit) {
            authVm.events.collectLatest { ev ->
                when (ev) {
                    is AuthEvent.LogoutError -> snackbarHost.showSnackbar(ev.message)
                    AuthEvent.LoggedOut -> rootNavigator.replaceAll(LoginScreen)
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHost) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Text("Hello from Compose Multiplatform 👋")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { authVm.logout() }) { Text("Logout") }
                        Spacer(Modifier.height(24.dp))
                        Text("Scroll list di bawah buat ngetes bottom bar auto-hide:")
                        Spacer(Modifier.height(12.dp))
                    }
                
                    items(count = 60) { index ->
                        val i = index + 1
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Item $i", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Text("Ini dummy content untuk cek perilaku scrolling.")
                            }
                        }
                    }
                }
            }
        }
    }
}
