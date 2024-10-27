package app.codeitralf.radiofinder.ui.common

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.navigation.SharedPlayerViewModel
import app.codeitralf.radiofinder.ui.common.exoVisualizer.ExoVisualizer
import javax.inject.Inject


@UnstableApi
class SharedVisualizer @Inject constructor(
    private val viewModel: SharedPlayerViewModel
) {
    private var activeVisualizer: ExoVisualizer? = null

    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        targetStation: RadioStation?,
    ) {
        val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
        val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
        val processor by viewModel.processor.collectAsStateWithLifecycle()

        // Capture colors from the theme
        val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
        val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
        val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
        val tertiaryColor = MaterialTheme.colorScheme.tertiary.toArgb()

        val shouldEnable = isPlaying && currentStation?.stationUuid == targetStation?.stationUuid

        Log.d("SharedVisualizer", "Invoke - currentStation: ${currentStation?.stationUuid}, " +
                "targetStation: ${targetStation?.stationUuid}, isPlaying: $isPlaying, " +
                "shouldEnable: $shouldEnable")

        AndroidView(
            modifier = modifier,
            factory = { context ->
                ExoVisualizer(context).apply {
                    this.processor = processor
                    setColor(
                        surfaceColor,
                        primaryColor,
                        secondaryColor,
                        tertiaryColor
                    )
                    updateProcessorListenerState(shouldEnable)
                    activeVisualizer = this
                }
            },
            update = { view ->
                view.processor = processor
                view.updateProcessorListenerState(shouldEnable)
                view.setColor(
                    surfaceColor,
                    primaryColor,
                    secondaryColor,
                    tertiaryColor
                )
            }
        )
    }
}