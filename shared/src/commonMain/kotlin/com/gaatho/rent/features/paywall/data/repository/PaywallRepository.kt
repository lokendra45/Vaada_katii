package com.gaatho.rent.features.paywall.data.repository

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
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
    suspend fun getOfferings(): ApiResponse<List<StoreProduct>>

    /**
     * Initiates a purchase for the given [product].
     * Returns a Sandwich [ApiResponse] wrapping the [StoreTransaction] on success.
     */
    suspend fun purchase(product: StoreProduct): ApiResponse<StoreTransaction>

    /**
     * Restores purchases previously made by this user on any device.
     */
    suspend fun restorePurchases(): ApiResponse<CustomerInfo>

    /**
     * Whether the user currently has an active Premium entitlement (non-reactive).
     */
    fun hasPremiumAccess(): Boolean
}

class RevenueCatPaywallRepository : PaywallRepository {

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: Flow<Boolean> = _isPremium.asStateFlow()

    init {
        try {
            // Set delegate to listen for CustomerInfo updates from RevenueCat KMP
            Purchases.sharedInstance.delegate = object : PurchasesDelegate {
                override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
                    _isPremium.value = customerInfo.entitlements.all[PREMIUM_ENTITLEMENT]?.isActive == true
                }

                override fun onPurchasePromoProduct(
                    product: StoreProduct,
                    startPurchase: (onError: (error: com.revenuecat.purchases.kmp.models.PurchasesError, userCancelled: Boolean) -> Unit, onSuccess: (transaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit) -> Unit
                ) {
                    // Optional promo product purchase handling
                }
            }
            // Fetch initial status
            Purchases.sharedInstance.getCustomerInfo(
                onError = { _isPremium.value = false },
                onSuccess = { customerInfo ->
                    _isPremium.value = customerInfo.entitlements.all[PREMIUM_ENTITLEMENT]?.isActive == true
                }
            )
        } catch (e: Exception) {
            // Purchases not configured yet or in unit tests
        }
    }

    override suspend fun getOfferings(): ApiResponse<List<StoreProduct>> =
        ApiResponse.suspendOf {
            suspendCancellableCoroutine { cont ->
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
        }

    override suspend fun purchase(product: StoreProduct): ApiResponse<StoreTransaction> =
        ApiResponse.suspendOf {
            suspendCancellableCoroutine { cont ->
                Purchases.sharedInstance.purchase(
                    storeProduct = product,
                    onError = { error, _ -> cont.resumeWithException(Exception(error.message)) },
                    onSuccess = { transaction, _ -> cont.resume(transaction) }
                )
            }
        }

    override suspend fun restorePurchases(): ApiResponse<CustomerInfo> =
        ApiResponse.suspendOf {
            suspendCancellableCoroutine { cont ->
                Purchases.sharedInstance.restorePurchases(
                    onError = { error -> cont.resumeWithException(Exception(error.message)) },
                    onSuccess = { customerInfo ->
                        _isPremium.value = customerInfo.entitlements.all[PREMIUM_ENTITLEMENT]?.isActive == true
                        cont.resume(customerInfo)
                    }
                )
            }
        }

    override fun hasPremiumAccess(): Boolean {
        return _isPremium.value
    }
}
