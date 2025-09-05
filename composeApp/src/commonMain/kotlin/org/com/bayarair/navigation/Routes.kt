package com.meet.bottom_navigation_bar_navigation_rail.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*


object Graph {
    const val NAVIGATION_BAR_SCREEN_GRAPH = "navigationBarScreenGraph"
}

sealed class Routes(var route: String) {
    data object Home : Routes("home")
    data object Pencatatan : Routes("pencatatan")
    data object History : Routes("history")

}

val navigationItemsLists = listOf(

    NavigationItem(
        unSelectedIcon =  Icons.Outlined.Edit,
        selectedIcon =  Icons.Filled.Edit,
        title = "Pencatatan",
        route = Routes.Pencatatan.route,
    ),
    NavigationItem(
        unSelectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        title = "Home",
        route = Routes.Home.route,
    ),
    NavigationItem(
        unSelectedIcon =  Icons.Outlined.DateRange,
        selectedIcon =  Icons.Filled.DateRange,
        title = "History",
        route = Routes.History.route,
    ),
)
//
//val navigationItemsLists = listOf(
//    NavigationItem(
//        unSelectedIcon = R.drawable.outlined_water_drop ,
//        selectedIcon = R.drawable.filled_water_drop ,
//        title = "Pencatatan",
//        route = Routes.Pencatatan.route,
//    ),
//    NavigationItem(
//        unSelectedIcon = R.drawable.outlined_home,
//        selectedIcon = R.drawable.filled_home,
//        title = "Home",
//        route = Routes.Home.route,
//    ),
//    NavigationItem(
//        unSelectedIcon =  R.drawable.history,
//        selectedIcon =  R.drawable.history,
//        title = "Histroy",
//        route = Routes.History.route,
//    ),
//)