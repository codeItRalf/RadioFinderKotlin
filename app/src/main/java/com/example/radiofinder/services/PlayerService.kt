package com.example.radiofinder.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.ui.main.MainActivity
import com.squareup.picasso.Picasso

@UnstableApi
class PlayerService : MediaSessionService() {
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private val binder = PlayerBinder()
    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer
    private val _currentStation = MutableLiveData<RadioStation?>()
    val currentStation: LiveData<RadioStation?> get() = _currentStation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying
    private val _fftAudioProcessor = FFTAudioProcessor()
    private var _isInForeground = false;

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializePlayer()
        initializeNotificationManager()
    }


    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED
        ) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            exoPlayer.removeListener(createPlayerListener())
            exoPlayer.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun initializeNotificationManager() {
        val context = this
        val mediaDescriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): String {
                return _currentStation.value?.name ?: "Unknown Station"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(context, MainActivity::class.java)
                return PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            override fun getCurrentContentText(player: Player): String? {
                return if (player.isPlaying)  "Playing" else "Paused"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                val faviconUrl = _currentStation.value?.favicon
                if (faviconUrl != null) {
                    Picasso.get()
                        .load(faviconUrl)
                        .into(object : com.squareup.picasso.Target {
                            override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                                callback.onBitmap(bitmap)
                            }

                            override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                            }
                        })
                }
                return null
            }
        }

        playerNotificationManager =
            PlayerNotificationManager.Builder(context, NOTIFICATION_ID, CHANNEL_ID)
                .setMediaDescriptionAdapter(mediaDescriptionAdapter)
                .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                    override fun onNotificationPosted(
                        notificationId: Int,
                        notification: Notification,
                        ongoing: Boolean
                    ) {
                        if (ongoing && !_isInForeground && _currentStation.value != null) {
                            startForeground(notificationId, notification)
                            _isInForeground = true
                        }
                    }

                    override fun onNotificationCancelled(
                        notificationId: Int,
                        dismissedByUser: Boolean
                    ) {
                       stopMedia()
                    }
                })

                .build()

        playerNotificationManager.setPlayer(exoPlayer)
    }



    private fun initializePlayer() {
        val context = this
        val sessionToken =
            SessionToken(context, ComponentName(context, PlayerService::class.java))
        val renderersFactory = createRenderersFactory(context)
        exoPlayer = ExoPlayer.Builder(context, renderersFactory).build()
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
        exoPlayer.addListener(createPlayerListener())
        val controllerFuture =
            MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            val mediaController = controllerFuture.get()
            val mediaItem =
                MediaItem.Builder()
                    .setMediaId(_currentStation.value?.stationUuid ?: "")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(_currentStation.value?.name ?: "")
                            .build()
                    )
                    .build()
            mediaController.setMediaItem(mediaItem)
        }, ContextCompat.getMainExecutor(context))

    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createRenderersFactory(context: Context): DefaultRenderersFactory {
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
                    DefaultAudioSink.DefaultAudioProcessorChain(_fftAudioProcessor)
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


    private fun createPlayerListener(): Player.Listener {
        return object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isLoading.postValue(playbackState == Player.STATE_BUFFERING)
            }
        }
    }

    fun playPause(station: RadioStation?) {
        if (station == null || station == _currentStation.value) {
            if (_isPlaying.value == true) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        } else {
            playNewStation(station)
        }
    }

    private fun playNewStation(station: RadioStation) {
        _currentStation.value = station
        try {
            val mediaItem = MediaItem.fromUri(station.resolvedUrl!!)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            Log.e("PlayerService", "Error playing station", e)
            exoPlayer.stop()
            _currentStation.value = null
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun stopMedia() {
        exoPlayer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        _isInForeground = false
        _currentStation.value = null
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


    fun getStation(): RadioStation? {
        return _currentStation.value
    }

    fun getAudioProcessor(): FFTAudioProcessor {
        return _fftAudioProcessor
    }

    companion object {
        private const val CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}
