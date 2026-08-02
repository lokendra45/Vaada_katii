package com.gaatho.rent.core.utils

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * A simple provider to hold a weak reference to the current activity.
 * Necessary for BiometricPrompt which requires a FragmentActivity.
 */
object ActivityProvider {
    private var activityRef: WeakReference<FragmentActivity>? = null

    var activity: FragmentActivity?
        get() = activityRef?.get()
        set(value) {
            activityRef = WeakReference(value)
        }
}
