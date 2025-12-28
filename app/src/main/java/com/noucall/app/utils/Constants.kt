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
    const val KEY_BLOCKED_SMS_COUNT = "blocked_sms_count"
    const val KEY_BLOCKED_CALLS_HISTORY = "blocked_calls_history"
    const val KEY_BLOCKED_SMS_HISTORY = "blocked_sms_history"
    
    // Default blocked prefixes (Démarchage Commercial)
    val DEFAULT_BLOCKED_PREFIXES = listOf(
        "09 48", "09 49",
        "01 62", "01 63",
        "02 70", "02 71",
        "03 77", "03 78",
        "04 24", "04 25",
        "05 68", "05 69"
    )
    
    // Default whitelisted countries (can be customized)
    val DEFAULT_WHITELISTED_COUNTRIES = listOf<String>()
    
    // Intent actions
    const val ACTION_CALL_BLOCKED = "com.noucall.app.CALL_BLOCKED"
    const val ACTION_SMS_BLOCKED = "com.noucall.app.SMS_BLOCKED"
    
    // Notification channels
    const val CHANNEL_BLOCKED_CALLS = "blocked_calls"
    const val CHANNEL_BLOCKED_SMS = "blocked_sms"
    
    // Request codes
    const val REQUEST_CALL_PERMISSION = 1001
    const val REQUEST_SMS_PERMISSION = 1002
    const val REQUEST_OVERLAY_PERMISSION = 1003
}
