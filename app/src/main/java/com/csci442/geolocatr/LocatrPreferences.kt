package com.csci442.geolocatr

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class LocatrPreferences(context: Context) {
    companion object {
        private const val PREFS_POLLING_KEY = "polling_key"
    }

    private val prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    var isPollingOn: Boolean
        get() = prefs.getBoolean(PREFS_POLLING_KEY, false)
        set(value) = prefs.edit {
            putBoolean(PREFS_POLLING_KEY, value)
            commit()
        }
    // Had to switch jvmTarget to 1.8, ask in comments why


}
