package com.app.carbonbuddy.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "carbon_buddy_prefs"
        private const val KEY_FIRST_TIME_LAUNCH = "first_time_launch"
    }

    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME_LAUNCH, true)
    }

    fun setFirstTimeLaunchCompleted() {
        sharedPreferences.edit()
            .putBoolean(KEY_FIRST_TIME_LAUNCH, false)
            .apply()
    }
}
