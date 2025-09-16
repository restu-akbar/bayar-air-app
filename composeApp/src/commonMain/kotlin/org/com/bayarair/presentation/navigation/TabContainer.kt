package org.com.bayarair.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

object TabContainer : Screen {
    @Composable
    override fun Content() {
        TabNavigator(HomeTab) { tabNavigator ->
            val behavior = rememberBottomBarScrollBehavior()

            Scaffold(
                bottomBar = {
                    AnimatedVisibility(
                        visible = behavior.visible,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        Box(Modifier.padding(horizontal = 25.dp, vertical = 25.dp)) {
                            val shape = RoundedCornerShape(18.dp)
                            Surface(
                                shape = shape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 6.dp,
                                shadowElevation = 8.dp
                            ) {
                                Box(Modifier.clip(shape)) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        tonalElevation = 0.dp,
                                        windowInsets = WindowInsets(0, 0, 0, 0)
                                    ) {
                                        run {
                                            val selected = tabNavigator.current.key == HomeTab.key
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = { tabNavigator.current = HomeTab },
                                                icon = {
                                                    Icon(
                                                        imageVector = if (selected) Icons.Filled.Home else Icons.Outlined.Home,
                                                        contentDescription = HomeTab.options.title,
                                                        tint = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                },
                                                label = { Text(HomeTab.options.title) },
                                                alwaysShowLabel = true,
                                                colors = NavigationBarItemDefaults.colors(
                                                    indicatorColor = Color.Transparent,
                                                    selectedIconColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                    unselectedIconColor = Color.White,
                                                    selectedTextColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                    unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }

                                        Spacer(Modifier.weight(0.5f))

                                        run {
                                            val selected = tabNavigator.current.key == ProfileTab.key
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = { tabNavigator.current = ProfileTab },
                                                icon = {
                                                    Icon(
                                                        imageVector = if (selected) Icons.Filled.Person else Icons.Outlined.Person,
                                                        contentDescription = ProfileTab.options.title,
                                                        tint = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                },
                                                label = { Text(ProfileTab.options.title) },
                                                alwaysShowLabel = true,
                                                colors = NavigationBarItemDefaults.colors(
                                                    indicatorColor = Color.Transparent,
                                                    selectedIconColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                    unselectedIconColor = Color.White,
                                                    selectedTextColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                    unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            val middleSelected = tabNavigator.current.key == RecordTab.key
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = -20.dp)
                                    .size(75.dp)
                                    .clickable { tabNavigator.current = RecordTab }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    tonalElevation = 2.dp,
                                    modifier = Modifier.matchParentSize()
                                ) {}

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(65.dp)
                                ) {}

                                Icon(
                                    imageVector = if (middleSelected) Icons.Filled.Edit else Icons.Outlined.Edit,
                                    contentDescription = RecordTab.options.title,
                                    tint = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.align(Alignment.Center).size(30.dp)
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                Box(
                    Modifier
                        .padding(padding)
                        .nestedScroll(behavior.connection)
                ) {
                    CurrentTab()
                }
            }
        }
    }
}

@Stable
class BottomBarScrollBehavior(
    private val threshold: Float = 8f
) {
    var visible by mutableStateOf(true)
        private set

    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
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
fun rememberBottomBarScrollBehavior(
    threshold: Float = 8f
): BottomBarScrollBehavior {
    return remember { BottomBarScrollBehavior(threshold) }
}

