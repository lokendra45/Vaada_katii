package com.gaatho.rent.core.notifications

/**
 * Service for showing local notifications.
 */
interface NotificationService {
    /**
     * Shows a local notification to the user.
     */
    fun showNotification(title: String, message: String)
}
