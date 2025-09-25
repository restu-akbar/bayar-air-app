package org.com.bayarair.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.com.bayarair.presentation.component.PdfViewer
import org.com.bayarair.presentation.component.loadingOverlay
import org.com.bayarair.presentation.navigation.TabContainer
import org.com.bayarair.presentation.navigation.root
import org.com.bayarair.presentation.viewmodel.RecordDetailEvent
import org.com.bayarair.presentation.viewmodel.RecordDetailViewModel
import org.com.bayarair.utils.LocalReceiptPrinter

data class RecordDetailScreen(
    val url: String,
    val recordId: String,
    val isDetail: Boolean,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: RecordDetailViewModel = koinScreenModel()
        val printer = LocalReceiptPrinter.current
        val navigator = LocalNavigator.currentOrThrow.root()
        val snackbarHostState = remember { SnackbarHostState() }

        var showLoading by remember { mutableStateOf(false) }
        var loadingMessage by remember { mutableStateOf("Loading…") }

        LaunchedEffect(Unit) {
            vm.events.collect { ev ->
                when (ev) {
                    is RecordDetailEvent.ShowSnackbar ->
                        snackbarHostState.showSnackbar(ev.message)

                    is RecordDetailEvent.ShowLoading -> {
                        loadingMessage = ev.message
                        showLoading = true
                    }

                    RecordDetailEvent.HideLoading -> showLoading = false
                    RecordDetailEvent.NavigateNext -> navigator.replaceAll(TabContainer())
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Struk Pembayaran") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.replaceAll(TabContainer()) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Back",
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    windowInsets = TopAppBarDefaults.windowInsets,
                )
            },
            bottomBar = {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = { vm.printReceipt(printer, url, recordId) },
                        modifier = Modifier.weight(1f),
                        enabled = !showLoading,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(if (showLoading) "Memproses…" else "Print")
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }
            },
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                PdfViewer(
                    url = url,
                    modifier = Modifier.fillMaxSize(),
                )

                if (showLoading) {
                    loadingOverlay(
                        loadingMessage,
                    )
                }
            }
        }
    }
}
