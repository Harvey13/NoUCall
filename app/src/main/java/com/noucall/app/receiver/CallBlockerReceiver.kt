package com.noucall.app.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.noucall.app.service.CallBlockerService
import com.noucall.app.utils.Constants
import com.noucall.app.utils.SharedPreferencesManager

class CallBlockerReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (!SharedPreferencesManager.isBlockingEnabled(context)) {
            return
        }
        
        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    if (incomingNumber != null && shouldBlockCall(context, incomingNumber)) {
                        blockCall(context, incomingNumber)
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
        
        // Check if the phone number starts with any blocked prefix
        for (prefix in blockedPrefixes) {
            val normalizedPrefix = prefix.replace(" ", "").trim()
            val normalizedPhoneNumber = phoneNumber.replace(" ", "").replace("+", "").trim()
            
            if (normalizedPhoneNumber.startsWith(normalizedPrefix)) {
                // Check if the number is from a whitelisted country
                if (whitelistedCountries.isNotEmpty()) {
                    val countryCode = extractCountryCode(phoneNumber)
                    if (countryCode != null && whitelistedCountries.contains(countryCode)) {
                        return false // Don't block if country is whitelisted
                    }
                }
                return true // Block the call
            }
        }
        
        return false
    }
    
    private fun blockCall(context: Context, phoneNumber: String) {
        try {
            // Update statistics
            SharedPreferencesManager.incrementBlockedCallsCount(context)
            SharedPreferencesManager.addBlockedCallToHistory(context, phoneNumber, System.currentTimeMillis())
            
            // Start the service to actually block the call
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
