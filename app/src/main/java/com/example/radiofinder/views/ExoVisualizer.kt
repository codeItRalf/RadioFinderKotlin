package com.example.radiofinder.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.radiofinder.services.FFTAudioProcessor

/**
 * The visualizer is a view which listens to the FFT changes and forwards it to the band view.
 */
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
        Log.d("ExoVisualizer", "Updating processor listener state: $enable, processor is null: ${processor == null}, listener is null: ${processor?.listener == null}")
        if (enable) {
            processor?.listener = this
        } else {
            processor?.listener = null
            currentWaveform = null
        }
    }



    override fun onFFTReady(sampleRateHz: Int, channelCount: Int, fft: FloatArray) {
        currentWaveform = fft
        bandView.onFFT(fft)
        Log.d("ExoVisualizer", "FFT data received: ${fft.contentToString()}")
    }

}