package com.example.phonenotifysync

import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationSyncService : NotificationListenerService() {
    companion object {
        fun isEnabled(context: Context): Boolean {
            val value = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
            return value.contains(ComponentName(context, NotificationSyncService::class.java).flattenToString())
        }
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val appName = try {
            val info = packageManager.getApplicationInfo(sbn.packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (_: Exception) { sbn.packageName }
        UdpSender.send(appName)
    }
}
