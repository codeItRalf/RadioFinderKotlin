package com.example.radiofinder.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.radiofinder.data.model.RadioStation

@UnstableApi
class PlayerService : Service() {

    private val binder = PlayerBinder()
    private val notificationHandler: NotificationHandler = NotificationHandler(this)
    private lateinit var exoPlayer: ExoPlayer
    private val _currentStation = MutableLiveData<RadioStation?>()
    val currentStation: LiveData<RadioStation?> get() = _currentStation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying
    private val _fftAudioProcessor = FFTAudioProcessor()


    override fun onCreate() {
        super.onCreate()
        val context = this



        val renderersFactory = object : DefaultRenderersFactory(context) {

            // Create a DefaultAudioProcessorChain
            val audioProcessorChain = DefaultAudioSink.DefaultAudioProcessorChain(
                _fftAudioProcessor,

            )
            // Create a Builder object for DefaultAudioSink
            val audioSinkBuilder = DefaultAudioSink.Builder()
                .setAudioProcessorChain(
                    audioProcessorChain
                )

            // Create a DefaultAudioSink object
            val defaultAudioSink = audioSinkBuilder.build()


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

        exoPlayer = ExoPlayer.Builder(context, renderersFactory)
            .build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if(playbackState == Player.STATE_BUFFERING) {
                    _isLoading.postValue(true)
                }else{
                    _isLoading.postValue(false)
                }

            }

        })


        startForeground(
            NOTIFICATION_ID,
            notificationHandler.createNotification("Radio Player", false, _currentStation.value)
        )
    }


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }


    fun playPause(station: RadioStation?) {
        if (station == null || station == _currentStation.value) {
            if (_isPlaying.value == true) {
                exoPlayer.pause()
                notificationHandler.updateNotification("Paused", false, _currentStation.value)
            } else {
                exoPlayer.play()
                notificationHandler.updateNotification("Playing", true, _currentStation.value)
            }
        } else {
            _currentStation.value = station
            try {


                val mediaSource = ProgressiveMediaSource.Factory(
                    DefaultDataSourceFactory(this, "ExoVisualizer")
                ).createMediaSource(MediaItem.Builder().setUri(station.resolvedUrl!!).build())
//                val mediaItem = MediaItem.fromUri(station.resolvedUrl!!)
//                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.play()
                notificationHandler.updateNotification("Playing", true, _currentStation.value)
            } catch (e: Exception) {
                Log.e("PlayerService", "Error playing station", e)
                exoPlayer.stop()
                _currentStation.value = null
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


     fun stopMedia() {
        exoPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    fun setVolume(volume: Float) {
        exoPlayer.volume = volume
    }

    inner class PlayerBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }


    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.radiofinder.ACTION_PLAY_PAUSE"
        const val ACTION_STOP = "com.example.radiofinder.ACTION_STOP"
        const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val mediaUrl = intent?.getStringExtra("media_url")

        when (action) {
            ACTION_PLAY_PAUSE -> {
                if (mediaUrl != null) {
                    playPause(_currentStation.value!!)
                } else {
                    playPause(_currentStation.value!!)
                }
            }

            ACTION_STOP -> {
                stopMedia()
            }
        }

        return START_STICKY
    }

    fun getStation(): RadioStation? {
        return _currentStation.value
    }

    fun getAudioProcessor(): FFTAudioProcessor {
        return _fftAudioProcessor
    }


}
