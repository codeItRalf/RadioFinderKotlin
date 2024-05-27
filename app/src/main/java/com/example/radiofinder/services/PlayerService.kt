package com.example.radiofinder.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.radiofinder.R
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.ui.main.MainActivity

class PlayerService : Service() {

    private val binder = PlayerBinder()
    private lateinit var exoPlayer: ExoPlayer
    private val _currentStation = MutableLiveData<RadioStation?>()
    val currentStation: LiveData<RadioStation?> get() = _currentStation

    private  val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this))
            .build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue(isPlaying)
            }
        })
        startForeground(NOTIFICATION_ID, createNotification("Radio Player", false))
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    fun play(station: RadioStation) {
        _currentStation.value = station
        val mediaItem = MediaItem.fromUri(station.resolvedUrl!!)

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        updateNotification("Playing", true)
    }


    fun pause() {
        exoPlayer.pause()
        updateNotification("Paused", false)
    }


    private fun stopMedia() {
        exoPlayer.stop()
        stopForeground(true)
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

    private fun createNotification(contentText: String, isPlaying: Boolean): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            val pauseIntent = Intent(this, PlayerService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent =
                PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                pausePendingIntent
            )
        } else {
            val playIntent = Intent(this, PlayerService::class.java).apply {
                action = ACTION_PLAY
            }
            val playPendingIntent =
                PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", playPendingIntent)
        }


        val channelId = "radio_channel_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Radio Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(currentStation.value?.name ?: "")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .setOngoing(isPlaying)
            .build()
    }

    private fun updateNotification(contentText: String, isPlaying: Boolean) {
        val notification = createNotification(contentText, isPlaying)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_PLAY = "com.example.radiofinder.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.radiofinder.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.radiofinder.ACTION_STOP"
        const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val mediaUrl = intent?.getStringExtra("media_url")

        when (action) {
            ACTION_PLAY -> {
                if (mediaUrl != null) {
                    play(_currentStation.value!!)
                } else if (!exoPlayer.isPlaying) {
                    exoPlayer.play()
                    updateNotification("Playing Radio", true)
                }
            }

            ACTION_PAUSE -> {
                pause()
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
}
