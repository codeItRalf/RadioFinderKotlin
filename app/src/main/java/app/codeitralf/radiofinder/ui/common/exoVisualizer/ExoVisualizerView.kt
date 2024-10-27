package app.codeitralf.radiofinder.ui.common.exoVisualizer

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.services.FFTAudioProcessor

class ExoVisualizerState {
    var visualizer by mutableStateOf<ExoVisualizer?>(null)
}

@Composable
fun rememberExoVisualizerState(): ExoVisualizerState {
    return remember { ExoVisualizerState() }
}

@OptIn(UnstableApi::class)
@Composable
fun ExoVisualizerView(
    modifier: Modifier = Modifier,
    processor: FFTAudioProcessor?,
    fillColor: Int,
    bandsColor: Int,
    avgColor: Int,
    pathColor: Int,
    enabled: Boolean = true
) {
    val visualizerState = rememberExoVisualizerState()

    Log.d("ExoVisualizerView", "Composing with processor: ${processor != null}, enabled: $enabled")

    // Add lifecycle logging
    DisposableEffect(Unit) {
        Log.d("ExoVisualizerView", "Entering composition")
        onDispose {
            Log.d("ExoVisualizerView", "Disposing composition")
            visualizerState.visualizer?.updateProcessorListenerState(false)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Log.d("ExoVisualizerView", "Creating new ExoVisualizer")
            ExoVisualizer(context).apply {
                this.processor = processor
                setColor(fillColor, bandsColor, avgColor, pathColor)
                updateProcessorListenerState(enabled)
                visualizerState.visualizer = this
            }
        },
        update = { view ->
            Log.d("ExoVisualizerView", "Updating ExoVisualizer, enabled: $enabled")
            view.processor = processor
            view.updateProcessorListenerState(enabled)
            view.setColor(fillColor, bandsColor, avgColor, pathColor)
        }
    )
}
