package app.codeitralf.radiofinder.utils

import android.content.Context
import android.os.Handler
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import app.codeitralf.radiofinder.services.FFTAudioProcessor

object RenderersFactoryUtil {

    fun createRenderersFactory(context: Context, fftAudioProcessor: FFTAudioProcessor): DefaultRenderersFactory {
        return object : DefaultRenderersFactory(context) {
            override fun buildAudioRenderers(
                context: Context,
                extensionRendererMode: Int,
                mediaCodecSelector: MediaCodecSelector,
                enableDecoderFallback: Boolean,
                audioSink: AudioSink,
                eventHandler: Handler,
                eventListener: AudioRendererEventListener,
                out: ArrayList<Renderer>
            ) {
                val audioProcessorChain =
                    DefaultAudioSink.DefaultAudioProcessorChain(fftAudioProcessor)
                val defaultAudioSink = DefaultAudioSink.Builder()
                    .setAudioProcessorChain(audioProcessorChain)
                    .build()

                out.add(
                    MediaCodecAudioRenderer(
                        context,
                        mediaCodecSelector,
                        enableDecoderFallback,
                        eventHandler,
                        eventListener,
                        defaultAudioSink
                    )
                )

                super.buildAudioRenderers(
                    context,
                    extensionRendererMode,
                    mediaCodecSelector,
                    enableDecoderFallback,
                    audioSink,
                    eventHandler,
                    eventListener,
                    out
                )
            }
        }
    }
}