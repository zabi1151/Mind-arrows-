package com.example.ads

import com.example.BuildConfig

object AdsConfig {
    /**
     * Centralized AdMob credentials.
     * Replace these with your real AdMob IDs in BuildConfig or here directly.
     */
    val ADMOB_APP_ID: String = BuildConfig.ADMOB_APP_ID.ifEmpty { "ca-app-pub-3940256099942544~3347511713" }
    val BANNER_AD_ID: String = BuildConfig.ADMOB_BANNER_AD_ID.ifEmpty { "ca-app-pub-3940256099942544/6300978111" }
    val INTERSTITIAL_AD_ID: String = BuildConfig.ADMOB_INTERSTITIAL_AD_ID.ifEmpty { "ca-app-pub-3940256099942544/1033173712" }
    val REWARDED_AD_ID: String = BuildConfig.ADMOB_REWARDED_AD_ID.ifEmpty { "ca-app-pub-3940256099942544/5224354917" }
    val APP_OPEN_AD_ID: String = BuildConfig.ADMOB_APP_OPEN_AD_ID.ifEmpty { "ca-app-pub-3940256099942544/9257395921" }

    // Interstitial interval: show every N completed levels
    const val INTERSTITIAL_INTERVAL_LEVELS = 2

    // Configurable Privacy Policy URL
    var PRIVACY_POLICY_URL = "https://zabi1151.github.io/Privacy/"

    // Test mode flag
    const val IS_TEST_MODE = true
}

enum class AdRewardType {
    HINT_BOOST,
    DOUBLE_COINS,
    LIVES_REFILL
}

data class ActiveAdState(
    val isShowing: Boolean = false,
    val adType: String = "", // "rewarded", "interstitial", or "app_open"
    val rewardType: AdRewardType = AdRewardType.DOUBLE_COINS,
    val adUnitId: String = "",
    val secondsRemaining: Int = 5,
    val onCompleted: (() -> Unit)? = null,
    val onDismissed: (() -> Unit)? = null
)
