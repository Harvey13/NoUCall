package com.noucall.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.noucall.app.data.BlockedPrefix
import com.noucall.app.utils.Constants
import com.noucall.app.utils.SharedPreferencesManager

class SmsBlockerReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (!SharedPreferencesManager.isBlockingEnabled(context)) {
            return
        }
        
        try {
            val bundle = intent.extras ?: return
            val pdus = bundle.get("pdus") as? Array<*> ?: return
            
            for (pdu in pdus) {
                val smsMessage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val format = bundle.getString("format")
                    SmsMessage.createFromPdu(pdu as ByteArray, format)
                } else {
                    @Suppress("DEPRECATION")
                    SmsMessage.createFromPdu(pdu as ByteArray)
                }
                
                smsMessage?.let {
                    val sender = it.originatingAddress ?: return@let
                    val messageBody = it.messageBody ?: return@let
                    
                    if (shouldBlockSms(context, sender, messageBody)) {
                        blockSms(context, sender, messageBody)
                        abortBroadcast() // Prevent the SMS from reaching other apps
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsBlockerReceiver", "Error processing SMS", e)
        }
    }
    
    private fun shouldBlockSms(context: Context, sender: String, messageBody: String): Boolean {
        val blockedPrefixes = SharedPreferencesManager.getBlockedPrefixes(context)
        val whitelistedCountries = SharedPreferencesManager.getWhitelistedCountries(context)
        
        // Check if the sender number starts with any blocked prefix
        for (blockedPrefix in blockedPrefixes) {
            val prefix = blockedPrefix.prefix
            val normalizedPrefix = prefix.replace(" ", "").trim()
            val normalizedSender = sender.replace(" ", "").replace("+", "").trim()
            
            if (normalizedSender.startsWith(normalizedPrefix)) {
                // Check if the sender is from a whitelisted country
                if (whitelistedCountries.isNotEmpty()) {
                    val countryCode = extractCountryCode(sender)
                    if (countryCode != null && whitelistedCountries.contains(countryCode)) {
                        return false // Don't block if country is whitelisted
                    }
                }
                return true // Block the SMS
            }
        }
        
        return false
    }
    
    private fun blockSms(context: Context, sender: String, messageBody: String) {
        try {
            // Update statistics
            SharedPreferencesManager.incrementBlockedSmsCount(context)
            SharedPreferencesManager.addBlockedSmsToHistory(context, sender, messageBody, System.currentTimeMillis())
            
            // Show notification about blocked SMS
            showBlockedSmsNotification(context, sender, messageBody)
            
            Log.d("SmsBlockerReceiver", "Blocked SMS from: $sender")
            
        } catch (e: Exception) {
            Log.e("SmsBlockerReceiver", "Error blocking SMS", e)
        }
    }
    
    private fun showBlockedSmsNotification(context: Context, sender: String, messageBody: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        // Create notification channel for Android 8.0+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                Constants.CHANNEL_BLOCKED_SMS,
                "SMS Bloqués",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les SMS bloqués"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create notification
        val notification = androidx.core.app.NotificationCompat.Builder(context, Constants.CHANNEL_BLOCKED_SMS)
            .setContentTitle("SMS Bloqué")
            .setContentText("SMS bloqué depuis $sender")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText("SMS bloqué depuis $sender\n\n$messageBody"))
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
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
