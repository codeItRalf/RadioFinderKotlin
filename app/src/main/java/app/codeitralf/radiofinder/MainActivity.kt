package app.codeitralf.radiofinder
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import app.codeitralf.radiofinder.navigation.AppNavigation
import app.codeitralf.radiofinder.ui.theme.RadioFinderTheme
import dagger.hilt.android.AndroidEntryPoint


@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            RadioFinderTheme {
                val navController = rememberNavController()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    CheckAndRequestNotificationPermission()
                }
                AppNavigation(navController = navController)
            }
        }
    }

    @Composable
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun CheckAndRequestNotificationPermission() {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}


