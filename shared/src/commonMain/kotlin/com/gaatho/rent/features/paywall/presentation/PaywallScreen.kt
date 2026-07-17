package com.gaatho.rent.features.paywall.presentation

import androidx.compose.runtime.Composable
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

/**
 * Displays the official RevenueCat Paywall UI.
 *
 * The paywall content (pricing, copy, design) is configured remotely from the
 * RevenueCat dashboard — no code changes needed to update pricing or A/B test designs.
 */
@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit
) {
    Paywall(
        options = PaywallOptions(
            dismissRequest = onDismiss
        ) {
            shouldDisplayDismissButton = true
            listener = object : com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener {
                override fun onPurchaseCompleted(
                    customerInfo: CustomerInfo,
                    storeTransaction: com.revenuecat.purchases.kmp.models.StoreTransaction
                ) {
                    onPurchaseSuccess()
                }

                override fun onRestoreCompleted(
                    customerInfo: CustomerInfo
                ) {
                    val hasPro = customerInfo.entitlements.active["Rentmanager Pro"] != null
                    if (hasPro) onPurchaseSuccess()
                }
            }
        }
    )
}
