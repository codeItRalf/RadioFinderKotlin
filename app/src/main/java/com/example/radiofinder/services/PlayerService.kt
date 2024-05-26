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
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.radiofinder.R
import com.example.radiofinder.ui.main.MainActivity

class PlayerService : Service() {

    private val binder = PlayerBinder()
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this))
            .build()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    fun play(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        startForeground(NOTIFICATION_ID, createNotification("Playing Radio", true))
    }

    fun pause() {
        exoPlayer.pause()
        updateNotification("Radio Paused", false)
    }

    fun stopMedia() {
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
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playPauseAction = if (isPlaying) {
            val pauseIntent = Intent(this, PlayerService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
        } else {
            val playIntent = Intent(this, PlayerService::class.java).apply {
                action = ACTION_PLAY
            }
            val playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
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
            .setContentTitle("Radio Player")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.music_note_beamed)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .build()
    }

    private fun updateNotification(contentText: String, isPlaying: Boolean) {
        val notification = createNotification(contentText, isPlaying)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                    play(mediaUrl)
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
}
