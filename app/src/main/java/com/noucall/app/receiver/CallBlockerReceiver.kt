package com.noucall.app.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.telecom.TelecomManager
import android.util.Log
import com.noucall.app.R
import com.noucall.app.utils.DebugLog
import com.noucall.app.utils.SharedPreferencesManager
import com.noucall.app.utils.LocaleManager
import com.noucall.app.data.BlockedPrefix
import com.noucall.app.service.CallBlockerService

class CallBlockerReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        DebugLog.d("CallBlockerReceiver", "onReceive called with action: ${intent.action}")
        
        // Handle boot completion and app restart
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                DebugLog.d("CallBlockerReceiver", "Boot/restart detected, checking if service should start")
                if (SharedPreferencesManager.isBlockingEnabled(context)) {
                    startCallBlockerService(context)
                }
                return
            }
        }
        
        // Handle phone state changes
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            DebugLog.d("CallBlockerReceiver", "Ignoring action: ${intent.action}")
            return
        }
        
        // Create localized context for proper string resolution
        val localizedContext = LocaleManager.updateContextLanguage(context, LocaleManager.getLanguage(context))
        
        val blockingEnabled = SharedPreferencesManager.isBlockingEnabled(localizedContext)
        DebugLog.d("CallBlockerReceiver", "Blocking enabled: $blockingEnabled")
        if (!blockingEnabled) {
            DebugLog.d("CallBlockerReceiver", "Blocking is disabled")
            return
        }

        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            DebugLog.d("CallBlockerReceiver", "Call state: $state")

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    DebugLog.d("CallBlockerReceiver", "Incoming number: $incomingNumber")
                    if (incomingNumber != null && shouldBlockCall(localizedContext, incomingNumber)) {
                        DebugLog.d("CallBlockerReceiver", "Should block call from: $incomingNumber")
                        blockCall(localizedContext, incomingNumber)
                    } else {
                        DebugLog.d("CallBlockerReceiver", "Not blocking call from: $incomingNumber")
                    }
                }
            }
        } catch (e: Exception) {
            DebugLog.e("CallBlockerReceiver", "Error processing call", e)
        }
    }
    
    private fun startCallBlockerService(context: Context) {
        try {
            val serviceIntent = Intent(context, CallBlockerService::class.java).apply {
                action = CallBlockerService.ACTION_START
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            DebugLog.d("CallBlockerReceiver", "Call blocker service started successfully")
        } catch (e: Exception) {
            DebugLog.e("CallBlockerReceiver", "Failed to start call blocker service", e)
        }
    }
    
    private fun shouldBlockCall(context: Context, phoneNumber: String): Boolean {
        val blockedPrefixes = SharedPreferencesManager.getBlockedPrefixes(context)
        val whitelistedCountries = SharedPreferencesManager.getWhitelistedCountries(context)

        DebugLog.d("CallBlockerReceiver", "Blocked prefixes: $blockedPrefixes")
        DebugLog.d("CallBlockerReceiver", "Whitelisted countries: $whitelistedCountries")

        // Normalize to E.164 format
        val normalizedPhoneNumber = normalizeToE164(phoneNumber)
        val countryCode = extractCountryCodeFromE164(context, normalizedPhoneNumber)
        val nationalNumber = getNationalNumberFromE164(normalizedPhoneNumber)

        DebugLog.d("CallBlockerReceiver", "Original: '$phoneNumber', E.164: '$normalizedPhoneNumber', country: $countryCode, national: '$nationalNumber'")

        // First check if country is whitelisted (but this doesn't override explicit prefix blocking)
        val isCountryWhitelisted = countryCode != null && whitelistedCountries.contains(countryCode)
        DebugLog.d("CallBlockerReceiver", "Country whitelisted: $isCountryWhitelisted")

        // Check if the phone number starts with any blocked prefix
        for (blockedPrefix in blockedPrefixes) {
            val prefix = normalizePrefix(blockedPrefix.prefix)
            
            DebugLog.d("CallBlockerReceiver", "Checking prefix: '$prefix' against national number: '$nationalNumber'")

            // Check for exact match with full phone numbers (E.164 format)
            if (normalizedPhoneNumber == "+$prefix" || normalizedPhoneNumber == prefix) {
                val reason = context.getString(R.string.blocked_full_number_match_reason, prefix, countryCode, isCountryWhitelisted)
                DebugLog.d("CallBlockerReceiver", reason)
                // Save last detection (works in both debug and release)
                DebugLog.saveDetection(context, phoneNumber, reason)
                return true // Always block if exact number matches, even if country is whitelisted
            }
            
            // Check for prefix match with national number
            if (nationalNumber.startsWith(prefix)) {
                val reason = context.getString(R.string.blocked_prefix_match_reason, prefix, countryCode, isCountryWhitelisted)
                DebugLog.d("CallBlockerReceiver", reason)
                // Save last detection (works in both debug and release)
                DebugLog.saveDetection(context, phoneNumber, reason)
                return true // Always block if prefix matches, even if country is whitelisted
            }
        }

        // If no prefix matched and country is whitelisted, don't block
        if (isCountryWhitelisted) {
            val reason = context.getString(R.string.allowed_whitelisted_country_reason, countryCode)
            DebugLog.d("CallBlockerReceiver", reason)
            // Save last detection (works in both debug and release)
            DebugLog.saveDetection(context, phoneNumber, reason)
            return false
        }
        
        val reason = context.getString(R.string.allowed_no_prefix_whitelist_reason, countryCode)
        DebugLog.d("CallBlockerReceiver", reason)
        // Save last detection (works in both debug and release)
        DebugLog.saveDetection(context, phoneNumber, reason)
        return false
    }

    private fun normalizeToE164(phoneNumber: String): String {
        // Remove all non-numeric characters
        var normalized = phoneNumber.replace("[^0-9+]".toRegex(), "")
        
        // Convert 00 prefix to +
        if (normalized.startsWith("00")) {
            normalized = "+" + normalized.substring(2)
        }
        
        // For French numbers without country code, assume France
        if (!normalized.startsWith("+")) {
            // If it starts with 0 and has 9-10 digits, it's likely a French number
            if (normalized.startsWith("0") && normalized.length >= 9 && normalized.length <= 10) {
                return "+33" + normalized.substring(1)
            }
            // For other short numbers, keep as is (might be special numbers)
            return normalized
        }
        
        return normalized
    }
    
    private fun normalizePrefix(prefix: String): String {
        // Remove all non-numeric characters and spaces
        return prefix.replace("[^0-9]".toRegex(), "")
    }
    
    private fun extractCountryCodeFromE164(context: Context, e164Number: String): String? {
        if (!e164Number.startsWith("+")) return null
        
        // Common country codes mapping with localized names
        val countryCodes = mapOf(
            "+33" to context.getString(R.string.country_france),
            "+41" to context.getString(R.string.country_switzerland),
            "+32" to context.getString(R.string.country_belgium),
            "+352" to context.getString(R.string.country_luxembourg),
            "+49" to context.getString(R.string.country_germany),
            "+43" to context.getString(R.string.country_austria),
            "+31" to context.getString(R.string.country_netherlands),
            "+34" to context.getString(R.string.country_spain),
            "+39" to context.getString(R.string.country_italy),
            "+44" to context.getString(R.string.country_united_kingdom),
            "+351" to context.getString(R.string.country_portugal),
            "+353" to context.getString(R.string.country_ireland),
            "+45" to context.getString(R.string.country_denmark),
            "+46" to context.getString(R.string.country_sweden),
            "+47" to context.getString(R.string.country_norway),
            "+358" to context.getString(R.string.country_finland),
            "+354" to context.getString(R.string.country_iceland)
        )
        
        for ((code, country) in countryCodes) {
            if (e164Number.startsWith(code)) {
                return country
            }
        }
        
        return null
    }
    
    private fun getNationalNumberFromE164(e164Number: String): String {
        if (!e164Number.startsWith("+")) return e164Number
        
        // Remove country code to get national number
        for (code in listOf("+33", "+41", "+32", "+352", "+49", "+43", "+31", "+34", "+39", "+44", "+351", "+353", "+45", "+46", "+47", "+358", "+354")) {
            if (e164Number.startsWith(code)) {
                var national = e164Number.substring(code.length)
                // For France, add leading 0 if not present
                if (code == "+33" && national.length == 9 && national.startsWith("6")) {
                    national = "0" + national
                }
                return national
            }
        }
        
        // If no known country code, return number without +
        return e164Number.substring(1)
    }
    
    private fun blockCall(context: Context, phoneNumber: String) {
        try {
            // Check if this call was already blocked in the last 10 seconds (deduplication)
            val currentTime = System.currentTimeMillis()
            val blockedCallsHistory = SharedPreferencesManager.getBlockedCallsHistory(context)
            val recentlyBlocked = blockedCallsHistory.any { 
                it.phoneNumber == phoneNumber && (currentTime - it.timestamp) < 10000 
            }
            
            if (recentlyBlocked) {
                DebugLog.d("CallBlockerReceiver", "Call from $phoneNumber was already blocked recently, skipping")
                return
            }

            // Try to end the call using TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                if (telecomManager != null) {
                    try {
                        telecomManager.endCall()
                        DebugLog.d("CallBlockerReceiver", "Call blocked via TelecomManager")
                        
                        // Add to history and increment counter only after successful blocking
                        SharedPreferencesManager.addBlockedCallToHistory(context, phoneNumber, System.currentTimeMillis())
                        SharedPreferencesManager.incrementBlockedCallsCount(context)
                        return
                    } catch (e: Exception) {
                        DebugLog.e("CallBlockerReceiver", "Failed to block via TelecomManager", e)
                    }
                }
            }

            // Fallback to service method
            val serviceIntent = Intent(context, CallBlockerService::class.java).apply {
                action = CallBlockerService.ACTION_BLOCK_CALL
                putExtra(CallBlockerService.EXTRA_PHONE_NUMBER, phoneNumber)
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            DebugLog.d("CallBlockerReceiver", "Blocked call from: $phoneNumber")

        } catch (e: Exception) {
            DebugLog.e("CallBlockerReceiver", "Error blocking call", e)
        }
    }
    
}
