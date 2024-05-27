package com.example.radiofinder.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.radiofinder.R
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.ui.main.MainActivity

class NotificationHandler(private val context: Context) {
    fun createNotification(contentText: String, isPlaying: Boolean, currentStation: RadioStation?): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            val pauseIntent = Intent(context, PlayerService::class.java).apply {
                action = PlayerService.ACTION_PLAY_PAUSE
            }
            val pausePendingIntent =
                PendingIntent.getService(context, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                pausePendingIntent
            )
        } else {
            val playIntent = Intent(context, PlayerService::class.java).apply {
                action = PlayerService.ACTION_PLAY_PAUSE
            }
            val playPendingIntent =
                PendingIntent.getService(context, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)
            NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", playPendingIntent)
        }

        val channelId = "radio_channel_id"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Radio Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(currentStation?.name ?: "")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .setOngoing(isPlaying)
            .build()
    }

    fun updateNotification(contentText: String, isPlaying: Boolean, currentStation: RadioStation?) {
        val notification = createNotification(contentText, isPlaying, currentStation)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(PlayerService.NOTIFICATION_ID, notification)
    }
}