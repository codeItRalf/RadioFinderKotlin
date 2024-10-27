package app.codeitralf.radiofinder.services
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.media3.common.util.UnstableApi

@UnstableApi
class ServiceConnectionManager(private val context: Context) {

    private var playerService: PlayerService? = null
    private var isBound = false
    private val callbacks = mutableListOf<(PlayerService?) -> Unit>()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("ServiceConnectionManager", "Service Connected")
            val binder = service as PlayerService.PlayerBinder
            playerService = binder.service
            isBound = true
            // Notify all registered callbacks
            callbacks.forEach { callback ->
                callback(playerService)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("ServiceConnectionManager", "Service Disconnected")
            isBound = false
            playerService = null
            callbacks.clear()
        }
    }

    fun bindService(onServiceConnectedCallback: (PlayerService?) -> Unit) {
        Log.d("ServiceConnectionManager", "Binding service, isBound=$isBound")

        // Add callback to the list
        if (!callbacks.contains(onServiceConnectedCallback)) {
            callbacks.add(onServiceConnectedCallback)
        }

        // If already bound, call callback immediately
        if (isBound && playerService != null) {
            Log.d("ServiceConnectionManager", "Service already bound, calling callback immediately")
            onServiceConnectedCallback(playerService)
            return
        }

        try {
            Intent(context, PlayerService::class.java).also { intent ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        } catch (e: Exception) {
            Log.e("ServiceConnectionManager", "Error binding service", e)
        }
    }

    fun unbindService() {
        if (isBound) {
            try {
                context.unbindService(connection)
                callbacks.clear()
                isBound = false
                playerService = null
            } catch (e: Exception) {
                Log.e("ServiceConnectionManager", "Error unbinding service", e)
            }
        }
    }

    fun getService(): PlayerService? {
        return playerService
    }

}