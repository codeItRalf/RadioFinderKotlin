package com.example.radiofinder.utils

import android.content.Context

object AppInfo {
    lateinit var appName: String
    lateinit var appVersion: String

    fun init(context: Context) {
        appName = context.packageName
        appVersion = context.packageManager
            .getPackageInfo(context.packageName, 0).versionName
    }
}