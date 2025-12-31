package com.noucall.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionManager {
    
    fun hasCallPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasReadPhoneStatePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasAnswerPhoneCallsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
    
    
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
    
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return hasCallPermission(context) &&
                hasReadPhoneStatePermission(context) &&
                hasAnswerPhoneCallsPermission(context) &&
                hasOverlayPermission(context) &&
                hasNotificationPermission(context)
    }
    
    fun getMissingPermissions(context: Context): List<String> {
        val missingPermissions = mutableListOf<String>()
        
        if (!hasCallPermission(context)) {
            missingPermissions.add(Manifest.permission.CALL_PHONE)
        }
        
        if (!hasReadPhoneStatePermission(context)) {
            missingPermissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        
        if (!hasAnswerPhoneCallsPermission(context)) {
            missingPermissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }
        
        if (!hasNotificationPermission(context)) {
            missingPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return missingPermissions
    }
}
