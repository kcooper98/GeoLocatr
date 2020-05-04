package com.csci442.geolocatr

import android.app.Application

class LocatrApplication : Application() {
    companion object {
        lateinit var locatrSharedPreferences: LocatrPreferences
    }

    override fun onCreate() {
        super.onCreate()
        locatrSharedPreferences = LocatrPreferences(applicationContext)
    }
}