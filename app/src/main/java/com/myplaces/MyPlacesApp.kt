package com.myplaces

import android.app.Application
import org.osmdroid.config.Configuration

class MyPlacesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialisation OSMDroid : cache de tuiles + user-agent
        Configuration.getInstance().apply {
            load(this@MyPlacesApp, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
        }
    }
}
