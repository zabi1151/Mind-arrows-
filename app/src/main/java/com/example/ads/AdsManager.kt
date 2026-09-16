package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AdsManager {
    private const val TAG = "AdsManager"
    private val _activeAd = MutableStateFlow<ActiveAdState?>(null)
    val activeAd: StateFlow<ActiveAdState?> = _activeAd.asStateFlow()

    private var levelsSinceLastInterstitial = 0
    var isPremiumAdFree = false

    private var countdownJob: Job? = null

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var appContext: Context? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        try {
            val requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    MobileAds.initialize(context) { status ->
                        isInitialized = true
                        Log.d(TAG, "MobileAds initialized: $status")
                        loadRewardedAd()
                        loadInterstitialAd()
                        loadAppOpenAd()
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "MobileAds async init error", e)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "MobileAds initialization error", e)
        }
    }

    fun loadAppOpenAd() {
        val ctx = appContext ?: return
        if (appOpenAd != null) return
        try {
            val adRequest = AdRequest.Builder().build()
            AppOpenAd.load(
                ctx,
                AdsConfig.APP_OPEN_AD_ID,
                adRequest,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        Log.d(TAG, "AdMob AppOpenAd loaded successfully")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        appOpenAd = null
                        Log.w(TAG, "AdMob AppOpenAd failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            Log.w(TAG, "Failed loading AppOpen ad", e)
        }
    }

    fun showAppOpenAd(activity: Activity?, onDismiss: () -> Unit = {}) {
        if (isPremiumAdFree) {
            onDismiss()
            return
        }

        val ad = appOpenAd
        if (ad != null && activity != null) {
            try {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "AdMob AppOpenAd dismissed")
                        appOpenAd = null
                        loadAppOpenAd()
                        onDismiss()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.w(TAG, "AdMob AppOpenAd failed to show: ${adError.message}")
                        appOpenAd = null
                        loadAppOpenAd()
                        onDismiss()
                    }
                }
                ad.show(activity)
            } catch (e: Throwable) {
                Log.w(TAG, "Error displaying AdMob AppOpenAd, showing fallback", e)
                appOpenAd = null
                loadAppOpenAd()
                showFallbackAppOpen(onDismiss)
            }
        } else {
            showFallbackAppOpen(onDismiss)
            loadAppOpenAd()
        }
    }

    private fun showFallbackAppOpen(onDismiss: () -> Unit) {
        countdownJob?.cancel()
        _activeAd.value = ActiveAdState(
            isShowing = true,
            adType = "app_open",
            adUnitId = AdsConfig.APP_OPEN_AD_ID,
            secondsRemaining = 3,
            onDismissed = onDismiss
        )
        countdownJob = CoroutineScope(Dispatchers.Default).launch {
            for (i in 3 downTo 1) {
                delay(1000)
                val current = _activeAd.value ?: break
                _activeAd.value = current.copy(secondsRemaining = i - 1)
            }
        }
    }

    fun loadRewardedAd() {
        val ctx = appContext ?: return
        if (rewardedAd != null) return
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                ctx,
                AdsConfig.REWARDED_AD_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        Log.d(TAG, "AdMob RewardedAd loaded successfully")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        Log.w(TAG, "AdMob RewardedAd failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            Log.w(TAG, "Failed loading rewarded ad", e)
        }
    }

    fun loadInterstitialAd() {
        val ctx = appContext ?: return
        if (interstitialAd != null) return
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                ctx,
                AdsConfig.INTERSTITIAL_AD_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        Log.d(TAG, "AdMob InterstitialAd loaded successfully")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        Log.w(TAG, "AdMob InterstitialAd failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            Log.w(TAG, "Failed loading interstitial ad", e)
        }
    }

    fun showRewardedAd(
        rewardType: AdRewardType = AdRewardType.DOUBLE_COINS,
        activity: Activity? = null,
        onRewardGranted: () -> Unit
    ) {
        if (isPremiumAdFree) {
            onRewardGranted()
            return
        }

        val ad = rewardedAd
        if (ad != null && activity != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                    showFallbackRewardedAd(rewardType, onRewardGranted)
                }
            }
            try {
                ad.show(activity) {
                    onRewardGranted()
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Ad playback error, falling back to simulated rewarded ad", e)
                rewardedAd = null
                loadRewardedAd()
                showFallbackRewardedAd(rewardType, onRewardGranted)
            }
            return
        }

        showFallbackRewardedAd(rewardType, onRewardGranted)
        loadRewardedAd()
    }

    private fun showFallbackRewardedAd(
        rewardType: AdRewardType,
        onRewardGranted: () -> Unit
    ) {
        countdownJob?.cancel()
        _activeAd.value = ActiveAdState(
            isShowing = true,
            adType = "rewarded",
            rewardType = rewardType,
            adUnitId = AdsConfig.REWARDED_AD_ID,
            secondsRemaining = 4,
            onCompleted = onRewardGranted
        )

        countdownJob = CoroutineScope(Dispatchers.Default).launch {
            for (i in 4 downTo 1) {
                delay(1000)
                val current = _activeAd.value ?: break
                _activeAd.value = current.copy(secondsRemaining = i - 1)
            }
        }
    }

    fun maybeShowInterstitial(
        activity: Activity? = null,
        onDismiss: () -> Unit
    ) {
        if (isPremiumAdFree) {
            onDismiss()
            return
        }

        levelsSinceLastInterstitial++
        if (levelsSinceLastInterstitial >= AdsConfig.INTERSTITIAL_INTERVAL_LEVELS) {
            levelsSinceLastInterstitial = 0

            val ad = interstitialAd
            if (ad != null && activity != null) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitialAd()
                        onDismiss()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                        loadInterstitialAd()
                        showFallbackInterstitial(onDismiss)
                    }
                }
                try {
                    ad.show(activity)
                } catch (e: Throwable) {
                    Log.w(TAG, "Interstitial playback error, falling back to simulated ad", e)
                    interstitialAd = null
                    loadInterstitialAd()
                    showFallbackInterstitial(onDismiss)
                }
                return
            }

            showFallbackInterstitial(onDismiss)
            loadInterstitialAd()
        } else {
            onDismiss()
        }
    }

    private fun showFallbackInterstitial(onDismiss: () -> Unit) {
        countdownJob?.cancel()
        _activeAd.value = ActiveAdState(
            isShowing = true,
            adType = "interstitial",
            adUnitId = AdsConfig.INTERSTITIAL_AD_ID,
            secondsRemaining = 3,
            onDismissed = onDismiss
        )
        countdownJob = CoroutineScope(Dispatchers.Default).launch {
            for (i in 3 downTo 1) {
                delay(1000)
                val current = _activeAd.value ?: break
                _activeAd.value = current.copy(secondsRemaining = i - 1)
            }
        }
    }

    fun dismissAd(rewardClaimed: Boolean = false) {
        countdownJob?.cancel()
        val current = _activeAd.value
        if (rewardClaimed || current?.adType == "interstitial" || (current?.secondsRemaining ?: 1) <= 0) {
            current?.onCompleted?.invoke()
        }
        current?.onDismissed?.invoke()
        _activeAd.value = null
    }
}
