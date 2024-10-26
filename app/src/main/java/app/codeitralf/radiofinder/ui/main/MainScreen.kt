import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.codeitralf.radiofinder.ui.main.MainViewModel


@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
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
                    isPlaying = isPlaying,
                    onPlayPauseClick = viewModel::playPause,
                    onControllerClick = { /* Navigate to details */ }
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
            onStationDetailsClick = { /* Navigate to details */ }
        )
    }
}
