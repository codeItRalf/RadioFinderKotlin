package app.codeitralf.radiofinder.views.exoVisualizer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.services.FFTAudioProcessor

@UnstableApi
class ExoVisualizer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), FFTAudioProcessor.FFTListener {

    var processor: FFTAudioProcessor? = null
        set(value) {
            Log.d("ExoVisualizer", "Setting processor: ${value != null}")
            if (field != value) {
                field = value
                applyListenerState()
            }
        }

    private var isListenerEnabled = false
        set(value) {
            if (field != value) {
                field = value
                applyListenerState()
            }
        }

    private val bandView = FFTBandView(context, attrs)

    init {
        addView(bandView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun applyListenerState() {
        Log.d("ExoVisualizer", "Applying listener state: enabled=$isListenerEnabled, hasProcessor=${processor != null}")
        if (isListenerEnabled && processor?.listener != this) {
            processor?.listener = this
            Log.d("ExoVisualizer", "Listener set")
        } else if (!isListenerEnabled && processor?.listener == this) {
            processor?.listener = null
            Log.d("ExoVisualizer", "Listener removed")
        }
    }

    fun updateProcessorListenerState(enable: Boolean) {
        Log.d("ExoVisualizer", "Updating listener state to: $enable, processor: ${processor != null}")
        isListenerEnabled = enable
    }

    override fun onFFTReady(sampleRateHz: Int, channelCount: Int, fft: FloatArray) {
        if (!isListenerEnabled) return
        bandView.onFFT(fft)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d("ExoVisualizer", "Detached from window")
    }

    fun setColor(fillColor: Int, bandsColor: Int, avgColor: Int, pathColor: Int) {
        bandView.setColor(fillColor, bandsColor, avgColor, pathColor)
    }
}