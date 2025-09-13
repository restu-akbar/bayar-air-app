package org.com.bayarair.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator

object TabContainer : Screen {
    @Composable
    override fun Content() {
        val tabs = listOf(HomeTab, RecordTab, ProfileTab)

        TabNavigator(HomeTab) { tabNavigator ->
            Scaffold(
                bottomBar = {
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
            ) { padding ->
                Box(Modifier.padding(padding)) { CurrentTab() }
            }
        }
    }
}
