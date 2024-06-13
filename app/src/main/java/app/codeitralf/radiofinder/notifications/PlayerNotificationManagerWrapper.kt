package app.codeitralf.radiofinder.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.ui.main.MainActivity
import com.squareup.picasso.Picasso

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
                        if (ongoing && !isInForeground && getCurrentStation() != null) {
                            startForeground(notificationId, notification)
                            isInForeground = true
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

    private fun startForeground(notificationId: Int, notification: Notification) {
        (context as MediaSessionService). startForeground(notificationId, notification)
    }

    companion object {
        private const val CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}
