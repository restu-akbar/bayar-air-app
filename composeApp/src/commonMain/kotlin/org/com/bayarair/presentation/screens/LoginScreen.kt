package org.com.bayarair.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import org.com.bayarair.presentation.navigation.Routes
import org.com.bayarair.data.token.TokenHandler
import org.com.bayarair.data.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(nav: NavController) {
    val authRepo = koinInject<AuthRepository>()
    val tokenStore = koinInject<TokenHandler>()
    val vm = remember { LoginViewModel(authRepo, tokenStore) }

    val login by vm.login.collectAsState()
    val password by vm.password.collectAsState()
    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            nav.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Login") }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = login,
                onValueChange = vm::onLoginChange,
                label = { Text("Username / Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = vm::onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            val loading = state is LoginState.Loading
            Button(
                onClick = vm::onLoginClick,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Signing in…")
                } else {
                    Text("Login")
                }
            }

            if (state is LoginState.Error) {
                Spacer(Modifier.height(12.dp))
                Text(
                    (state as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
