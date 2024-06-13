package app.codeitralf.radiofinder.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.OptIn
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.ui.main.MainActivity

class NotificationPermissionHelper(private val context: Context) {

    @OptIn(UnstableApi::class)
    fun checkAndRequestNotificationPermission(onPermissionGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted, proceed with the action
                onPermissionGranted()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity, Manifest.permission.POST_NOTIFICATIONS) -> {
                // Explain to the user why you need the permission and request it
                requestNotificationPermission()
            }
            else -> {
                // Directly request the permission
                requestNotificationPermission()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun requestNotificationPermission() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("request_notification_permission", true)
        }
        context.startActivity(intent)
    }
}
