package com.noucall.app.utils

import android.util.Log
import com.noucall.app.BuildConfig

object DebugLog {
    private const val TAG = "NoUCall"
    
    fun d(tag: String, message: String) {
        // Always log in debug mode, conditionally in release
        if (BuildConfig.DEBUG_LOGS) {
            Log.d("$TAG-$tag", message)
        }
        // Also save to SharedPreferences for last detection (works in both debug/release)
        if (tag == "CallBlockerReceiver" && message.contains("Original:")) {
            // Extract phone number from log message for last detection
            val phoneNumber = extractPhoneNumberFromLog(message)
            if (phoneNumber.isNotEmpty()) {
                // This will be handled in the shouldBlockCall method
            }
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG_LOGS) {
            if (throwable != null) {
                Log.e("$TAG-$tag", message, throwable)
            } else {
                Log.e("$TAG-$tag", message)
            }
        }
    }
    
    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG_LOGS) {
            Log.w("$TAG-$tag", message)
        }
    }
    
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG_LOGS) {
            Log.i("$TAG-$tag", message)
        }
    }
    
    // Always save detection info (works in both debug and release)
    fun saveDetection(context: android.content.Context, phoneNumber: String, reason: String) {
        try {
            SharedPreferencesManager.setLastDetection(context, phoneNumber, reason, System.currentTimeMillis())
        } catch (e: Exception) {
            // Fallback if SharedPreferences fails
            Log.e("DebugLog", "Failed to save detection info", e)
        }
    }
    
    private fun extractPhoneNumberFromLog(message: String): String {
        // Extract phone number from log message like "Original: '0612345678', E.164:..."
        val regex = """Original:\s*'([^']+)'""".toRegex()
        val match = regex.find(message)
        return match?.groupValues?.get(1) ?: ""
    }
}
