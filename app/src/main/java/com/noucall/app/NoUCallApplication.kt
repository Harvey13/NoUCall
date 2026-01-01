package com.noucall.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.noucall.app.utils.SharedPreferencesManager

class NoUCallApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Apply theme at application level
        applyTheme()
    }
    
    private fun applyTheme() {
        val isDarkMode = SharedPreferencesManager.getInstance(this).isDarkMode()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
