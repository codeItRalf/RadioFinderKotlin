import androidx.annotation.OptIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.navigation.SharedPlayerViewModel
import app.codeitralf.radiofinder.ui.common.SharedVisualizer
import app.codeitralf.radiofinder.ui.feature.main.MainViewModel


@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToDetails: (RadioStation) -> Unit,
    visualizer: SharedVisualizer,
    sharedPlayerViewModel: SharedPlayerViewModel
) {
    val currentStation by sharedPlayerViewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by sharedPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
    val playerIsLoading by sharedPlayerViewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            RadioAppBar(
                onSearchQueryChanged = viewModel::searchStations
            )
        },
        bottomBar = {
            if (currentStation != null) {
                FloatingPlayerController(
                    station = currentStation,
                    onPlayPauseClick = sharedPlayerViewModel::playPause,
                    onControllerClick = { currentStation?.let(onNavigateToDetails) },
                    visualizer = visualizer,
                    isPlaying = isPlaying,
                    isLoading = playerIsLoading
                )
            }
        }
    ) { paddingValues ->
        MainContent(
            modifier = Modifier.padding(paddingValues),
            isPlayerLoading = playerIsLoading,
            currentStation = currentStation,
            onStationClick = sharedPlayerViewModel::playPause,
            onStationDetailsClick = onNavigateToDetails,
            viewModel = viewModel
        )
    }
}
