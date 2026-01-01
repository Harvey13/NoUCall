package com.noucall.app.utils

object Constants {
    // SharedPreferences
    const val PREFS_NAME = "noucall_prefs"
    
    // Keys for SharedPreferences
    const val KEY_BLOCKING_ENABLED = "blocking_enabled"
    const val KEY_BLOCKED_PREFIXES = "blocked_prefixes"
    const val KEY_WHITELISTED_COUNTRIES = "whitelisted_countries"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_BLOCKED_CALLS_COUNT = "blocked_calls_count"
    const val KEY_BLOCKED_CALLS_HISTORY = "blocked_calls_history"
    const val KEY_LAST_DETECTED_NUMBER = "last_detected_number"
    const val KEY_LAST_DETECTION_REASON = "last_detection_reason"
    const val KEY_LAST_DETECTION_TIMESTAMP = "last_detection_timestamp"
    
    // Default blocked prefixes (Démarchage Commercial) - normalized to digits only
    val DEFAULT_BLOCKED_PREFIXES = listOf(
        "0948", "0949",
        "0162", "0163",
        "0270", "0271",
        "0377", "0378",
        "0424", "0425",
        "0568", "0569"
    )
    
    // Default whitelisted countries (can be customized)
    val DEFAULT_WHITELISTED_COUNTRIES = listOf<String>()
    
    // Intent actions
    const val ACTION_CALL_BLOCKED = "com.noucall.app.CALL_BLOCKED"
    
    // Notification channels
    const val CHANNEL_BLOCKED_CALLS = "blocked_calls"
    
    // Request codes
    const val REQUEST_CALL_PERMISSION = 1001
    const val REQUEST_OVERLAY_PERMISSION = 1003
}
