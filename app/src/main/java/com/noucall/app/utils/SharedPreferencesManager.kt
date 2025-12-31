package com.noucall.app.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noucall.app.data.BlockedPrefix
import java.lang.ref.WeakReference

class SharedPreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    companion object {
        private var instance: SharedPreferencesManager? = null
        private var contextReference: WeakReference<Context>? = null
        
        fun getInstance(context: Context): SharedPreferencesManager {
            if (instance == null) {
                instance = SharedPreferencesManager(context.applicationContext)
                contextReference = WeakReference(context.applicationContext)
            }
            return instance!!
        }
        
        // Convenience methods
        fun isBlockingEnabled(context: Context): Boolean {
            return getInstance(context).isBlockingEnabled()
        }
        
        fun setBlockingEnabled(context: Context, enabled: Boolean) {
            getInstance(context).setBlockingEnabled(enabled)
        }
        
        fun getBlockedPrefixes(context: Context): List<BlockedPrefix> {
            return getInstance(context).getBlockedPrefixes()
        }
        
        fun addBlockedPrefix(context: Context, prefix: String, comment: String = "") {
            getInstance(context).addBlockedPrefix(prefix, comment)
        }
        
        fun removeBlockedPrefix(context: Context, prefix: String) {
            getInstance(context).removeBlockedPrefix(prefix)
        }
        
        fun getWhitelistedCountries(context: Context): List<String> {
            return getInstance(context).getWhitelistedCountries()
        }
        
        fun addWhitelistedCountry(context: Context, country: String) {
            getInstance(context).addWhitelistedCountry(country)
        }
        
        fun removeWhitelistedCountry(context: Context, country: String) {
            getInstance(context).removeWhitelistedCountry(country)
        }
        
        fun isDarkMode(context: Context): Boolean {
            return getInstance(context).isDarkMode()
        }
        
        fun setDarkMode(context: Context, isDarkMode: Boolean) {
            getInstance(context).setDarkMode(isDarkMode)
        }
        
        fun getBlockedCallsCount(context: Context): Int {
            return getInstance(context).getBlockedCallsCount()
        }
        
        fun incrementBlockedCallsCount(context: Context) {
            getInstance(context).incrementBlockedCallsCount()
        }
        
        
        fun addBlockedCallToHistory(context: Context, phoneNumber: String, timestamp: Long) {
            getInstance(context).addBlockedCallToHistory(phoneNumber, timestamp)
        }
        
        
        fun clearHistory(context: Context) {
            getInstance(context).clearHistory()
        }
        
        fun getBlockedCallsHistory(context: Context): List<BlockedCall> {
            return getInstance(context).getBlockedCallsHistory()
        }
        
    }
    
    // Blocking enabled status
    fun isBlockingEnabled(): Boolean {
        val isEnabled = sharedPreferences.getBoolean(Constants.KEY_BLOCKING_ENABLED, false)
        Log.d("SPManager", "isBlockingEnabled read as: $isEnabled")
        return isEnabled
    }
    
    fun setBlockingEnabled(enabled: Boolean) {
        Log.d("SPManager", "setBlockingEnabled called with: $enabled")
        sharedPreferences.edit().putBoolean(Constants.KEY_BLOCKING_ENABLED, enabled).commit()
    }
    
    // Blocked prefixes management
    fun getBlockedPrefixes(): List<BlockedPrefix> {
        val prefixesJson = sharedPreferences.getString(Constants.KEY_BLOCKED_PREFIXES, null)
        return if (prefixesJson != null) {
            try {
                // Try to parse as List<BlockedPrefix>
                val type = object : TypeToken<List<BlockedPrefix>>() {}.type
                gson.fromJson(prefixesJson, type)
            } catch (e: Exception) {
                // If parsing fails, try to parse as List<String> and migrate
                try {
                    val stringType = object : TypeToken<List<String>>() {}.type
                    val oldPrefixes = gson.fromJson<List<String>>(prefixesJson, stringType)
                    // Migrate to BlockedPrefix with default comment for known prefixes
                    oldPrefixes.map { prefix ->
                        val comment = if (Constants.DEFAULT_BLOCKED_PREFIXES.contains(prefix)) {
                            "Démarchage Commercial"
                        } else {
                            ""
                        }
                        BlockedPrefix(prefix, comment)
                    }
                } catch (e2: Exception) {
                    // If both fail, return default
                    Constants.DEFAULT_BLOCKED_PREFIXES.map { BlockedPrefix(it, "Démarchage Commercial") }
                }
            }
        } else {
            Constants.DEFAULT_BLOCKED_PREFIXES.map { BlockedPrefix(it, "Démarchage Commercial") }
        }
    }
    
    fun setBlockedPrefixes(prefixes: List<BlockedPrefix>) {
        val prefixesJson = gson.toJson(prefixes)
        sharedPreferences.edit().putString(Constants.KEY_BLOCKED_PREFIXES, prefixesJson).commit()
    }
    
    fun addBlockedPrefix(prefix: String, comment: String = "") {
        val currentPrefixes = getBlockedPrefixes().toMutableList()
        if (!currentPrefixes.any { it.prefix == prefix }) {
            val finalComment = if (comment.isEmpty() && Constants.DEFAULT_BLOCKED_PREFIXES.contains(prefix)) {
                "Démarchage Commercial"
            } else {
                comment
            }
            currentPrefixes.add(BlockedPrefix(prefix, finalComment))
            setBlockedPrefixes(currentPrefixes)
        }
    }
    
    fun removeBlockedPrefix(prefix: String) {
        val currentPrefixes = getBlockedPrefixes().toMutableList()
        currentPrefixes.removeAll { it.prefix == prefix }
        setBlockedPrefixes(currentPrefixes)
    }
    
    // Whitelisted countries management
    fun getWhitelistedCountries(): List<String> {
        val countriesJson = sharedPreferences.getString(Constants.KEY_WHITELISTED_COUNTRIES, null)
        return if (countriesJson != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(countriesJson, type) ?: Constants.DEFAULT_WHITELISTED_COUNTRIES
        } else {
            Constants.DEFAULT_WHITELISTED_COUNTRIES
        }
    }
    
    fun setWhitelistedCountries(countries: List<String>) {
        val countriesJson = gson.toJson(countries)
        sharedPreferences.edit().putString(Constants.KEY_WHITELISTED_COUNTRIES, countriesJson).commit()
    }
    
    fun addWhitelistedCountry(country: String) {
        val currentCountries = getWhitelistedCountries().toMutableList()
        if (!currentCountries.contains(country)) {
            currentCountries.add(country)
            setWhitelistedCountries(currentCountries)
        }
    }
    
    fun removeWhitelistedCountry(country: String) {
        val currentCountries = getWhitelistedCountries().toMutableList()
        currentCountries.remove(country)
        setWhitelistedCountries(currentCountries)
    }
    
    // Theme management
    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(Constants.KEY_DARK_MODE, false)
    }
    
    fun setDarkMode(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean(Constants.KEY_DARK_MODE, isDarkMode).commit()
    }
    
    // Statistics management
    fun getBlockedCallsCount(): Int {
        return sharedPreferences.getInt(Constants.KEY_BLOCKED_CALLS_COUNT, 0)
    }
    
    fun incrementBlockedCallsCount() {
        val currentCount = getBlockedCallsCount()
        sharedPreferences.edit().putInt(Constants.KEY_BLOCKED_CALLS_COUNT, currentCount + 1).commit()
        // Send broadcast to update UI
        contextReference?.get()?.sendBroadcast(Intent("com.noucall.app.STATISTICS_UPDATED"))
    }
    
    
    // History management
    fun addBlockedCallToHistory(phoneNumber: String, timestamp: Long) {
        val history = getBlockedCallsHistory().toMutableList()
        history.add(BlockedCall(phoneNumber, timestamp))
        val historyJson = gson.toJson(history)
        sharedPreferences.edit().putString(Constants.KEY_BLOCKED_CALLS_HISTORY, historyJson).commit()
    }
    
    fun getBlockedCallsHistory(): List<BlockedCall> {
        val historyJson = sharedPreferences.getString(Constants.KEY_BLOCKED_CALLS_HISTORY, null)
        return if (historyJson != null) {
            val type = object : TypeToken<List<BlockedCall>>() {}.type
            gson.fromJson(historyJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    
    fun clearHistory() {
        sharedPreferences.edit()
            .remove(Constants.KEY_BLOCKED_CALLS_HISTORY)
            .putInt(Constants.KEY_BLOCKED_CALLS_COUNT, 0)
            .commit()
    }
}

// Data classes for history
data class BlockedCall(
    val phoneNumber: String,
    val timestamp: Long
)

data class BlockedSms(
    val phoneNumber: String,
    val message: String,
    val timestamp: Long
)
