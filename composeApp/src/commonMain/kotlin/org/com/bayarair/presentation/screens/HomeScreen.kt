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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.com.bayarair.presentation.theme.activeButton
import org.com.bayarair.presentation.theme.activeButtonText
import org.com.bayarair.presentation.theme.inactiveButton
import org.com.bayarair.presentation.theme.inactiveButtonText
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
        var name = "Acong Sukoco" //
        var switcher by remember { mutableStateOf(true) } // History

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
            Column(
            ){
                Column(
                    modifier = Modifier
                        .padding(2.dp),
                ){
                    Text(
                        text = "Bayar Air Dashboard",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Selamat Datang, $name !",
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { switcher = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!switcher)
                                    MaterialTheme.colorScheme.activeButton
                                else
                                    MaterialTheme.colorScheme.inactiveButton,
                                contentColor = if (!switcher)
                                    MaterialTheme.colorScheme.activeButtonText
                                else
                                    MaterialTheme.colorScheme.inactiveButtonText,
                            )
                        ) {
                            Text("Statistik")
                        }
                        Button(
                            onClick = { switcher = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (switcher)
                                    MaterialTheme.colorScheme.activeButton
                                else
                                    MaterialTheme.colorScheme.inactiveButton,
                                contentColor = if (switcher)
                                    MaterialTheme.colorScheme.activeButtonText
                                else
                                    MaterialTheme.colorScheme.inactiveButtonText,
                            )
                        ) {
                            Text("History")
                        }
                    }
                }
                if (switcher){
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
                            items(count = 20) { index ->
                                val i = index + 1
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text("Nama pelanggan $i", style = MaterialTheme.typography.titleMedium)
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ){
                                            Column(){
                                                Text(" Status Pembayaran")
                                                Text(" tanggal dibuat")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else{
                    Text("ini statistik")
                }
            }

        }
    }
}
