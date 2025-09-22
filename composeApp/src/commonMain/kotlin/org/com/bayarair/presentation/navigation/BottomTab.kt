package org.com.bayarair.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.com.bayarair.presentation.screens.HomeScreen
import org.com.bayarair.presentation.screens.ProfileScreen
import org.com.bayarair.presentation.screens.RecordScreen

object HomeTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val title = "Home"
            val icon = rememberVectorPainter(Icons.Outlined.Home)
            return remember { TabOptions(index = 0u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(HomeScreen) { nav ->
            nav.lastItem.Content()
        }
    }
}

object RecordTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val title = "Record"
            val icon = rememberVectorPainter(Icons.Outlined.Edit)
            return remember { TabOptions(index = 1u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(RecordScreen) { nav ->
            nav.lastItem.Content()
        }
    }
}

object ProfileTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val title = "Profile"
            val icon = rememberVectorPainter(Icons.Outlined.Person)
            return remember { TabOptions(index = 2u, title = title, icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(ProfileScreen) { nav ->
            nav.lastItem.Content()
        }
    }
}
