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
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.ui.main.MainActivity
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

    init {
        createNotificationChannel()
        initializeNotificationManager()

        showInitialNotification()
    }

    // Add this method
    private fun showInitialNotification() {
        val notification = createInitialNotification()
        (context as MediaSessionService).startForeground(NOTIFICATION_ID, notification)
        isInForeground = true
    }

    // Add this method
    private fun createInitialNotification(): Notification {
        return androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getCurrentStation()?.name ?: "Radio Player")
            .setContentText("Loading...")
            .setSmallIcon(app.codeitralf.radiofinder.R.drawable.icon) // Make sure you have this icon
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun initializeNotificationManager() {
        val mediaDescriptionAdapter = object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): String {
                return getCurrentStation()?.name ?: "Unknown Station"
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
                return if (player.isPlaying) "Playing" else "Paused"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                val faviconUrl = getCurrentStation()?.favicon
                if (faviconUrl != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        loadBitmap(faviconUrl)?.let { bitmap ->
                            withContext(Dispatchers.Main) {
                                callback.onBitmap(bitmap)
                            }
                        }
                    }
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
                        if (ongoing && getCurrentStation() != null) {
                            // Update the notification instead of starting foreground
                            if (!isInForeground) {
                                (context as MediaSessionService).startForeground(notificationId, notification)
                                isInForeground = true
                            }
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

    private suspend fun loadBitmap(url: String): Bitmap? {
        return try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(144, 144)  // Recommended size for notification icons
                .allowHardware(false)  // Required for getting Bitmap
                .build()

            val result = (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
            result
        } catch (e: Exception) {
            null
        }
    }


    companion object {
        private const val CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}
