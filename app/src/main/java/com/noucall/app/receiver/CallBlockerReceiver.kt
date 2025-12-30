package com.noucall.app.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import com.noucall.app.service.CallBlockerService
import com.noucall.app.utils.Constants
import com.noucall.app.utils.SharedPreferencesManager

class CallBlockerReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CallBlockerReceiver", "onReceive called with action: ${intent.action}")
        if (!SharedPreferencesManager.isBlockingEnabled(context)) {
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

        // Normalize the phone number
        val normalizedPhoneNumber = phoneNumber.replace(" ", "").replace("-", "").trim()
        val nationalNumber = getNationalNumber(normalizedPhoneNumber)

        Log.d("CallBlockerReceiver", "Original number: '$phoneNumber', normalized: '$normalizedPhoneNumber', national: '$nationalNumber'")

        // Check if the phone number starts with any blocked prefix
        for (prefix in blockedPrefixes) {
            val normalizedPrefix = prefix.replace(" ", "").replace("-", "").trim()

            Log.d("CallBlockerReceiver", "Checking prefix: '$normalizedPrefix' against national number: '$nationalNumber'")

            if (nationalNumber.startsWith(normalizedPrefix)) {
                Log.d("CallBlockerReceiver", "Prefix match found: $normalizedPrefix")
                // Check if the number is from a whitelisted country
                if (whitelistedCountries.isNotEmpty()) {
                    val countryCode = extractCountryCode(phoneNumber)
                    Log.d("CallBlockerReceiver", "Extracted country: $countryCode")
                    if (countryCode != null && whitelistedCountries.contains(countryCode)) {
                        Log.d("CallBlockerReceiver", "Country is whitelisted, not blocking")
                        return false // Don't block if country is whitelisted
                    }
                }
                Log.d("CallBlockerReceiver", "Blocking call")
                return true // Block the call
            }
        }

        Log.d("CallBlockerReceiver", "No prefix match, not blocking")
        return false
    }

    private fun getNationalNumber(phoneNumber: String): String {
        // Remove country code for known countries
        if (phoneNumber.startsWith("+33")) {
            val withoutCountry = phoneNumber.substring(3) // Remove +33 for France
            // For French mobile numbers starting with 6, add the leading 0
            if (withoutCountry.startsWith("6") && withoutCountry.length == 9) {
                return "0$withoutCountry"
            }
            return withoutCountry
        }
        if (phoneNumber.startsWith("0033")) {
            val withoutCountry = phoneNumber.substring(4) // Remove 0033 for France
            // For French mobile numbers starting with 6, add the leading 0
            if (withoutCountry.startsWith("6") && withoutCountry.length == 9) {
                return "0$withoutCountry"
            }
            return withoutCountry
        }
        // For numbers without country code, assume they are national
        if (!phoneNumber.startsWith("+")) {
            return phoneNumber
        }
        // For other countries, keep as is for now
        return phoneNumber.replace("+", "")
    }
    
    private fun blockCall(context: Context, phoneNumber: String) {
        try {
            // Update statistics
            SharedPreferencesManager.incrementBlockedCallsCount(context)
            SharedPreferencesManager.addBlockedCallToHistory(context, phoneNumber, System.currentTimeMillis())

            // Try to block using TelecomManager if we're the default dialer
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                if (telecomManager.defaultDialerPackage == context.packageName) {
                    Log.d("CallBlockerReceiver", "Using TelecomManager to block call")
                    try {
                        telecomManager.endCall()
                        Log.d("CallBlockerReceiver", "Call blocked via TelecomManager")
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
    
    private fun extractCountryCode(phoneNumber: String): String? {
        val normalizedNumber = phoneNumber.replace(" ", "").replace("-", "").trim()
        
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
            if (normalizedNumber.startsWith(code.substring(1))) { // Remove + for comparison
                return country
            }
        }
        
        // For French numbers starting with 0 (without country code)
        if (normalizedNumber.startsWith("0") && normalizedNumber.length >= 9) {
            return "France"
        }
        
        return null
    }
}
