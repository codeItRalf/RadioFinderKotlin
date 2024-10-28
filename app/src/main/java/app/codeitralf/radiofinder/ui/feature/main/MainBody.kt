
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.ui.feature.main.MainViewModel
import app.codeitralf.radiofinder.ui.feature.main.components.StationItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged


@Composable
fun MainBody(
    modifier: Modifier = Modifier,
    isPlayerLoading: Boolean,
    currentStation: RadioStation?,
    viewModel: MainViewModel,
    onStationClick: (RadioStation) -> Unit,
    onStationDetailsClick: (RadioStation) -> Unit,
    isPlaying: Boolean
) {

    val stations by viewModel.filteredStations.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()


    val listState = rememberLazyListState()

    // Handle pagination
    LaunchedEffect(Unit) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = stations.size
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // Return pair of current position and if we should load more
            lastVisibleItem to (lastVisibleItem >= totalItemsCount - 5)
        }
            .distinctUntilChanged() // Only emit when the values actually change
            .collectLatest { (_, shouldLoadMore) ->
                if (shouldLoadMore && !isLoading) {
                    viewModel.loadNextPage()
                }
            }
    }


    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
        ) {
            items(items = stations,
                key = { it.stationUuid }) { station ->
                StationItem(
                    station = station,
                    isPlaying = station.stationUuid == currentStation?.stationUuid && isPlaying,
                    onClick = { onStationClick(station) },
                    onDetailsClick = { onStationDetailsClick(station) },
                    isLoading = isPlayerLoading && station.stationUuid == currentStation?.stationUuid
                )
            }
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

            }

        }
    }
}



