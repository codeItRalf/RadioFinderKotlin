package app.codeitralf.radiofinder

import android.app.Application
import app.codeitralf.radiofinder.utils.AppInfo
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RadioFinder : Application() {
    override fun onCreate() {
        super.onCreate()
         AppInfo.init(this)
    }
}