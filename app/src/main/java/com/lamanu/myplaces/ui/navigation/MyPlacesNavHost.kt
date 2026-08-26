package com.lamanu.myplaces.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lamanu.myplaces.ui.addplace.AddPlaceScreen
import com.lamanu.myplaces.ui.map.MapScreen
import com.lamanu.myplaces.ui.settings.SettingsScreen

@Composable
fun MyPlacesNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Destinations.MAP) {

        composable(Destinations.MAP) {
            MapScreen(
                onAddPlace = { latitude, longitude ->
                    navController.navigate(Destinations.addPlace(latitude, longitude))
                },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
            )
        }

        composable(
            route = Destinations.ADD_PLACE,
            arguments = listOf(
                // StringType et non FloatType : un Float perd trop de precision sur des
                // coordonnees GPS (~1 m d'erreur des la 7e decimale).
                navArgument(Destinations.ARG_LATITUDE) { type = NavType.StringType },
                navArgument(Destinations.ARG_LONGITUDE) { type = NavType.StringType },
            ),
        ) {
            AddPlaceScreen(onDone = { navController.popBackStack() })
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
