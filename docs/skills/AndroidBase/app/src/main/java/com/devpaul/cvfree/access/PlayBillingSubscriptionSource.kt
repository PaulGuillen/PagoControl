package com.devpaul.cvfree.access

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.devpaul.cvfree.BuildConfig
import com.devpaul.features.settings.ui.PlanNotice
import com.devpaul.features.settings.ui.PlanUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single([PlayBillingSubscriptionSource::class, PremiumEntitlementSource::class])
internal class PlayBillingSubscriptionSource(context: Context) : PremiumEntitlementSource {
    private val debugOverride = BuildConfig.DEBUG && BuildConfig.PREMIUM_ACCESS_OVERRIDE
    private val premiumAccess = MutableStateFlow(debugOverride)
    override val hasPremiumAccess = premiumAccess.asStateFlow()
    override val declaredPlan: AccessPlan
        get() = if (premiumAccess.value) AccessPlan.Premium else AccessPlan.Free

    private val mutableState = MutableStateFlow(
        PlanUiState(isPremium = debugOverride, isDebugOverride = debugOverride),
    )
    val state = mutableState.asStateFlow()
    private var connecting = false
    private var queryingPurchases = false
    private var refreshQueued = false
    private var restoreRequested = false
    private val acknowledgingTokens = mutableSetOf<String>()
    private val acknowledgedTokens = mutableSetOf<String>()
    private var latestPurchases = emptyList<Purchase>()
    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener { result, _ ->
            mutableState.update { it.copy(isLaunchingPurchase = false) }
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK,
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refresh()
                BillingClient.BillingResponseCode.USER_CANCELED -> Unit
                else -> showNotice(PlanNotice.PurchaseError)
            }
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    fun refresh(restore: Boolean = false) {
        restoreRequested = restoreRequested || restore
        if (connecting) return
        mutableState.update { it.copy(isRefreshing = true, notice = null) }
        if (billingClient.isReady) {
            queryPurchases()
            queryOffer()
            return
        }
        connecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connecting = false
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                    queryOffer()
                } else {
                    restoreRequested = false
                    mutableState.update { it.copy(isRefreshing = false, notice = PlanNotice.ConnectionError) }
                }
            }

            override fun onBillingServiceDisconnected() {
                connecting = false
            }
        })
    }

    private fun queryPurchases() {
        if (queryingPurchases) {
            refreshQueued = true
            return
        }
        queryingPurchases = true
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
        ) { result, purchases ->
            queryingPurchases = false
            val restoring = restoreRequested
            restoreRequested = false
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                latestPurchases = purchases.filter { BuildConfig.PREMIUM_PRODUCT_ID in it.products }
                updateEntitlement()
                latestPurchases.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
                }.forEach(::acknowledge)
                mutableState.update {
                    it.copy(
                        isRefreshing = false,
                        purchasesLoaded = true,
                        notice = if (restoring) {
                            if (latestPurchases.any { purchase ->
                                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                                }) PlanNotice.Restored else PlanNotice.NoPurchases
                        } else it.notice,
                    )
                }
            } else {
                mutableState.update { it.copy(isRefreshing = false, notice = PlanNotice.ConnectionError) }
            }
            if (refreshQueued) {
                refreshQueued = false
                queryPurchases()
            }
        }
    }

    private fun updateEntitlement() {
        val access = resolveSubscriptionAccess(latestPurchases, BuildConfig.PREMIUM_PRODUCT_ID, acknowledgedTokens)
        premiumAccess.value = debugOverride || access.hasPremiumAccess
        mutableState.update {
            it.copy(
                isPremium = premiumAccess.value,
                autoRenewing = access.autoRenewing,
                hasPendingPurchase = access.hasPendingPurchase,
                isProcessingPurchase = access.isProcessingPurchase,
            )
        }
    }

    private fun acknowledge(purchase: Purchase) {
        if (purchase.purchaseToken in acknowledgedTokens || !acknowledgingTokens.add(purchase.purchaseToken)) return
        billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
        ) { result ->
            acknowledgingTokens.remove(purchase.purchaseToken)
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                acknowledgedTokens.add(purchase.purchaseToken)
                updateEntitlement()
            } else {
                showNotice(PlanNotice.AcknowledgementError)
            }
        }
    }

    private fun queryOffer(onOffer: ((ProductDetails, ProductDetails.SubscriptionOfferDetails) -> Unit)? = null) {
        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BuildConfig.PREMIUM_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                ),
            ).build(),
        ) { result, details ->
            val product = details.productDetailsList.firstOrNull { it.productId == BuildConfig.PREMIUM_PRODUCT_ID }
            val offer = product?.subscriptionOfferDetails?.firstOrNull {
                it.basePlanId == BuildConfig.PREMIUM_BASE_PLAN_ID && it.offerId == null &&
                    it.pricingPhases.pricingPhaseList.size == 1 &&
                    it.pricingPhases.pricingPhaseList.single().billingPeriod == "P1M" &&
                    it.pricingPhases.pricingPhaseList.single().recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING
            }
            if (result.responseCode != BillingClient.BillingResponseCode.OK || product == null || offer == null) {
                mutableState.update {
                    it.copy(monthlyPrice = null, isLaunchingPurchase = false, notice = PlanNotice.ProductUnavailable)
                }
            } else {
                mutableState.update { it.copy(monthlyPrice = offer.pricingPhases.pricingPhaseList.single().formattedPrice) }
                onOffer?.invoke(product, offer)
            }
        }
    }

    fun purchase(activity: Activity) {
        val current = state.value
        if (current.isPremium || current.hasPendingPurchase || current.isProcessingPurchase || current.isLaunchingPurchase ||
            current.isRefreshing || !current.purchasesLoaded || current.monthlyPrice == null
        ) return
        if (!billingClient.isReady) {
            refresh()
            return
        }
        mutableState.update { it.copy(isLaunchingPurchase = true, notice = null) }
        queryOffer { product, offer ->
            if (activity.isFinishing || activity.isDestroyed) {
                mutableState.update { it.copy(isLaunchingPurchase = false) }
                return@queryOffer
            }
            if (offer.pricingPhases.pricingPhaseList.single().formattedPrice != current.monthlyPrice) {
                mutableState.update { it.copy(isLaunchingPurchase = false, notice = PlanNotice.PriceChanged) }
                return@queryOffer
            }
            val result = billingClient.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder().setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product).setOfferToken(offer.offerToken).build(),
                    ),
                ).build(),
            )
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                mutableState.update { it.copy(isLaunchingPurchase = false, notice = PlanNotice.PurchaseError) }
                if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) refresh()
            }
        }
    }

    fun manageSubscription(context: Context) {
        val uri = Uri.parse("https://play.google.com/store/account/subscriptions").buildUpon()
            .appendQueryParameter("sku", BuildConfig.PREMIUM_PRODUCT_ID)
            .appendQueryParameter("package", context.packageName)
            .build()
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            showNotice(PlanNotice.ManagementUnavailable)
        }
    }

    fun showNotice(notice: PlanNotice) {
        mutableState.update { it.copy(notice = notice) }
    }
}
