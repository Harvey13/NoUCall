package com.noucall.app.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import com.noucall.app.data.BlockedPrefix
import com.noucall.app.service.CallBlockerService
import com.noucall.app.utils.SharedPreferencesManager

class CallBlockerReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CallBlockerReceiver", "onReceive called with action: ${intent.action}")
        val blockingEnabled = SharedPreferencesManager.isBlockingEnabled(context)
        Log.d("CallBlockerReceiver", "Blocking enabled: $blockingEnabled")
        if (!blockingEnabled) {
            Log.d("CallBlockerReceiver", "Blocking is disabled")
            return
        }

        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            Log.d("CallBlockerReceiver", "Call state: $state")

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    Log.d("CallBlockerReceiver", "Incoming number: $incomingNumber")
                    if (incomingNumber != null && shouldBlockCall(context, incomingNumber)) {
                        Log.d("CallBlockerReceiver", "Should block call from: $incomingNumber")
                        blockCall(context, incomingNumber)
                    } else {
                        Log.d("CallBlockerReceiver", "Not blocking call from: $incomingNumber")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CallBlockerReceiver", "Error processing call", e)
        }
    }
    
    private fun shouldBlockCall(context: Context, phoneNumber: String): Boolean {
        val blockedPrefixes = SharedPreferencesManager.getBlockedPrefixes(context)
        val whitelistedCountries = SharedPreferencesManager.getWhitelistedCountries(context)

        Log.d("CallBlockerReceiver", "Blocked prefixes: $blockedPrefixes")
        Log.d("CallBlockerReceiver", "Whitelisted countries: $whitelistedCountries")

        // Normalize to E.164 format
        val normalizedPhoneNumber = normalizeToE164(phoneNumber)
        val countryCode = extractCountryCodeFromE164(normalizedPhoneNumber)
        val nationalNumber = getNationalNumberFromE164(normalizedPhoneNumber)

        Log.d("CallBlockerReceiver", "Original: '$phoneNumber', E.164: '$normalizedPhoneNumber', country: $countryCode, national: '$nationalNumber'")

        // First check if country is whitelisted (but this doesn't override explicit prefix blocking)
        val isCountryWhitelisted = countryCode != null && whitelistedCountries.contains(countryCode)
        Log.d("CallBlockerReceiver", "Country whitelisted: $isCountryWhitelisted")

        // Check if the phone number starts with any blocked prefix
        for (blockedPrefix in blockedPrefixes) {
            val prefix = normalizePrefix(blockedPrefix.prefix)
            
            Log.d("CallBlockerReceiver", "Checking prefix: '$prefix' against national number: '$nationalNumber'")

            if (nationalNumber.startsWith(prefix)) {
                Log.d("CallBlockerReceiver", "Prefix match found: $prefix - BLOCKING (explicit prefix blocking overrides country whitelist)")
                return true // Always block if prefix matches, even if country is whitelisted
            }
        }

        // If no prefix matched and country is whitelisted, don't block
        if (isCountryWhitelisted) {
            Log.d("CallBlockerReceiver", "No prefix match and country is whitelisted, not blocking")
            return false
        }

        Log.d("CallBlockerReceiver", "No prefix match and country not whitelisted, not blocking")
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
    
    private fun extractCountryCodeFromE164(e164Number: String): String? {
        if (!e164Number.startsWith("+")) return null
        
        // Common country codes mapping
        val countryCodes = mapOf(
            "+33" to "France",
            "+41" to "Suisse",
            "+32" to "Belgique",
            "+352" to "Luxembourg",
            "+49" to "Allemagne",
            "+43" to "Autriche",
            "+31" to "Pays-Bas",
            "+34" to "Espagne",
            "+39" to "Italie",
            "+44" to "Royaume-Uni",
            "+351" to "Portugal",
            "+353" to "Irlande",
            "+45" to "Danemark",
            "+46" to "Suède",
            "+47" to "Norvège",
            "+358" to "Finlande",
            "+354" to "Islande"
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
                Log.d("CallBlockerReceiver", "Call from $phoneNumber was already blocked recently, skipping")
                return
            }

            // Try to end the call using TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                if (telecomManager != null) {
                    try {
                        telecomManager.endCall()
                        Log.d("CallBlockerReceiver", "Call blocked via TelecomManager")
                        
                        // Add to history and increment counter only after successful blocking
                        SharedPreferencesManager.addBlockedCallToHistory(context, phoneNumber, System.currentTimeMillis())
                        SharedPreferencesManager.incrementBlockedCallsCount(context)
                        return
                    } catch (e: Exception) {
                        Log.e("CallBlockerReceiver", "Failed to block via TelecomManager", e)
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

            Log.d("CallBlockerReceiver", "Blocked call from: $phoneNumber")

        } catch (e: Exception) {
            Log.e("CallBlockerReceiver", "Error blocking call", e)
        }
    }
    
}
