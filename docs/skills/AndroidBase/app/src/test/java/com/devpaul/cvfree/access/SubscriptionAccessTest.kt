package com.devpaul.cvfree.access

import com.android.billingclient.api.Purchase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SubscriptionAccessTest {
    @Test
    fun `pending payment never grants premium`() {
        val access = resolveSubscriptionAccess(listOf(purchase(state = Purchase.PurchaseState.PENDING)), PRODUCT_ID)

        assertFalse(access.hasPremiumAccess)
        assertTrue(access.hasPendingPurchase)
        assertFalse(access.isProcessingPurchase)
    }

    @Test
    fun `purchase waits for successful acknowledgement`() {
        val purchase = purchase(acknowledged = false)
        val before = resolveSubscriptionAccess(listOf(purchase), PRODUCT_ID)
        val after = resolveSubscriptionAccess(listOf(purchase), PRODUCT_ID, setOf("token"))

        assertFalse(before.hasPremiumAccess)
        assertTrue(before.isProcessingPurchase)
        assertTrue(after.hasPremiumAccess)
        assertFalse(after.isProcessingPurchase)
    }

    @Test
    fun `canceled renewal retains premium while Play returns the purchase`() {
        val access = resolveSubscriptionAccess(listOf(purchase(autoRenewing = false)), PRODUCT_ID)

        assertTrue(access.hasPremiumAccess)
        assertFalse(access.autoRenewing)
    }

    @Test
    fun `expired purchase missing from Play removes access even with a known token`() {
        val access = resolveSubscriptionAccess(emptyList(), PRODUCT_ID, setOf("token"))

        assertFalse(access.hasPremiumAccess)
        assertFalse(access.autoRenewing)
    }

    @Test
    fun `other products and unspecified purchases do not grant premium`() {
        val access = resolveSubscriptionAccess(
            listOf(purchase(productId = "other"), purchase(state = Purchase.PurchaseState.UNSPECIFIED_STATE)),
            PRODUCT_ID,
        )

        assertFalse(access.hasPremiumAccess)
        assertFalse(access.hasPendingPurchase)
    }

    @Test
    fun `restored acknowledged purchase grants renewable premium`() {
        val access = resolveSubscriptionAccess(listOf(purchase()), PRODUCT_ID)

        assertTrue(access.hasPremiumAccess)
        assertTrue(access.autoRenewing)
    }

    private fun purchase(
        productId: String = PRODUCT_ID,
        state: Int = Purchase.PurchaseState.PURCHASED,
        acknowledged: Boolean = true,
        autoRenewing: Boolean = true,
    ): Purchase = mock(Purchase::class.java).also { purchase ->
        `when`(purchase.products).thenReturn(listOf(productId))
        `when`(purchase.purchaseState).thenReturn(state)
        `when`(purchase.isAcknowledged).thenReturn(acknowledged)
        `when`(purchase.isAutoRenewing).thenReturn(autoRenewing)
        `when`(purchase.purchaseToken).thenReturn("token")
    }

    private companion object {
        const val PRODUCT_ID = "cvfree_premium"
    }
}
