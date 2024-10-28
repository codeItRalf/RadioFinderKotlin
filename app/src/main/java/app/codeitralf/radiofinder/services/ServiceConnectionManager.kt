package app.codeitralf.radiofinder.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.media3.common.util.UnstableApi

@UnstableApi
class ServiceConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "ServiceConnectionManager"
    }

    private var playerService: PlayerService? = null
    private var isBound = false
    private val serviceCallbacks = mutableListOf<ServiceCallback>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "Service Connected")
            playerService = (service as PlayerService.PlayerBinder).service
            isBound = true
            notifyCallbacks()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG, "Service Disconnected")
            isBound = false
            playerService = null
            clearCallbacks()
        }
    }

    fun bindService(onServiceConnected: ServiceCallback) {
        Log.d(TAG, "Binding service, isBound=$isBound")

        if (!serviceCallbacks.contains(onServiceConnected)) {
            serviceCallbacks.add(onServiceConnected)
        }

        if (isBound && playerService != null) {
            Log.d(TAG, "Service already bound, calling callback immediately")
            onServiceConnected(playerService)
            return
        }

        startAndBindService()
    }

    fun unbindService() {
        if (isBound) {
            try {
                context.unbindService(serviceConnection)
                clearCallbacks()
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
        }
    }

    fun getService(): PlayerService? = playerService

    private fun startAndBindService() {
        try {
            Intent(context, PlayerService::class.java).also { intent ->
                startServiceBasedOnAndroidVersion(intent)
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding service", e)
        }
    }

    private fun startServiceBasedOnAndroidVersion(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun notifyCallbacks() {
        serviceCallbacks.forEach { it(playerService) }
    }

    private fun clearCallbacks() {
        serviceCallbacks.clear()
        isBound = false
        playerService = null
    }
}

// Type alias for service callback
private typealias ServiceCallback = (PlayerService?) -> Unit