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
import app.codeitralf.radiofinder.ui.composables.SharedVisualizer
import app.codeitralf.radiofinder.ui.details.DetailsScreen

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Details : Screen("details/{stationId}") {
        fun createRoute(stationId: String) = "details/$stationId"
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Main.route,
    sharedPlayerViewModel: SharedPlayerViewModel = hiltViewModel()
) {

    val visualizer = SharedVisualizer(sharedPlayerViewModel);

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
                visualizer = visualizer,
                sharedPlayerViewModel = sharedPlayerViewModel
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
                    visualizer = visualizer,
                    sharedPlayerViewModel = sharedPlayerViewModel
                )
            }
        }
    }
}