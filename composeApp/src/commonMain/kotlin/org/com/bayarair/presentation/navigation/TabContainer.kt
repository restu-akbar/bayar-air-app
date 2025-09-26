package org.com.bayarair.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Person2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator

val LocalPreviousTabKey = staticCompositionLocalOf { mutableStateOf(HomeTab.key) }

data class TabContainer(
    val startupMessage: String? = null,
) : Screen {
    @Composable
    override fun Content() {
        TabNavigator(HomeTab) { tabNavigator ->
            val behavior = rememberBottomBarScrollBehavior()
            val prevTabKey = rememberSaveable { mutableStateOf(HomeTab.key) }
            val snackbarHost = remember { SnackbarHostState() }

            LaunchedEffect(tabNavigator.current.key) {
                val k = tabNavigator.current.key
                if (k != RecordTab.key) prevTabKey.value = k
            }
            val isRecordTab = tabNavigator.current.key == RecordTab.key

            LaunchedEffect(startupMessage) {
                startupMessage
                    ?.takeIf { it.isNotBlank() }
                    ?.let { snackbarHost.showSnackbar(it) }
            }
            CompositionLocalProvider(LocalPreviousTabKey provides prevTabKey) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHost) },
                    bottomBar = {
                        if (!isRecordTab) {
                            AnimatedVisibility(
                                visible = behavior.visible,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut(),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 24.dp),
                                ) {
                                    val barShape = RoundedCornerShape(18.dp)
                                    val barHeight = 70.dp
                                    val middleBtnSize = 56.dp
                                    val middleBtnOffset = -(middleBtnSize * 0.37f)

                                    Surface(
                                        shape = barShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        tonalElevation = 6.dp,
                                        shadowElevation = 10.dp,
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .height(barHeight),
                                    ) {
                                        Box(Modifier.fillMaxSize()) {
                                            NavigationBar(
                                                modifier =
                                                    Modifier
                                                        .align(Alignment.Center)
                                                        .fillMaxWidth()
                                                        .height(56.dp),
                                                windowInsets = WindowInsets(0),
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            ) {
                                                // Home
                                                run {
                                                    val selected =
                                                        tabNavigator.current.key == HomeTab.key
                                                    NavigationBarItem(
                                                        selected = selected,
                                                        onClick = {
                                                            tabNavigator.current = HomeTab
                                                        },
                                                        icon = {
                                                            Icon(
                                                                imageVector = if (selected) Icons.Filled.House else Icons.Outlined.House,
                                                                contentDescription = HomeTab.options.title,
                                                                tint =
                                                                    if (selected) {
                                                                        MaterialTheme.colorScheme.tertiaryContainer
                                                                    } else {
                                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                                    },
                                                            )
                                                        },
                                                        label = { Text(HomeTab.options.title) },
                                                        alwaysShowLabel = true,
                                                        colors =
                                                            NavigationBarItemDefaults.colors(
                                                                indicatorColor = Color.Transparent,
                                                                selectedIconColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                                unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                selectedTextColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                                unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            ),
                                                    )
                                                }

                                                Spacer(Modifier.weight(0.3f))

                                                // Profile
                                                run {
                                                    val selected =
                                                        tabNavigator.current.key == ProfileTab.key
                                                    NavigationBarItem(
                                                        selected = selected,
                                                        onClick = {
                                                            tabNavigator.current = ProfileTab
                                                        },
                                                        icon = {
                                                            Icon(
                                                                imageVector = if (selected) Icons.Filled.Person2 else Icons.Outlined.Person2,
                                                                contentDescription = ProfileTab.options.title,
                                                                tint =
                                                                    if (selected) {
                                                                        MaterialTheme.colorScheme.tertiaryContainer
                                                                    } else {
                                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                                    },
                                                            )
                                                        },
                                                        label = { Text(ProfileTab.options.title) },
                                                        alwaysShowLabel = true,
                                                        colors =
                                                            NavigationBarItemDefaults.colors(
                                                                indicatorColor = Color.Transparent,
                                                                selectedIconColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                                unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                selectedTextColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                                unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            ),
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // record
                                    val middleSelected = tabNavigator.current.key == RecordTab.key
                                    Box(
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomCenter)
                                                .offset(y = middleBtnOffset),
                                    ) {
                                        val ringThickness = 5.dp
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shadowElevation = 0.dp,
                                            tonalElevation = 0.dp,
                                            modifier =
                                                Modifier
                                                    .size(middleBtnSize + ringThickness * 2),
                                        ) {}

                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shadowElevation = 8.dp,
                                            tonalElevation = 2.dp,
                                            modifier =
                                                Modifier
                                                    .align(Alignment.Center)
                                                    .size(middleBtnSize)
                                                    .clip(CircleShape)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                    ) { tabNavigator.current = RecordTab },
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (middleSelected) Icons.Filled.Edit else Icons.Outlined.Edit,
                                                    contentDescription = RecordTab.options.title,
                                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                ) { padding ->
                    Box(
                        Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .let { base ->
                                if (tabNavigator.current.key == RecordTab.key) {
                                    base
                                } else {
                                    base.nestedScroll(behavior.connection)
                                }
                            },
                    ) {
                        CurrentTab()
                    }
                }
            }
        }
    }
}

@Stable
class BottomBarScrollBehavior(
    private val threshold: Float = 8f,
) {
    var visible by mutableStateOf(true)
        private set

    val connection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val dy = available.y
                if (dy < -threshold) {
                    if (visible) visible = false
                } else if (dy > threshold) {
                    if (!visible) visible = true
                }
                return Offset.Zero
            }
        }
}

@Composable
fun rememberBottomBarScrollBehavior(threshold: Float = 8f): BottomBarScrollBehavior =
    remember { BottomBarScrollBehavior(threshold) }
