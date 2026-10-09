package com.buggedlab.pocketops

import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.engine.notification.NotificationCategorizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationCategorizerTest {

    private lateinit var categorizer: NotificationCategorizer

    @Before
    fun setUp() {
        categorizer = NotificationCategorizer()
    }

    @Test
    fun testCriticalSecurityAlertsCategorization() {
        val category = categorizer.categorize(
            packageName = "com.chase.sig.android",
            title = "Security Alert",
            text = "Your one-time verification code is 482910."
        )
        assertEquals(NotificationCategory.CRITICAL, category)

        val isProtected = categorizer.isProtectedNotification(
            packageName = "com.chase.sig.android",
            category = category,
            title = "Security Alert",
            text = "Your one-time verification code is 482910."
        )
        assertTrue("Security alerts must be protected", isProtected)
    }

    @Test
    fun testWorkAlertsCategorization() {
        val category = categorizer.categorize(
            packageName = "com.slack",
            title = "#dev-team",
            text = "Austin: Sprint review meeting starts in 10 minutes."
        )
        assertEquals(NotificationCategory.WORK, category)
    }

    @Test
    fun testPromotionalShoppingCategorization() {
        val category = categorizer.categorize(
            packageName = "com.ubercab.eats",
            title = "Hungry?",
            text = "Get 40% off your next lunch order with promo code LUNCH40!"
        )
        assertEquals(NotificationCategory.SHOPPING, category)
    }

    @Test
    fun testSocialMediaCategorization() {
        val category = categorizer.categorize(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "john_doe liked your post."
        )
        assertEquals(NotificationCategory.SOCIAL, category)
    }

    @Test
    fun testMessagingCategorization() {
        val category = categorizer.categorize(
            packageName = "com.google.android.apps.messaging",
            title = "Mom",
            text = "Are we still having dinner tonight?"
        )
        assertEquals(NotificationCategory.MESSAGES, category)
    }
}
