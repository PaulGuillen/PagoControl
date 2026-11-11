package com.devpaul.features.settings.ui

data class PlanUiState(
    val isPremium: Boolean = false,
    val isDebugOverride: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLaunchingPurchase: Boolean = false,
    val purchasesLoaded: Boolean = false,
    val hasPendingPurchase: Boolean = false,
    val isProcessingPurchase: Boolean = false,
    val autoRenewing: Boolean = false,
    val monthlyPrice: String? = null,
    val notice: PlanNotice? = null,
)

enum class PlanNotice {
    ConnectionError,
    ProductUnavailable,
    PurchaseError,
    AcknowledgementError,
    NoPurchases,
    Restored,
    ManagementUnavailable,
    PriceChanged,
}
