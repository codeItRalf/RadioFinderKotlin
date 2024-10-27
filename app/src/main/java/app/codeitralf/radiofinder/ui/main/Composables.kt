
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.ui.composables.RoundedPlayButton
import app.codeitralf.radiofinder.ui.composables.SharedVisualizer
import app.codeitralf.radiofinder.ui.main.MainViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioAppBar(
    onSearchQueryChanged: (String) -> Unit,

    ) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    placeholder = { Text("Search stations...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("Radio Finder")
            }
        },
        actions = {
            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) "Close search" else "Open search"
                )
            }
        }
    )
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    isPlayerLoading: Boolean,
    currentStation: RadioStation?,
    viewModel: MainViewModel,
    onStationClick: (RadioStation) -> Unit,
    onStationDetailsClick: (RadioStation) -> Unit
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
                    isPlaying = station.stationUuid == currentStation?.stationUuid,
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

@Composable
fun StationItem(
    station: RadioStation,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = station.favicon,
                contentDescription = "Station logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name ?: "",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            IconButton(onClick = { onDetailsClick() }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Station details"
                )
            }



            RoundedPlayButton(
                onClick = onClick,
                isPlaying = isPlaying,
                isLoading = isLoading,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun FloatingPlayerController(
    station: RadioStation?,
    onPlayPauseClick: (RadioStation?) -> Unit,
    onControllerClick: () -> Unit,
    modifier: Modifier = Modifier,
    visualizer: SharedVisualizer,
    isPlaying: Boolean,
    isLoading: Boolean
) {
    if (station == null) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        visualizer(
            modifier = Modifier.fillMaxSize(),
            targetStation = station
        )
        Surface(
            modifier = modifier
                .fillMaxSize()
                .clickable(onClick = onControllerClick),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = station.favicon,
                    contentDescription = "Station logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = station.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                RoundedPlayButton(
                    onClick = { onPlayPauseClick(station) },
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}