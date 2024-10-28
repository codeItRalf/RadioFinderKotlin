package app.codeitralf.radiofinder.navigation

import MainScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.ui.common.exoVisualizer.SharedVisualizer
import app.codeitralf.radiofinder.ui.feature.details.DetailsScreen

sealed class NavigationRoute(val route: String) {
    data object Main : NavigationRoute("main")
    data object Details : NavigationRoute("details/{stationId}") {
        const val ARG_STATION_ID = "stationId"
        const val KEY_STATION = "station"

        fun createRoute(stationId: String) = "details/$stationId"
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = NavigationRoute.Main.route,
    sharedPlayerViewModel: SharedPlayerViewModel = hiltViewModel()
) {
    val visualizer = SharedVisualizer(sharedPlayerViewModel)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationRoute.Main.route) {
            MainScreen(
                onNavigateToDetails = { station ->
                    navController.navigateToDetails(station)
                },
                visualizer = visualizer,
                sharedPlayerViewModel = sharedPlayerViewModel
            )
        }

        composable(
            route = NavigationRoute.Details.route,
            arguments = listOf(
                navArgument(NavigationRoute.Details.ARG_STATION_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            val station = navController.previousBackStackEntry?.
            savedStateHandle?.get<RadioStation>(NavigationRoute.Details.KEY_STATION)

            station?.let {
                DetailsScreen(
                    station = it,
                    onBackPressed = { navController.popBackStack() },
                    visualizer = visualizer,
                    sharedPlayerViewModel = sharedPlayerViewModel
                )
            }
        }
    }
}

private fun NavHostController.navigateToDetails(station: RadioStation) {
    currentBackStackEntry?.savedStateHandle?.set(
        NavigationRoute.Details.KEY_STATION,
        station
    )
    navigate(NavigationRoute.Details.createRoute(station.stationUuid))
}