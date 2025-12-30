package com.noucall.app.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.noucall.app.utils.SharedPreferencesManager

class CallBlockerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val className = event.className?.toString()
            Log.d("CallBlockerAccessibility", "Window changed: $className")

            // Detect incoming call screen
            if (className == "com.android.incallui.InCallActivity" ||
                className == "com.android.phone.InCallScreen" ||
                className?.contains("InCall") == true) {

                Log.d("CallBlockerAccessibility", "Incoming call detected")

                // Get the phone number from the event if possible
                // This is limited, but we can check if we should block based on recent calls

                // For simplicity, we'll block all calls when accessibility is enabled
                // In a real app, you'd need to extract the number from the UI
                if (SharedPreferencesManager.isBlockingEnabled(this)) {
                    Log.d("CallBlockerAccessibility", "Blocking enabled, attempting to end call")
                    performGlobalAction(GLOBAL_ACTION_BACK) // Try to go back
                    // Or use other methods
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("CallBlockerAccessibility", "Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("CallBlockerAccessibility", "Accessibility service connected")
    }
}