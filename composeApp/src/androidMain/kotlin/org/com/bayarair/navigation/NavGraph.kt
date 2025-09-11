package org.com.bayarair.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.com.bayarair.viewmodel.HomeViewModel
import org.com.bayarair.ui.screens.HomeScreen
import androidx.compose.material3.Text

@Composable
fun BayarAirNav() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = "home"
    ) {
        composable("home") {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                presenter = vm.presenter,
                onOpenDetail = { id ->
                    nav.navigate("detail/$id")
                }
            )
        }

        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            DetailScreen(id = id)
        }
    }
}

@Composable
fun DetailScreen(id: String?) {
    Text(text = "Detail screen for item: $id")
}
