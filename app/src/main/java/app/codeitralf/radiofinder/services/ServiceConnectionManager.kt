package app.codeitralf.radiofinder.services
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.media3.common.util.UnstableApi

@UnstableApi
class ServiceConnectionManager(private val context: Context) {

    private var playerService: PlayerService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PlayerService.PlayerBinder
            playerService = binder.service
            isBound = true
            onServiceConnectedCallback?.invoke(playerService)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            playerService = null
        }
    }

    private var onServiceConnectedCallback: ((PlayerService?) -> Unit)? = null

    fun bindService(onServiceConnectedCallback: (PlayerService?) -> Unit) {
        this.onServiceConnectedCallback = onServiceConnectedCallback
        Intent(context, PlayerService::class.java).also { intent ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }

    fun getService(): PlayerService? {
        return playerService
    }

    fun isServiceBound(): Boolean {
        return isBound
    }
}