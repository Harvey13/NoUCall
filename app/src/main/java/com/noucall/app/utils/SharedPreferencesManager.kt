package com.noucall.app.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noucall.app.data.BlockedPrefix

class SharedPreferencesManager private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val contextReference = java.lang.ref.WeakReference(context)
    
    companion object {
        @Volatile
        private var INSTANCE: SharedPreferencesManager? = null
        
        fun getInstance(context: Context): SharedPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharedPreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Blocking enabled status
        fun isBlockingEnabled(context: Context): Boolean {
            return getInstance(context).isBlockingEnabled()
        }
        
        fun setBlockingEnabled(context: Context, enabled: Boolean) {
            getInstance(context).setBlockingEnabled(enabled)
        }
        
        // Blocked prefixes management
        fun getBlockedPrefixes(context: Context): List<BlockedPrefix> {
            return getInstance(context).getBlockedPrefixes()
        }
        
        fun addBlockedPrefix(context: Context, prefix: String, comment: String = "") {
            getInstance(context).addBlockedPrefix(prefix, comment)
        }
        
        fun removeBlockedPrefix(context: Context, prefix: String) {
            getInstance(context).removeBlockedPrefix(prefix)
        }
        
        // Whitelisted countries management
        fun getWhitelistedCountries(context: Context): List<String> {
            return getInstance(context).getWhitelistedCountries()
        }
        
        fun addWhitelistedCountry(context: Context, country: String) {
            getInstance(context).addWhitelistedCountry(country)
        }
        
        fun removeWhitelistedCountry(context: Context, country: String) {
            getInstance(context).removeWhitelistedCountry(country)
        }
        
        fun initializeFrenchCommercialPrefixes(context: Context) {
            getInstance(context).initializeFrenchCommercialPrefixes()
        }
        
        fun cleanupFrenchCommercialPrefixes(context: Context) {
            getInstance(context).cleanupFrenchCommercialPrefixes()
        }
        
        fun forceResetFrenchCommercialPrefixes(context: Context) {
            getInstance(context).forceResetFrenchCommercialPrefixes()
        }
        
        // Statistics management
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
        
        fun setBlockedCallsHistory(context: Context, history: List<BlockedCall>) {
            getInstance(context).setBlockedCallsHistory(history)
        }
        
        // Dark mode management
        fun isDarkMode(context: Context): Boolean {
            return getInstance(context).isDarkMode()
        }
        
        fun setDarkMode(context: Context, isDark: Boolean) {
            getInstance(context).setDarkMode(isDark)
        }
        
        // Last detection management
        fun getLastDetectedNumber(context: Context): String {
            return getInstance(context).getLastDetectedNumber()
        }
        
        fun getLastDetectionReason(context: Context): String {
            return getInstance(context).getLastDetectionReason()
        }
        
        fun getLastDetectionTimestamp(context: Context): Long {
            return getInstance(context).getLastDetectionTimestamp()
        }
        
        fun setLastDetection(context: Context, number: String, reason: String, timestamp: Long) {
            getInstance(context).setLastDetection(number, reason, timestamp)
        }
        
        fun clearLastDetection(context: Context) {
            getInstance(context).clearLastDetection()
        }
    }
    
    // Instance methods
    fun isBlockingEnabled(): Boolean {
        val isEnabled = sharedPreferences.getBoolean(Constants.KEY_BLOCKING_ENABLED, false)
        Log.d("SPManager", "isBlockingEnabled read as: $isEnabled")
        return isEnabled
    }
    
    fun setBlockingEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(Constants.KEY_BLOCKING_ENABLED, enabled).commit()
        Log.d("SPManager", "isBlockingEnabled set to: $enabled")
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
                    // Migrate to BlockedPrefix with empty comment (no default prefixes)
                    oldPrefixes.map { prefix ->
                        BlockedPrefix(prefix, "")
                    }
                } catch (e2: Exception) {
                    // If both fail, return empty list (no default prefixes)
                    emptyList<BlockedPrefix>()
                }
            }
        } else {
            // No default prefixes - they will be added dynamically when France is whitelisted
            emptyList<BlockedPrefix>()
        }
    }
    
    fun setBlockedPrefixes(prefixes: List<BlockedPrefix>) {
        val prefixesJson = gson.toJson(prefixes)
        sharedPreferences.edit().putString(Constants.KEY_BLOCKED_PREFIXES, prefixesJson).commit()
    }
    
    fun addBlockedPrefix(prefix: String, comment: String = "") {
        // Normalize prefix before adding
        val normalizedPrefix = prefix.replace("[^0-9]".toRegex(), "")
        if (normalizedPrefix.isEmpty()) return
        
        val currentPrefixes = getBlockedPrefixes().toMutableList()
        if (!currentPrefixes.any { it.prefix == normalizedPrefix }) {
            currentPrefixes.add(BlockedPrefix(normalizedPrefix, comment))
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
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(countriesJson, type) ?: emptyList()
            } catch (e: Exception) {
                Constants.DEFAULT_WHITELISTED_COUNTRIES
            }
        } else {
            Constants.DEFAULT_WHITELISTED_COUNTRIES
        }
    }
    
    fun setWhitelistedCountries(countries: List<String>) {
        val countriesJson = gson.toJson(countries)
        sharedPreferences.edit().putString(Constants.KEY_WHITELISTED_COUNTRIES, countriesJson).commit()
    }
    
    fun addWhitelistedCountry(country: String) {
        Log.d("SharedPreferencesManager", "Adding country: $country")
        val currentCountries = getWhitelistedCountries().toMutableList()
        if (!currentCountries.contains(country)) {
            currentCountries.add(country)
            setWhitelistedCountries(currentCountries)
            
            // If France is being added, add French commercial prefixes
            if (country == "+33") {
                Log.d("SharedPreferencesManager", "France added to whitelist, adding French commercial prefixes")
                addFrenchCommercialPrefixes()
            }
        } else {
            Log.d("SharedPreferencesManager", "Country $country already in whitelist")
        }
    }
    
    fun removeWhitelistedCountry(country: String) {
        Log.d("SharedPreferencesManager", "Removing country: $country")
        val currentCountries = getWhitelistedCountries().toMutableList()
        val wasPresent = currentCountries.contains(country)
        currentCountries.removeAll { it == country }
        setWhitelistedCountries(currentCountries)
        
        Log.d("SharedPreferencesManager", "Country $country was present: $wasPresent")
        
        // If France is being removed, remove French commercial prefixes
        if (country == "+33" && wasPresent) {
            Log.d("SharedPreferencesManager", "France removed from whitelist, removing French commercial prefixes")
            removeFrenchCommercialPrefixes()
        }
    }
    
    // Initialize French commercial prefixes if France is in whitelist (for first startup)
    fun initializeFrenchCommercialPrefixes() {
        val whitelistedCountries = getWhitelistedCountries()
        if (whitelistedCountries.contains("+33")) {
            addFrenchCommercialPrefixes()
        } else {
            // If France is not in whitelist, ensure no French commercial prefixes exist
            removeFrenchCommercialPrefixes()
        }
    }
    
    // Force cleanup of French commercial prefixes (for migration)
    fun cleanupFrenchCommercialPrefixes() {
        val whitelistedCountries = getWhitelistedCountries()
        if (!whitelistedCountries.contains("+33")) {
            removeFrenchCommercialPrefixes()
        }
    }
    
    // Force complete reset of French commercial prefixes
    fun forceResetFrenchCommercialPrefixes() {
        Log.d("SharedPreferencesManager", "Force reset of French commercial prefixes started")
        val currentPrefixes = getBlockedPrefixes().toMutableList()
        val beforeCount = currentPrefixes.size
        Log.d("SharedPreferencesManager", "Current prefixes before reset: ${currentPrefixes.map { it.prefix }}")
        
        // Remove all French commercial prefixes aggressively
        currentPrefixes.removeAll { prefix ->
            val prefixStr = prefix.prefix
            // Check for any French commercial prefix patterns
            prefixStr == "0948" || prefixStr == "0949" ||
            prefixStr == "0162" || prefixStr == "0163" ||
            prefixStr == "0270" || prefixStr == "0271" ||
            prefixStr == "0377" || prefixStr == "0378" ||
            prefixStr == "0424" || prefixStr == "0425" ||
            prefixStr == "0568" || prefixStr == "0569" ||
            prefixStr.startsWith("094") || prefixStr.startsWith("016") ||
            prefixStr.startsWith("027") || prefixStr.startsWith("037") ||
            prefixStr.startsWith("042") || prefixStr.startsWith("056")
        }
        
        val afterCount = currentPrefixes.size
        Log.d("SharedPreferencesManager", "Force reset: removed ${beforeCount - afterCount} French commercial prefixes")
        Log.d("SharedPreferencesManager", "Remaining prefixes after reset: ${currentPrefixes.map { it.prefix }}")
        
        setBlockedPrefixes(currentPrefixes)
        Log.d("SharedPreferencesManager", "Force reset completed")
    }
    
    private fun addFrenchCommercialPrefixes() {
        val currentPrefixes = getBlockedPrefixes().toMutableList()
        val beforeCount = currentPrefixes.size
        var addedCount = 0
        
        Constants.FRENCH_COMMERCIAL_PREFIXES.forEach { prefix ->
            if (!currentPrefixes.any { it.prefix == prefix }) {
                currentPrefixes.add(BlockedPrefix(prefix, "Démarchage Commercial"))
                addedCount++
                Log.d("SharedPreferencesManager", "Added French commercial prefix: $prefix")
            } else {
                Log.d("SharedPreferencesManager", "French commercial prefix already exists: $prefix")
            }
        }
        
        val afterCount = currentPrefixes.size
        Log.d("SharedPreferencesManager", "French commercial prefixes: before=$beforeCount, after=$afterCount, added=$addedCount")
        
        setBlockedPrefixes(currentPrefixes)
    }
    
    private fun removeFrenchCommercialPrefixes() {
        val currentPrefixes = getBlockedPrefixes().toMutableList()
        val beforeCount = currentPrefixes.size
        Log.d("SharedPreferencesManager", "Current prefixes before removal: ${currentPrefixes.map { "${it.prefix}(${it.comment})" }}")
        
        // Remove exact matches
        currentPrefixes.removeAll { Constants.FRENCH_COMMERCIAL_PREFIXES.contains(it.prefix) }
        
        // Also remove any prefix that starts with commercial numbers (more aggressive)
        currentPrefixes.removeAll { prefix ->
            Constants.FRENCH_COMMERCIAL_PREFIXES.any { commercial ->
                prefix.prefix.startsWith(commercial)
            }
        }
        
        val afterCount = currentPrefixes.size
        Log.d("SharedPreferencesManager", "Removed ${beforeCount - afterCount} French commercial prefixes")
        Log.d("SharedPreferencesManager", "Remaining prefixes after removal: ${currentPrefixes.map { "${it.prefix}(${it.comment})" }}")
        
        setBlockedPrefixes(currentPrefixes)
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
    
    fun setBlockedCallsHistory(history: List<BlockedCall>) {
        val historyJson = gson.toJson(history)
        sharedPreferences.edit().putString(Constants.KEY_BLOCKED_CALLS_HISTORY, historyJson).commit()
    }
    
    fun clearHistory() {
        sharedPreferences.edit()
            .remove(Constants.KEY_BLOCKED_CALLS_HISTORY)
            .putInt(Constants.KEY_BLOCKED_CALLS_COUNT, 0)
            .commit()
    }
    
    // Dark mode management
    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(Constants.KEY_DARK_MODE, false)
    }
    
    fun setDarkMode(isDark: Boolean) {
        sharedPreferences.edit().putBoolean(Constants.KEY_DARK_MODE, isDark).commit()
    }
    
    // Last detection management
    fun getLastDetectedNumber(): String {
        return sharedPreferences.getString(Constants.KEY_LAST_DETECTED_NUMBER, "") ?: ""
    }
    
    fun getLastDetectionReason(): String {
        return sharedPreferences.getString(Constants.KEY_LAST_DETECTION_REASON, "") ?: ""
    }
    
    fun getLastDetectionTimestamp(): Long {
        return sharedPreferences.getLong(Constants.KEY_LAST_DETECTION_TIMESTAMP, 0L)
    }
    
    fun setLastDetection(number: String, reason: String, timestamp: Long) {
        sharedPreferences.edit()
            .putString(Constants.KEY_LAST_DETECTED_NUMBER, number)
            .putString(Constants.KEY_LAST_DETECTION_REASON, reason)
            .putLong(Constants.KEY_LAST_DETECTION_TIMESTAMP, timestamp)
            .commit()
    }
    
    fun clearLastDetection() {
        sharedPreferences.edit()
            .remove(Constants.KEY_LAST_DETECTED_NUMBER)
            .remove(Constants.KEY_LAST_DETECTION_REASON)
            .remove(Constants.KEY_LAST_DETECTION_TIMESTAMP)
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
