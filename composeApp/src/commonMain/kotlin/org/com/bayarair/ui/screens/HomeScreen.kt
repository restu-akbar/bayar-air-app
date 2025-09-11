package org.com.bayarair.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.com.bayarair.shared.home.HomePresenter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    presenter: HomePresenter,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by presenter.state.collectAsState()

    LaunchedEffect(Unit) { presenter.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bayar Air") },
                actions = {
                    IconButton(onClick = { presenter.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(inner),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Memuat…")
                    }
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error ?: "Terjadi kesalahan",
                        onRetry = { presenter.load() }
                    )
                }
                else -> {
                    GreetingCard(
                        greeting = state.greeting,
                        onDetail = { onOpenDetail("123") }
                    )
                }
            }
        }
    }
}

@Composable
private fun GreetingCard(
    greeting: String,
    onDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = greeting.ifBlank { "Halo! 👋" },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Selamat datang di Compose Multiplatform.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onDetail) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Lihat Detail")
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ups, ada kendala",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Coba lagi")
        }
    }
}
