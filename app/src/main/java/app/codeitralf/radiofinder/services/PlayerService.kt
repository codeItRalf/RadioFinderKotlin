package app.codeitralf.radiofinder.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.notifications.PlayerNotificationManagerWrapper
import app.codeitralf.radiofinder.utils.PlayerInitializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class PlayerService : MediaSessionService() {

    // Binder given to clients
    private val binder = PlayerBinder()

    // Media session and player
    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer

    // Notification manager
    private lateinit var playerNotificationManagerWrapper: PlayerNotificationManagerWrapper

    // LiveData for current station, loading and playing state
    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?>  = _currentStation.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(value = false)
    val isLoading: StateFlow<Boolean>  =  _isLoading.asStateFlow()

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying: StateFlow<Boolean>  = _isPlaying.asStateFlow()

    // Audio processor for visualization
    private val _fftAudioProcessor = FFTAudioProcessor()

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        playerNotificationManagerWrapper = PlayerNotificationManagerWrapper(
            this,
            exoPlayer,
            { _currentStation.value },
            { stopMedia() }
        )

    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.let { player ->
            if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
                stopSelf()
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            exoPlayer.removeListener(createPlayerListener())
            exoPlayer.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun initializePlayer() {
        PlayerInitializer.initializePlayer(this, this, { _currentStation.value }) { exoPlayer, mediaSession ->
            this.exoPlayer = exoPlayer
            this.mediaSession = mediaSession
        }
    }

    fun createPlayerListener(): Player.Listener {
        return object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isLoading.value = playbackState == Player.STATE_BUFFERING
            }
        }
    }

    fun playPause(station: RadioStation?) {
        if (station == null || station == _currentStation.value) {
            if (_isPlaying.value) {
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
            stopMedia()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun stopMedia() {
        exoPlayer.stop()
        if(playerNotificationManagerWrapper.isInForeground){
            stopForeground(STOP_FOREGROUND_REMOVE)
            playerNotificationManagerWrapper.isInForeground = false
        }
        _currentStation.value = null
    }

    fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    // Binder class to return the service instance to clients
    inner class PlayerBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    // Accessor methods for LiveData
    fun getStation(): RadioStation? {
        return _currentStation.value
    }

    fun getAudioProcessor(): FFTAudioProcessor {
        return _fftAudioProcessor
    }
}
