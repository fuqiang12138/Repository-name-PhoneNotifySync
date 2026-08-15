package com.example.phonenotifysync.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.phonenotifysync.model.NotificationInfo

class PhoneAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        val text = event.text.joinToString(" ")

        val notification = NotificationInfo(
            appName = packageName,
            title = packageName,
            content = text
        )

        // TODO: send through encrypted WebSocket transport
    }

    override fun onInterrupt() {
    }
}
