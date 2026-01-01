package com.noucall.app.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleManager {
    private const val PREF_LANGUAGE = "app_language"
    
    // Supported languages
    const val LANGUAGE_FRENCH = "fr"
    const val LANGUAGE_ENGLISH = "en"
    
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANGUAGE, languageCode).apply()
        
        // Apply language to context
        updateContextLanguage(context, languageCode)
    }
    
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, LANGUAGE_FRENCH) ?: LANGUAGE_FRENCH
    }
    
    fun updateContextLanguage(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config) ?: context
    }
    
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_FRENCH -> "Français"
            LANGUAGE_ENGLISH -> "English"
            else -> "Français"
        }
    }
    
    fun getLanguageFlag(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_FRENCH -> "🇫🇷"
            LANGUAGE_ENGLISH -> "🇬🇧"
            else -> "🇫🇷"
        }
    }
}
