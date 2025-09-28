package org.com.bayarair.presentation.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    val isDetail: Boolean = true,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: RecordDetailViewModel = koinScreenModel<RecordDetailViewModel>()
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
                    RecordDetailEvent.NavigateNext -> navigator.replaceAll(TabContainer(startAtHome = true))
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Struk Pembayaran") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isDetail) {
                                navigator.pop()
                            } else {
                                navigator.replaceAll(TabContainer(startAtHome = true))
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Back",
                                Modifier.size(18.dp)
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
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 50.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    DualActionButton(
                        text = "Print",
                        enabled = !showLoading,
                        onClick = {
                            vm.printReceipt(
                                printer,
                                url,
                                recordId
                            )
                        },
                        onLongClick = {
                            vm.printReceipt(
                                printer,
                                url,
                                recordId,
                                forcePick = true
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )


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

@Composable
fun DualActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container =
        if (enabled) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    val content =
        if (enabled) MaterialTheme.colorScheme.onTertiaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant

    val interaction = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = container,
        contentColor = content,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .combinedClickable(
                    enabled = enabled,
                    interactionSource = interaction,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

