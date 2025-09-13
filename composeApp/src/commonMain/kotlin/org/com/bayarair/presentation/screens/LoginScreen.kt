package org.com.bayarair.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.compose.koinInject
import org.com.bayarair.presentation.navigation.Routes
import org.com.bayarair.presentation.theme.BayarAirTheme
import org.com.bayarair.data.token.TokenHandler
import org.com.bayarair.data.repository.AuthRepository
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import org.com.bayarair.Res
import org.com.bayarair.logo


@Composable
fun LoginScreen(nav: NavController) {
    val authRepo = koinInject<AuthRepository>()
    val tokenStore = koinInject<TokenHandler>()
    val vm = remember { AuthViewModel(authRepo, tokenStore) }

    val login by vm.login.collectAsState()
    val password by vm.password.collectAsState()
    val state by vm.state.collectAsState()

    val context = LocalContext.current


    LaunchedEffect(state) {
        val s = state
        when (s) {
            is AuthState.Success -> {
                nav.navigate(Routes.Home) {
                    popUpTo(Routes.Login) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    var showPass by rememberSaveable { mutableStateOf(false) }
    val loading = state is AuthState.Loading

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Bayar Air",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Bayar Atuh Euy!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    "Selamat Datang!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(5.dp))

                Text(
                    "Silakan login dengan akun anda",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )


                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = login,
                    onValueChange = vm::onLoginChange,
                    label = { Text("Username / Email", style = MaterialTheme.typography.labelLarge) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp),
                    shape = RoundedCornerShape(20.dp),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            cursorColor = MaterialTheme.colorScheme.onBackground,
                            focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            focusedLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        )
                )
                
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = vm::onPasswordChange,
                    shape = RoundedCornerShape(20.dp),
                    label = { Text("Password", style = MaterialTheme.typography.labelLarge) },
                    singleLine = true,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (showPass) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 44.dp),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            cursorColor = MaterialTheme.colorScheme.onBackground,
                            focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            focusedLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor =MaterialTheme.colorScheme.onBackground 
                        )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = vm::onLoginClick,
                    enabled = !loading && login.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Signing in…")
                    } else {
                        Text("Login")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
