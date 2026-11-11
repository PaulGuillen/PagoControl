package com.devpaul.cvfree.access

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.MutableStateFlow

class FeatureAccessControllerTest {

    @Test
    fun `free plan keeps AI enabled while premium paywall is disabled`() {
        val access = resolveFeatureAccess(
            entitlementSource = entitlement(AccessPlan.Free, premium = false),
            flags = FeatureFlags(
                aiImportEnabled = true,
                premiumPaywallEnabled = false,
            ),
        )

        assertTrue(access.aiResumeImportEnabled)
        assertEquals(AccessPlan.Free, access.plan)
    }

    @Test
    fun `free plan keeps weekly AI access when premium paywall is enabled`() {
        val access = resolveFeatureAccess(
            entitlementSource = entitlement(AccessPlan.Free, premium = false),
            flags = FeatureFlags(
                aiImportEnabled = true,
                premiumPaywallEnabled = true,
            ),
        )

        assertTrue(access.aiResumeImportEnabled)
    }

    @Test
    fun `premium entitlement keeps AI enabled behind paywall`() {
        val access = resolveFeatureAccess(
            entitlementSource = entitlement(AccessPlan.Premium, premium = true),
            flags = FeatureFlags(
                aiImportEnabled = true,
                premiumPaywallEnabled = true,
            ),
        )

        assertTrue(access.aiResumeImportEnabled)
        assertEquals(AccessPlan.Premium, access.plan)
    }

    @Test
    fun `global AI switch disables imports for every plan`() {
        val access = resolveFeatureAccess(
            entitlementSource = entitlement(AccessPlan.Premium, premium = true),
            flags = FeatureFlags(
                aiImportEnabled = false,
                premiumPaywallEnabled = false,
            ),
        )

        assertFalse(access.aiResumeImportEnabled)
    }

    private fun entitlement(
        plan: AccessPlan,
        premium: Boolean,
    ): PremiumEntitlementSource = object : PremiumEntitlementSource {
        override val declaredPlan: AccessPlan = plan
        override val hasPremiumAccess = MutableStateFlow(premium)
    }
}
