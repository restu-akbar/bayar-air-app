package org.com.bayarair.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import org.com.bayarair.presentation.navigation.HomeTab
import org.com.bayarair.presentation.navigation.LocalPreviousTabKey
import org.com.bayarair.presentation.navigation.ProfileTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickyScaffold(
    bg: Color,
    textOnBg: Color,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable () -> Unit
) {
    val tabNavigator = LocalTabNavigator.current
    val prevKey = LocalPreviousTabKey.current.value

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            tabNavigator.current = when (prevKey) {
                                HomeTab.key -> HomeTab
                                ProfileTab.key -> ProfileTab
                                else -> HomeTab
                            }
                        },
                        modifier = Modifier
                            .padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Kembali",
                            tint = textOnBg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    scrolledContainerColor = bg,
                    navigationIconContentColor = textOnBg
                ),
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            content()
        }
    }
}
