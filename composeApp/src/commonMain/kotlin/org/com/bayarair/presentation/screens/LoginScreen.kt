package org.com.bayarair.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.com.bayarair.Res
import org.com.bayarair.logo
import org.com.bayarair.presentation.navigation.TabContainer
import org.com.bayarair.presentation.viewmodel.AuthState
import org.com.bayarair.presentation.viewmodel.AuthViewModel
import org.jetbrains.compose.resources.painterResource

data class LoginScreen(
    val message: String? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm: AuthViewModel = koinScreenModel()

        val login by vm.login.collectAsState()
        val password by vm.password.collectAsState()
        val state by vm.state.collectAsState()

        val snackbarHost = remember { SnackbarHostState() }
        var showPass by rememberSaveable { mutableStateOf(false) }
        val loading = state is AuthState.Loading

        LaunchedEffect(message) {
            message
                ?.takeIf { it.isNotBlank() }
                ?.let { snackbarHost.showSnackbar(it) }
        }

        LaunchedEffect(state) {
            when (val s = state) {
                is AuthState.Success -> {
                    navigator.replaceAll(TabContainer(s.message))
                }

                else -> Unit
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHost) },
        ) { padding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp)
                            .padding(horizontal = 24.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Bayar Air",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(32.dp))

                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(160.dp),
                    )

                    Spacer(Modifier.height(32.dp))

                    Text(
                        "Selamat Datang!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        "Silakan login dengan akun anda",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = login,
                        onValueChange = vm::onLoginChange,
                        label = {
                            Text(
                                "Username / Email",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        singleLine = true,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.7f
                                ),
                                cursorColor = MaterialTheme.colorScheme.onBackground,
                                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.8f
                                ),
                                focusedLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedLeadingIconColor =
                                    MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.8f,
                                    ),
                                focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTrailingIconColor =
                                    MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.8f,
                                    ),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            ),
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = vm::onPasswordChange,
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        label = { Text("Password", style = MaterialTheme.typography.labelLarge) },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (showPass) "Hide password" else "Show password",
                                )
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.7f
                                ),
                                cursorColor = MaterialTheme.colorScheme.onBackground,
                                focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.8f
                                ),
                                focusedLeadingIconColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedLeadingIconColor =
                                    MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.8f,
                                    ),
                                focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTrailingIconColor =
                                    MaterialTheme.colorScheme.onBackground.copy(
                                        alpha = 0.8f,
                                    ),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            ),
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = vm::onLoginClick,
                        enabled = !loading && login.isNotBlank() && password.isNotBlank(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                disabledContainerColor =
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(
                                        alpha = 0.5f,
                                    ),
                                disabledContentColor =
                                    MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                        alpha = 0.6f,
                                    ),
                            ),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
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
}
