package com.buggedlab.pocketops.engine.notification

import android.app.Notification
import com.buggedlab.pocketops.core.model.NotificationCategory

class NotificationCategorizer {

    private val criticalPackages = setOf(
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        "com.android.phone",
        "com.android.server.telecom",
        "com.google.android.apps.authenticator2",
        "com.duosecurity.duomobile",
        "com.authy.authy",
        "com.microsoft.msa.authenticator",
        "com.bitwarden",
        "com.1password",
        "com.chase.sig.android",
        "com.infonow.bofa",
        "com.wf.wellsfargomobile"
    )

    private val workPackages = setOf(
        "com.slack",
        "com.microsoft.teams",
        "com.microsoft.office.outlook",
        "com.google.android.apps.meetings",
        "com.zoom.videomeetings",
        "com.atlassian.jira",
        "com.github.android"
    )

    private val messagePackages = setOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.whatsapp",
        "org.thoughtcrime.securesms",
        "org.telegram.messenger",
        "com.discord"
    )

    private val shoppingPackages = setOf(
        "com.amazon.mShop.android.shopping",
        "com.ebay.mobile",
        "com.target.ui",
        "com.walmart.android",
        "com.ubercab.eats",
        "com.dd.doordash",
        "com.grubhub.android",
        "com.einnovation.temu"
    )

    private val socialPackages = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.twitter.android",
        "com.facebook.katana",
        "com.snapchat.android",
        "com.reddit.frontpage",
        "com.linkedin.android"
    )

    private val entertainmentPackages = setOf(
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.spotify.music",
        "tv.twitch.android",
        "com.disney.disneyplus",
        "com.hbo.hbonow"
    )

    private val promoKeywords = listOf(
        "off", "discount", "sale", "deal", "coupon", "promo", "save up to", "special offer",
        "limited time", "flash sale", "order now", "free shipping", "cashback", "exclusive"
    )

    private val criticalKeywords = listOf(
        "verification code", "security alert", "fraud", "unauthorized", "login attempt",
        "two-factor", "2fa", "otp", "passcode", "emergency", "tornado", "amber alert", "warning"
    )

    fun categorize(
        packageName: String,
        title: String,
        text: String,
        subText: String? = null,
        systemCategory: String? = null,
        channelId: String? = null
    ): NotificationCategory {
        val fullContent = "$title $text ${subText ?: ""}".lowercase()

        // 1. Check for Critical security & 2FA keywords first
        if (criticalKeywords.any { fullContent.contains(it) } || criticalPackages.contains(packageName)) {
            return NotificationCategory.CRITICAL
        }

        // 2. Check for Promotional & Shopping keywords
        if (promoKeywords.any { fullContent.contains(it) }) {
            return NotificationCategory.SHOPPING
        }

        // 3. Check System Category Hints from Android OS
        when (systemCategory) {
            Notification.CATEGORY_CALL -> return NotificationCategory.CRITICAL
            Notification.CATEGORY_ALARM -> return NotificationCategory.CRITICAL
            Notification.CATEGORY_MESSAGE -> return NotificationCategory.MESSAGES
            Notification.CATEGORY_PROMO -> return NotificationCategory.SHOPPING
            Notification.CATEGORY_SOCIAL -> return NotificationCategory.SOCIAL
            Notification.CATEGORY_WORKOUT, Notification.CATEGORY_PROGRESS -> return NotificationCategory.SYSTEM
            Notification.CATEGORY_SYSTEM -> return NotificationCategory.SYSTEM
        }

        // 4. Package matches
        if (workPackages.contains(packageName)) return NotificationCategory.WORK
        if (messagePackages.contains(packageName)) return NotificationCategory.MESSAGES
        if (shoppingPackages.contains(packageName)) return NotificationCategory.SHOPPING
        if (socialPackages.contains(packageName)) return NotificationCategory.SOCIAL
        if (entertainmentPackages.contains(packageName)) return NotificationCategory.ENTERTAINMENT

        // 5. System packages
        if (packageName.startsWith("android") || packageName.startsWith("com.android") || packageName.startsWith("com.google.android.gms")) {
            return NotificationCategory.SYSTEM
        }

        return NotificationCategory.UNKNOWN
    }

    fun isProtectedNotification(
        packageName: String,
        category: NotificationCategory,
        title: String,
        text: String
    ): Boolean {
        if (category == NotificationCategory.CRITICAL) return true
        if (criticalPackages.contains(packageName)) return true
        val content = "$title $text".lowercase()
        return criticalKeywords.any { content.contains(it) }
    }
}
