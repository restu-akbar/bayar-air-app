package org.com.bayarair.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.com.bayarair.BuildConfig
import org.com.bayarair.presentation.component.ConfirmDialog
import org.com.bayarair.presentation.component.LoadingOverlay
import org.com.bayarair.presentation.navigation.root
import org.com.bayarair.presentation.viewmodel.AuthViewModel
import org.com.bayarair.presentation.viewmodel.ProfileViewModel
import org.com.bayarair.presentation.viewmodel.UserShared
import org.koin.compose.koinInject

object ProfileScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val rootNav = LocalNavigator.currentOrThrow.root()
        val vm: ProfileViewModel = koinScreenModel()
        val authVm: AuthViewModel = koinScreenModel<AuthViewModel>()
        val state by vm.state.collectAsState()
        var show by remember { mutableStateOf(false) }
        var dialogType by remember { mutableStateOf<String?>(null) }
        val uriHandler = LocalUriHandler.current
        val userShared: UserShared = koinInject()
        val user by userShared.user.collectAsState()

        LaunchedEffect(Unit) {
            vm.getUser()
        }


        Scaffold { _ ->
            if (user != null) {
                PullToRefreshBox(
                    isRefreshing = state.loading,
                    onRefresh = { vm.getUser(true) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(top = 50.dp),
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val initials = user?.name
                                ?.split(" ")
                                ?.filter { it.isNotBlank() }
                                ?.take(2)
                                ?.map { it.first().uppercaseChar() }
                                ?.joinToString("")
                                ?: "U"
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = user?.name ?: "User",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = user?.email ?: "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.height(35.dp))
                        val iconMap = mapOf(
                            "edit" to Icons.Default.BorderColor,
                            "password" to Icons.Default.LockReset,
                            "faq" to Icons.AutoMirrored.Filled.HelpCenter,
                            "logout" to Icons.AutoMirrored.Filled.Logout
                        )

                        ProfileMenuCard(
                            icon = iconMap["edit"] ?: Icons.Default.BorderColor,
                            text = "Edit Profil",
                            onClick = { rootNav.push(EditProfileScreen) }
                        )
                        ProfileMenuCard(
                            icon = iconMap["password"] ?: Icons.Default.LockReset,
                            text = "Ganti Password",
                            onClick = {
                                show = true
                                dialogType = "password"
                            }
                        )

                        ProfileMenuCard(
                            icon = iconMap["faq"] ?: Icons.AutoMirrored.Filled.HelpCenter,
                            text = "FAQ",
                            onClick = { rootNav.push(FaqScreen) }
                        )

                        ProfileMenuCard(
                            icon = iconMap["logout"] ?: Icons.AutoMirrored.Filled.Logout,
                            text = "Logout",
                            onClick = {
                                show = true
                                dialogType = "logout"
                            }
                        )
                    }
                    Text(
                        text = "Bayar Air v${BuildConfig.APP_VERSION}",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp
                    )
                    if (show && dialogType != null) {
                        when (dialogType) {
                            "password" -> {
                                ConfirmDialog(
                                    visible = true,
                                    title = "Ganti Password?",
                                    message = "Anda akan diarahkan ke halaman ganti password.",
                                    confirmText = "Lanjut",
                                    cancelText = "Batal",
                                    destructive = true,
                                    onConfirm = {
                                        show = false
                                        uriHandler.openUri("https://bayarair.progantara.com/forgot-password")
                                    },
                                    onDismiss = { show = false }
                                )
                            }

                            "logout" -> {
                                ConfirmDialog(
                                    visible = true,
                                    title = "Log out?",
                                    message = "Apakah anda yakin ingin logout dari akun ini?",
                                    confirmText = "Logout",
                                    cancelText = "Batal",
                                    destructive = true,
                                    onConfirm = {
                                        show = false
                                        authVm.logout()
                                    },
                                    onDismiss = { show = false }
                                )
                            }
                        }
                    }
                }
            } else {
                LoadingOverlay()
            }
        }
    }
}

@Composable
fun ProfileMenuCard(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    val color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 5.dp,
                horizontal = 20.dp
            )
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = color
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = color
            )
        }
    }
}
