package com.example.radiofinder.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.radiofinder.services.FFTAudioProcessor

@UnstableApi
class ExoVisualizer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Player.Listener, FFTAudioProcessor.FFTListener {

    var processor: FFTAudioProcessor? = null

    private var currentWaveform: FloatArray? = null

    private val bandView = FFTBandView(context, attrs)

    init {
        addView(bandView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun updateProcessorListenerState(enable: Boolean) {
        if (processor == null) return

        if (enable) {
            if (processor?.listener == null) {
                processor?.listener = this
            }
        } else {
            processor?.listener = null
            currentWaveform = null
        }
    }

    fun setColor(fillColor: Int, bandsColor: Int, avgColor: Int, pathColor: Int) {
        bandView.setColor(fillColor, bandsColor, avgColor, pathColor)
    }

    override fun onFFTReady(sampleRateHz: Int, channelCount: Int, fft: FloatArray) {
        currentWaveform = fft
        bandView.onFFT(fft)
    }
}
