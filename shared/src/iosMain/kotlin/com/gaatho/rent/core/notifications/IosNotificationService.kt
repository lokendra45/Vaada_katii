package com.gaatho.rent.core.notifications

/**
 * iOS implementation of NotificationService.
 * Requires UNUserNotificationCenter setup in AppDelegate to actually dispatch.
 */
class IosNotificationService : NotificationService {
    override fun showNotification(title: String, message: String) {
        // Stub for iOS push notifications
        // In a real app, this would use UNUserNotificationCenter.currentNotificationCenter()
    }
}
