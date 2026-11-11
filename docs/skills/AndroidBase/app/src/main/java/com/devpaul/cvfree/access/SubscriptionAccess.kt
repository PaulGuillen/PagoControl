package com.devpaul.cvfree.access

import com.android.billingclient.api.Purchase

internal data class SubscriptionAccess(
    val hasPremiumAccess: Boolean,
    val autoRenewing: Boolean,
    val hasPendingPurchase: Boolean,
    val isProcessingPurchase: Boolean,
)

internal fun resolveSubscriptionAccess(
    purchases: List<Purchase>,
    productId: String,
    acknowledgedTokens: Set<String> = emptySet(),
): SubscriptionAccess {
    val matching = purchases.filter { productId in it.products }
    val purchased = matching.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
    val active = purchased.filter { it.isAcknowledged || it.purchaseToken in acknowledgedTokens }
    return SubscriptionAccess(
        hasPremiumAccess = active.isNotEmpty(),
        autoRenewing = active.any { it.isAutoRenewing },
        hasPendingPurchase = matching.any { it.purchaseState == Purchase.PurchaseState.PENDING },
        isProcessingPurchase = purchased.any { !it.isAcknowledged && it.purchaseToken !in acknowledgedTokens },
    )
}
