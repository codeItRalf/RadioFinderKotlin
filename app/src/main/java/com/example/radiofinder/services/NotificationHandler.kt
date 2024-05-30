package com.example.radiofinder.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import com.example.radiofinder.R
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.ui.main.MainActivity

class NotificationHandler(private val context: Context) {

    private val channelId = "radio_channel_id"

    init {
        createNotificationChannel()
    }

    @OptIn(UnstableApi::class)
    fun createNotification(contentText: String, isPlaying: Boolean, currentStation: RadioStation?): Notification {
        val pendingIntent = createPendingIntent()
        val playPauseAction = createPlayPauseAction(isPlaying)

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(currentStation?.name ?: "")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .setOngoing(isPlaying)
            .build()
    }

    @OptIn(UnstableApi::class)
    fun updateNotification(contentText: String, isPlaying: Boolean, currentStation: RadioStation?) {
        val notification = createNotification(contentText, isPlaying, currentStation)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(PlayerService.NOTIFICATION_ID, notification)
    }

    @OptIn(UnstableApi::class)
    private fun createPendingIntent(): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    @OptIn(UnstableApi::class)
    private fun createPlayPauseAction(isPlaying: Boolean): NotificationCompat.Action {
        val actionIntent = Intent(context, PlayerService::class.java).apply {
            action = PlayerService.ACTION_PLAY_PAUSE
        }
        val actionPendingIntent = PendingIntent.getService(context, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE)
        return if (isPlaying) {
            NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", actionPendingIntent)
        } else {
            NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", actionPendingIntent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Radio Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
