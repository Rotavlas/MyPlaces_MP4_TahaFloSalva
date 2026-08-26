package com.myplaces.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.myplaces.ui.viewmodel.PlacesViewModel

private sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    object Map      : Tab("map",       "Carte",     Icons.Default.Map)
    object MyPlaces : Tab("my_places", "Mes Lieux", Icons.Default.List)
    object Settings : Tab("settings",  "Paramètres",Icons.Default.Settings)
}

private val tabs = listOf(Tab.Map, Tab.MyPlaces, Tab.Settings)

@Composable
fun AppNavigation(viewModel: PlacesViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // La bottom bar ne s'affiche pas sur l'écran d'ajout
    val showBottomBar = tabs.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Tab.Map.route) {
                MapScreen(
                    viewModel = viewModel,
                    onAddPlace = { lat, lon ->
                        navController.navigate("add_place/$lat/$lon")
                    }
                )
            }
            composable(Tab.MyPlaces.route) {
                ListScreen(viewModel = viewModel)
            }
            composable(Tab.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
            composable(
                route = "add_place/{lat}/{lon}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lon") { type = NavType.StringType }
                )
            ) { backStack ->
                val lat = backStack.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                val lon = backStack.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
                AddPlaceScreen(lat = lat, lon = lon, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
