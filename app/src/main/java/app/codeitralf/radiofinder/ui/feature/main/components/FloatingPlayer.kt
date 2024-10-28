package app.codeitralf.radiofinder.ui.feature.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.ui.common.RoundedPlayButton
import app.codeitralf.radiofinder.ui.common.exoVisualizer.SharedVisualizer
import coil.compose.AsyncImage

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