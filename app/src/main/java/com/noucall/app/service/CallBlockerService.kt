package com.noucall.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.noucall.app.R
import com.noucall.app.utils.Constants

class CallBlockerService : Service() {
    
    companion object {
        const val ACTION_BLOCK_CALL = "block_call"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val NOTIFICATION_ID = 1001
    }
    
    private lateinit var notificationManager: NotificationManager
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_BLOCK_CALL -> {
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                if (phoneNumber != null) {
                    blockIncomingCall(phoneNumber)
                }
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private fun blockIncomingCall(phoneNumber: String) {
        try {
            // Method 1: End the call using TelephonyManager
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // For Android 9 and above
                try {
                    val telecomManager = getSystemService(Context.TELECOM_SERVICE) as android.telecom.TelecomManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // For Android 10 and above, we need to use a different approach
                        rejectCallViaTelecomManager(phoneNumber)
                    } else {
                        // For Android 9
                        endCallViaTelephonyManager()
                    }
                } catch (e: Exception) {
                    Log.e("CallBlockerService", "Error using TelecomManager", e)
                    // Fallback to older method
                    endCallViaTelephonyManager()
                }
            } else {
                // For older Android versions
                endCallViaTelephonyManager()
            }
            
            // Show notification about blocked call
            showBlockedCallNotification(phoneNumber)
            
        } catch (e: Exception) {
            Log.e("CallBlockerService", "Error blocking call from $phoneNumber", e)
        }
    }
    
    @Suppress("DEPRECATION")
    private fun endCallViaTelephonyManager() {
        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val clazz = Class.forName(telephonyManager.javaClass.name)
            val method = clazz.getDeclaredMethod("getITelephony")
            method.isAccessible = true
            val telephonyInterface = method.invoke(telephonyManager)
            val telephonyClass = Class.forName(telephonyInterface.javaClass.name)
            val endCallMethod = telephonyClass.getDeclaredMethod("endCall")
            endCallMethod.invoke(telephonyInterface)
        } catch (e: Exception) {
            Log.e("CallBlockerService", "Error ending call via TelephonyManager", e)
        }
    }
    
    @Suppress("DEPRECATION")
    private fun rejectCallViaTelecomManager(phoneNumber: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+, we need to use a different approach
                // This is a simplified version - in practice, you might need to use
                // a more sophisticated method or accessibility services
                val telecomManager = getSystemService(Context.TELECOM_SERVICE) as android.telecom.TelecomManager
                
                // Try to reject the call
                try {
                    telecomManager.endCall()
                } catch (e: Exception) {
                    Log.e("CallBlockerService", "Error rejecting call via TelecomManager", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CallBlockerService", "Error in rejectCallViaTelecomManager", e)
        }
    }
    
    private fun showBlockedCallNotification(phoneNumber: String) {
        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_BLOCKED_CALLS)
            .setContentTitle("Appel Bloqué")
            .setContentText("Appel bloqué depuis $phoneNumber")
            .setSmallIcon(R.drawable.ic_block)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_BLOCKED_CALLS,
                "Appels Bloqués",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les appels bloqués"
                enableLights(true)
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
}
