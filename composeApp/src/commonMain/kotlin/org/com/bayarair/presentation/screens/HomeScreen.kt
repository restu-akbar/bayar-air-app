package org.com.bayarair.presentation.screens

import org.com.bayarair.data.token.TokenHandler
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.presentation.viewmodel.HomeViewModel
import org.com.bayarair.presentation.navigation.Routes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // << ini context-nya
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import android.widget.Toast // << untuk Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val authRepo: AuthRepository = koinInject()
    val tokenHandler: TokenHandler = koinInject()
    val vm = remember { HomeViewModel(tokenHandler, authRepo) }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hello from Compose Multiplatform 👋")
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    vm.logout(
                        onFinally = {
                            nav.navigate(Routes.Login) {
                                popUpTo(nav.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text("Logout")
                }
            }
        }
    }
}
