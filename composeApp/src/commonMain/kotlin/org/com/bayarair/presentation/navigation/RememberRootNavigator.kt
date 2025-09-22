package org.com.bayarair.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow

@Composable
fun rememberRootNavigator(): Navigator {
    val nav = LocalNavigator.currentOrThrow
    return remember(nav) {
        generateSequence(nav) { it.parent }.last()
    }
}
