package com.gaatho.rent.features.paywall.data.repository

import com.revenuecat.purchases.kmp.CustomerInfo
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val PREMIUM_ENTITLEMENT = "Rentmanager Pro"

interface PaywallRepository {
    /**
     * Reactive stream of the current customer's subscription status.
     * Emits true if the "Premium" entitlement is active.
     */
    val isPremium: Flow<Boolean>

    /**
     * Returns available offerings from RevenueCat (fetched from the RC dashboard).
     */
    suspend fun getOfferings(): List<StoreProduct>

    /**
     * Initiates a purchase for the given [product].
     * Returns a [Result] wrapping the [StoreTransaction] on success.
     */
    suspend fun purchase(product: StoreProduct): Result<StoreTransaction>

    /**
     * Restores purchases previously made by this user on any device.
     */
    suspend fun restorePurchases(): Result<CustomerInfo>

    /**
     * Whether the user currently has an active Premium entitlement (non-reactive).
     */
    fun hasPremiumAccess(): Boolean
}

class RevenueCatPaywallRepository : PaywallRepository {

    private val _isPremium = MutableStateFlow(
        try {
            Purchases.sharedInstance.cachedCustomerInfo
                ?.entitlements?.active?.containsKey(PREMIUM_ENTITLEMENT) == true
        } catch (e: Exception) {
            false
        }
    )
    override val isPremium: Flow<Boolean> = _isPremium.asStateFlow()

    init {
        try {
            // Listen for CustomerInfo updates from RevenueCat
            Purchases.sharedInstance.updatedCustomerInfoListener = { customerInfo ->
                _isPremium.value = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
            }
        } catch (e: Exception) {
            // Purchases not configured yet or in unit tests
        }
    }

    override suspend fun getOfferings(): List<StoreProduct> = suspendCoroutine { cont ->
        Purchases.sharedInstance.getOfferings(
            onError = { error -> cont.resumeWithException(Exception(error.message)) },
            onSuccess = { offerings ->
                val products = offerings.current?.availablePackages
                    ?.map { it.storeProduct }
                    ?: emptyList()
                cont.resume(products)
            }
        )
    }

    override suspend fun purchase(product: StoreProduct): Result<StoreTransaction> =
        suspendCoroutine { cont ->
            Purchases.sharedInstance.purchase(
                storeProduct = product,
                onError = { error, _ -> cont.resume(Result.failure(Exception(error.message))) },
                onSuccess = { transaction, _ -> cont.resume(Result.success(transaction)) }
            )
        }

    override suspend fun restorePurchases(): Result<CustomerInfo> = suspendCoroutine { cont ->
        Purchases.sharedInstance.restorePurchases(
            onError = { error -> cont.resume(Result.failure(Exception(error.message))) },
            onSuccess = { customerInfo ->
                _isPremium.value = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
                cont.resume(Result.success(customerInfo))
            }
        )
    }

    override fun hasPremiumAccess(): Boolean {
        return try {
            Purchases.sharedInstance.cachedCustomerInfo
                ?.entitlements?.active?.containsKey(PREMIUM_ENTITLEMENT) == true
        } catch (e: Exception) {
            false
        }
    }
}
