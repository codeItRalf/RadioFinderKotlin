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

    companion object {
        private const val TAG = "PlayerService"
    }

    // Service state
    private val binder = PlayerBinder()
    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerNotificationManagerWrapper: PlayerNotificationManagerWrapper
    private val fftAudioProcessor = FFTAudioProcessor()

    // State flows
    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeNotificationManager()
    }

    private fun initializePlayer() {
        PlayerInitializer.initializePlayer(
            context = this,
            service = this,
            currentStation = { _currentStation.value }
        ) { exoPlayer, mediaSession ->
            this.exoPlayer = exoPlayer
            this.mediaSession = mediaSession
        }
    }

    private fun initializeNotificationManager() {
        playerNotificationManagerWrapper = PlayerNotificationManagerWrapper(
            context = this,
            exoPlayer = exoPlayer,
            getCurrentStation = { _currentStation.value },
            stopMedia = { stopMedia() }
        )
    }

    fun createPlayerListener() = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _isLoading.value = playbackState == Player.STATE_BUFFERING
        }
    }

    fun playPause(station: RadioStation?) {
        when {
            station == null || station == _currentStation.value -> togglePlayback()
            else -> playNewStation(station)
        }
    }

    private fun togglePlayback() {
        if (_isPlaying.value) exoPlayer.pause() else exoPlayer.play()
    }

    private fun playNewStation(station: RadioStation) {
        _currentStation.value = station
        try {
            station.resolvedUrl?.let { url ->
                exoPlayer.setMediaItem(MediaItem.fromUri(url))
                exoPlayer.prepare()
                exoPlayer.play()
            }
        } catch (e: Exception) {
            handlePlaybackError(e)
        }
    }

    private fun handlePlaybackError(error: Exception) {
        Log.e(TAG, "Error playing station", error)
        stopMedia()
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
    }

    fun stopMedia() {
        exoPlayer.stop()
        if (playerNotificationManagerWrapper.isInForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            playerNotificationManagerWrapper.isInForeground = false
        }
        _currentStation.value = null
    }

    fun getAudioProcessor(): FFTAudioProcessor = fftAudioProcessor

    // Service lifecycle methods
    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.let { player ->
            if (!player.playWhenReady ||
                player.mediaItemCount == 0 ||
                player.playbackState == Player.STATE_ENDED
            ) {
                stopSelf()
            }
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

    inner class PlayerBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }
}