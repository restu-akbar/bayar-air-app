package org.com.bayarair.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import org.com.bayarair.presentation.screens.SplashScreen
import org.com.bayarair.presentation.screens.HomeScreen
import org.com.bayarair.presentation.screens.LoginScreen // buat placeholder dulu

@Composable
fun BayarAirNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.Splash) {
        composable(Routes.Splash) { SplashScreen(nav) }
        composable(Routes.Login)  { LoginScreen(nav) }
        composable(Routes.Home)   { HomeScreen() } 
    }
}
