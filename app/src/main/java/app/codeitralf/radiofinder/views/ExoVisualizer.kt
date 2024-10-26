package app.codeitralf.radiofinder.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.services.FFTAudioProcessor

@UnstableApi
class ExoVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Player.Listener, FFTAudioProcessor.FFTListener {

    var processor: FFTAudioProcessor? = null
        set(value) {
            if (field != value) {
                field?.listener = null  // Remove listener from old processor
                field = value
                if (field != null && enabled) {
                    field?.listener = this  // Set listener only if enabled
                }
            }
        }

    private var enabled = false
    private var currentWaveform: FloatArray? = null
    private val bandView = FFTBandView(context, attrs)

    init {
        addView(bandView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun updateProcessorListenerState(enable: Boolean) {
        enabled = enable
        if (processor == null) return

        if (enable) {
            if (processor?.listener == null) {
                processor?.listener = this
            }
        } else {
            processor?.listener = null
            currentWaveform = null
            bandView.onFFT(FloatArray(0)) // This will trigger clearVisualization
        }
    }


    fun setColor(fillColor: Int, bandsColor: Int, avgColor: Int, pathColor: Int) {
        bandView.setColor(fillColor, bandsColor, avgColor, pathColor)
    }

    override fun onFFTReady(sampleRateHz: Int, channelCount: Int, fft: FloatArray) {
        if (!enabled) return
        currentWaveform = fft
        bandView.onFFT(fft)
    }
}
