package org.com.bayarair.presentation.navigation

import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator

fun Navigator.root(): Navigator = parent?.root() ?: this
