package com.noucall.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.noucall.app.service.CallBlockerService
import com.noucall.app.utils.SharedPreferencesManager

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BootReceiver", "BootReceiver triggered with action: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d("BootReceiver", "Starting call blocker service after boot/restart")
                
                // Check if blocking is enabled
                if (SharedPreferencesManager.isBlockingEnabled(context)) {
                    Log.d("BootReceiver", "Blocking is enabled, starting service")
                    
                    // Start the foreground service
                    val serviceIntent = Intent(context, CallBlockerService::class.java)
                    serviceIntent.action = CallBlockerService.ACTION_START
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    
                    Log.d("BootReceiver", "Call blocker service started successfully")
                } else {
                    Log.d("BootReceiver", "Blocking is disabled, not starting service")
                }
            }
        }
    }
}
