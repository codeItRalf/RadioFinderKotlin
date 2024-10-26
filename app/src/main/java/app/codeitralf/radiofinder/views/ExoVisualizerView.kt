import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.services.FFTAudioProcessor
import app.codeitralf.radiofinder.views.ExoVisualizer

@Composable
fun rememberExoVisualizerState(): ExoVisualizerState {
    return remember { ExoVisualizerState() }
}

class ExoVisualizerState {
    var visualizer by mutableStateOf<ExoVisualizer?>(null)
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


    // Ensure immediate update when enabled state changes
    LaunchedEffect(processor, enabled) {
        visualizerState.visualizer?.let { visualizer ->
            visualizer.processor = processor
            visualizer.updateProcessorListenerState(enabled)
        }
    }

    DisposableEffect(processor, enabled) {
        onDispose {
            visualizerState.visualizer?.updateProcessorListenerState(false)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ExoVisualizer(context).apply {
                this.processor = processor
                setColor(fillColor, bandsColor, avgColor, pathColor)
                updateProcessorListenerState(enabled)
                visualizerState.visualizer = this
            }
        },
        update = { view ->
            view.processor = processor
            view.updateProcessorListenerState(enabled)
        }
    )
}