package app.codeitralf.radiofinder.navigation

import MainScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.ui.composables.sharedVisualizer.SharedVisualizer
import app.codeitralf.radiofinder.ui.details.DetailsScreen

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Details : Screen("details/{stationId}") {
        fun createRoute(stationId: String) = "details/$stationId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Main.route
) {

    val visualizer = SharedVisualizer();

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToDetails = { station ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("station", station)
                    navController.navigate(Screen.Details.createRoute(station.stationUuid))
                },
                visualizer = visualizer
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("stationId") { type = NavType.StringType }
            )
        ) {
            val station = navController.previousBackStackEntry?.savedStateHandle?.get<RadioStation>("station")
            station?.let {
                DetailsScreen(
                    station = it,
                    onBackPressed = { navController.popBackStack() },
                    visualizer = visualizer
                )
            }
        }
    }
}