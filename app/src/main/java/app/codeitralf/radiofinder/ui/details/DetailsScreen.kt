package app.codeitralf.radiofinder.ui.details

import ExoVisualizerView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.model.StationCheck
import coil.compose.AsyncImage

@ExperimentalMaterial3Api
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DetailsScreen(
    station: RadioStation,
    onBackPressed: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val stationChecks by viewModel.stationChecks.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val processor by viewModel.processor.collectAsStateWithLifecycle()


    // Ensure cleanup when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanupVisualizer()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = station.name ?: "N/A") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            if (currentStation != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    // Visualizer background
                    ExoVisualizerView(
                        modifier = Modifier.fillMaxSize(),
                        processor = processor,
                        fillColor = MaterialTheme.colorScheme.surface.toArgb(),
                        bandsColor = MaterialTheme.colorScheme.primary.toArgb(),
                        avgColor = MaterialTheme.colorScheme.secondary.toArgb(),
                        pathColor = MaterialTheme.colorScheme.tertiary.toArgb(),
                        enabled = isPlaying && currentStation?.stationUuid == station.stationUuid
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Station Image
                AsyncImage(
                    model = station.favicon,
                    contentDescription = "Station logo",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )

                // Play Button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.playPause(station) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                if (isPlaying && currentStation?.stationUuid == station.stationUuid)
                                    Icons.Default.Close
                                else
                                    Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                        }
                    }
                }

                // Station Details
                StationDetails(
                    station = station,
                    stationCheck = stationChecks.firstOrNull()
                )
            }

            // Loading Indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun StationDetails(
    station: RadioStation,
    stationCheck: StationCheck?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailItem("Country", station.country ?: "N/A")
        DetailItem("Tags", stationCheck?.tags ?: station.tags ?: "N/A")
        DetailItem("Bitrate", "${stationCheck?.bitrate ?: station.bitrate ?: "N/A"} kbps")
        DetailItem("Language", stationCheck?.languageCodes ?: station.language ?: "N/A")
        DetailItem("Votes", station.votes?.toString() ?: "N/A")
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}