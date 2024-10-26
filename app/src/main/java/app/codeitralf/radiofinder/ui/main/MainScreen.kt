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
import app.codeitralf.radiofinder.ui.composables.sharedVisualizer.SharedVisualizer
import app.codeitralf.radiofinder.ui.main.MainViewModel


@OptIn(UnstableApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToDetails: (RadioStation) -> Unit,
    visualizer: SharedVisualizer

) {
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

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
                    onPlayPauseClick = viewModel::playPause,
                    onControllerClick = { currentStation?.let(onNavigateToDetails) },
                    visualizer = visualizer,
                    isPlaying = isPlaying
                )
            }
        }
    ) { paddingValues ->
        MainContent(
            modifier = Modifier.padding(paddingValues),
            stations = stations,
            isLoading = isLoading,
            currentStation = currentStation,
            onStationClick = viewModel::playPause,
            onStationDetailsClick = onNavigateToDetails
        )
    }
}
