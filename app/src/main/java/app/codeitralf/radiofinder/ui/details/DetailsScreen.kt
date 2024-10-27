package app.codeitralf.radiofinder.ui.details

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.model.StationCheck
import app.codeitralf.radiofinder.navigation.SharedPlayerViewModel
import app.codeitralf.radiofinder.ui.composables.RoundedPlayButton
import app.codeitralf.radiofinder.ui.composables.SharedVisualizer
import coil.compose.AsyncImage

@ExperimentalMaterial3Api
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun DetailsScreen(
    station: RadioStation,
    onBackPressed: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
    visualizer: SharedVisualizer,
    sharedPlayerViewModel: SharedPlayerViewModel
) {
    val stationChecks by viewModel.stationChecks.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentStation by sharedPlayerViewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by sharedPlayerViewModel.isPlaying.collectAsStateWithLifecycle()
    val playerIsLoading by sharedPlayerViewModel.isLoading.collectAsStateWithLifecycle()

    viewModel.getStationCheck(station.stationUuid)

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
            Log.d("DetailsScreen", "Rendering visualizer for station: ${station.stationUuid}")
            if (currentStation != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    visualizer(
                        modifier = Modifier.fillMaxSize(),
                        targetStation = station
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues).background(Color.Black)
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

                    RoundedPlayButton(
                        onClick = { sharedPlayerViewModel.playPause(station) },
                        isPlaying =  isPlaying && currentStation?.stationUuid == station.stationUuid,
                        isLoading = playerIsLoading,
                        modifier = Modifier.size(70.dp)
                    )


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