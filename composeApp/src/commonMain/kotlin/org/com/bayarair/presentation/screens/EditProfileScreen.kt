@file:Suppress("FunctionName")

package org.com.bayarair.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.com.bayarair.presentation.navigation.root
import org.com.bayarair.presentation.viewmodel.ProfileViewModel

object EditProfileScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val rootNav = LocalNavigator.currentOrThrow.root()
        val vm: ProfileViewModel = koinScreenModel()
        val state by vm.state.collectAsState()
        val scrollState = rememberScrollState()
        LaunchedEffect(Unit) {
            vm.getUser()
        }

        val f = state.profileForm
        val e = state.errors
        val cs = MaterialTheme.colorScheme

        val headerHeight = 120.dp
        val avatarSize = 96.dp
        val overlapAbove = 1f
        val avatarTop = headerHeight - (avatarSize * overlapAbove)
        val contentTop = headerHeight - (avatarSize * 0.55f)

        val fieldColors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = cs.onPrimaryContainer,
                unfocusedBorderColor = cs.onPrimaryContainer.copy(alpha = 0.55f),
                disabledBorderColor = cs.onPrimaryContainer.copy(alpha = 0.25f),
                cursorColor = cs.onPrimaryContainer,
                focusedLabelColor = cs.onPrimaryContainer,
                unfocusedLabelColor = cs.onPrimaryContainer.copy(alpha = 0.75f),
                focusedTextColor = cs.onPrimaryContainer,
                unfocusedTextColor = cs.onPrimaryContainer,
                focusedLeadingIconColor = cs.onPrimaryContainer,
                unfocusedLeadingIconColor = cs.onPrimaryContainer,
            )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { rootNav.pop() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Kembali",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = cs.background,
                            titleContentColor = cs.onBackground,
                            navigationIconContentColor = cs.onBackground,
                            actionIconContentColor = cs.onBackground,
                        ),
                )
            },
            containerColor = cs.background,
        ) { inner ->
            Box(Modifier.fillMaxSize().padding(inner)) {
                Surface(
                    color = cs.background,
                    shape = RoundedCornerShape(bottomStart = 1000.dp, bottomEnd = 1000.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(headerHeight)
                            .align(Alignment.TopCenter),
                ) {}

                Surface(
                    color = cs.primaryContainer,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = contentTop),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                                .imePadding()
                                .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        if (state.loading || state.saving) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        Spacer(Modifier.height(avatarSize * 0.40f))

                        Text(
                            "Nama",
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onPrimaryContainer,
                        )
                        OutlinedTextField(
                            value = f.name,
                            onValueChange = vm::onNamaChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = e.name != null,
                            supportingText = {
                                e.name?.let {
                                    Text(
                                        it,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            colors = fieldColors,
                        )

                        // ==== Username ====
                        Text(
                            "Username",
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onPrimaryContainer,
                        )
                        OutlinedTextField(
                            value = f.username,
                            onValueChange = vm::onUsernameChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = e.username != null,
                            supportingText = {
                                e.username?.let {
                                    Text(
                                        it,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            colors = fieldColors,
                        )

                        // ==== No HP ====
                        Text(
                            "No HP",
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onPrimaryContainer,
                        )
                        OutlinedTextField(
                            value = f.phone_number,
                            onValueChange = vm::onNoHpChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = e.phone_number != null,
                            supportingText = {
                                e.phone_number?.let {
                                    Text(
                                        it,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            colors = fieldColors,
                        )

                        // ==== Email ====
                        Text(
                            "Email",
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onPrimaryContainer,
                        )
                        OutlinedTextField(
                            value = f.email,
                            onValueChange = vm::onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = e.email != null,
                            supportingText = {
                                e.email?.let {
                                    Text(
                                        it,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                            colors = fieldColors,
                        )

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = vm::updateProfile,
                            enabled = state.canSubmit && !state.saving,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            if (state.saving) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                )
                            } else {
                                Text("Simpan")
                            }
                        }
                    }
                }

                Surface(
                    color = cs.primary,
                    shape = CircleShape,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    modifier =
                        Modifier
                            .size(avatarSize)
                            .align(Alignment.TopCenter)
                            .offset(y = avatarTop),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = f.name.initials(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = cs.onPrimary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

private fun String.initials(): String {
    val parts = trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].first().toString() + parts[1].first()).uppercase()
    }
}
