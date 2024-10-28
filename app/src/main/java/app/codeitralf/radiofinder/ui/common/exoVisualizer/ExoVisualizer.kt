package app.codeitralf.radiofinder.ui.common.exoVisualizer

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.services.FFTAudioProcessor

@UnstableApi
class ExoVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), FFTAudioProcessor.FFTListener {

    companion object {
        private const val TAG = "ExoVisualizer"
    }

    private val bandView = FFTBandView(context, attrs).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private var isListenerEnabled = false
        set(value) {
            if (field != value) {
                field = value
                updateListener()
            }
        }

    var processor: FFTAudioProcessor? = null
        set(value) {
            logDebug("Setting processor: ${value != null}")
            if (field != value) {
                field = value
                updateListener()
            }
        }

    init {
        addView(bandView)
    }

    override fun onFFTReady(sampleRateHz: Int, channelCount: Int, fft: FloatArray) {
        if (isListenerEnabled) {
            bandView.onFFT(fft)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        logDebug("Detached from window")
        disableListener()
    }

    fun updateProcessorListenerState(enable: Boolean) {
        logDebug("Updating listener state to: $enable, processor: ${processor != null}")
        isListenerEnabled = enable
    }

    fun setColor(
        fillColor: Int,
        bandsColor: Int,
        avgColor: Int,
        pathColor: Int
    ) {
        bandView.setColor(
            fillColor = fillColor,
            bandsColor = bandsColor,
            avgColor = avgColor,
            pathColor = pathColor
        )
    }

    private fun updateListener() {
        logDebug("Updating listener: enabled=$isListenerEnabled, hasProcessor=${processor != null}")

        processor?.let { proc ->
            if (isListenerEnabled && proc.listener != this) {
                proc.listener = this
                logDebug("Listener enabled")
            } else if (!isListenerEnabled && proc.listener == this) {
                proc.listener = null
                logDebug("Listener disabled")
            }
        }
    }

    private fun disableListener() {
        isListenerEnabled = false
        processor?.listener = null
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }
}