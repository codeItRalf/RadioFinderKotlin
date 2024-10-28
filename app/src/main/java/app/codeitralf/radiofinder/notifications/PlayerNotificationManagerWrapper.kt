package app.codeitralf.radiofinder.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import app.codeitralf.radiofinder.MainActivity
import app.codeitralf.radiofinder.R
import app.codeitralf.radiofinder.data.model.RadioStation
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class PlayerNotificationManagerWrapper(
    private val context: Context,
    private val exoPlayer: Player,
    private val getCurrentStation: () -> RadioStation?,
    private val stopMedia: () -> Unit
) {
    var isInForeground = false
    private lateinit var playerNotificationManager: PlayerNotificationManager

    companion object {
        private const val CHANNEL_ID = "media_playback_channel"
        private const val CHANNEL_NAME = "Media Playback"
        private const val NOTIFICATION_ID = 1
        private const val ICON_SIZE = 144
        private const val LOADING_TEXT = "Loading..."
        private const val UNKNOWN_STATION = "Unknown Station"
    }

    init {
        createNotificationChannel()
        initializeNotificationManager()
        showInitialNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun showInitialNotification() {
        val notification = createInitialNotification()
        (context as MediaSessionService).startForeground(NOTIFICATION_ID, notification)
        isInForeground = true
    }

    private fun createInitialNotification(): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getCurrentStation()?.name ?: UNKNOWN_STATION)
            .setContentText(LOADING_TEXT)
            .setSmallIcon(R.drawable.icon)
            .setOngoing(true)
            .build()

    private fun initializeNotificationManager() {
        val mediaDescriptionAdapter = createMediaDescriptionAdapter()
        playerNotificationManager = createPlayerNotificationManager(mediaDescriptionAdapter)
        playerNotificationManager.setPlayer(exoPlayer)
    }

    private fun createMediaDescriptionAdapter() =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): String =
                getCurrentStation()?.name ?: UNKNOWN_STATION

            override fun createCurrentContentIntent(player: Player): PendingIntent? =
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

            override fun getCurrentContentText(player: Player): String =
                if (player.isPlaying) "Playing" else "Paused"

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                getCurrentStation()?.favicon?.let { favicon ->
                    loadStationIcon(favicon, callback)
                }
                return null
            }
        }

    private fun createPlayerNotificationManager(
        mediaDescriptionAdapter: PlayerNotificationManager.MediaDescriptionAdapter
    ) = PlayerNotificationManager.Builder(context, NOTIFICATION_ID, CHANNEL_ID)
        .setMediaDescriptionAdapter(mediaDescriptionAdapter)
        .setNotificationListener(createNotificationListener())
        .build()

    private fun createNotificationListener() =
        object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                if (ongoing && getCurrentStation() != null && !isInForeground) {
                    (context as MediaSessionService).startForeground(notificationId, notification)
                    isInForeground = true
                }
            }

            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean
            ) {
                stopMedia()
            }
        }

    private fun loadStationIcon(url: String, callback: PlayerNotificationManager.BitmapCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(ICON_SIZE, ICON_SIZE)
                    .allowHardware(false)
                    .build()

                val bitmap = (ImageLoader(context).execute(request).drawable as? BitmapDrawable)?.bitmap
                bitmap?.let {
                    withContext(Dispatchers.Main) {
                        callback.onBitmap(it)
                    }
                }
            } catch (e: Exception) {
                // Silently fail if icon loading fails
            }
        }
    }
}