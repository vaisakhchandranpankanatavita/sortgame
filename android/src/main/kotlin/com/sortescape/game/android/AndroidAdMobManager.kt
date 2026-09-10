package com.sortescape.game.android

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.sortescape.game.BuildConfig
import com.sortescape.game.ads.AdManager

/**
 * Real Google Mobile Ads (AdMob) wiring behind the AdManager interface (sections 28-31).
 * Ad unit IDs come from BuildConfig, generated from android/build.gradle.kts - both build
 * types currently point at Google's public *test* IDs, so this is safe to build and run
 * before you have a real AdMob account; swap the values in build.gradle.kts's "release"
 * block for your own once you register the app at https://apps.admob.com.
 *
 * Ads never block gameplay (section 31's "the player should never feel forced"): every
 * failure path here calls the caller's completion callback immediately rather than stalling.
 */
class AndroidAdMobManager(private val activity: Activity) : AdManager {

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var adsRemoved = false
    private var levelsSinceLastInterstitial = 0
    private var initialized = false

    init {
        MobileAds.initialize(activity) {
            initialized = true
            loadRewarded()
            loadInterstitial()
        }
    }

    private fun loadRewarded() {
        RewardedAd.load(
            activity, BuildConfig.ADMOB_REWARDED_UNIT_ID, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            activity, BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    override fun isRewardedAdReady(): Boolean = rewardedAd != null

    override fun showRewardedAd(onReward: () -> Unit, onFailedOrCancelled: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            onFailedOrCancelled()
            if (initialized) loadRewarded()
            return
        }

        var earnedReward = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded()
                if (!earnedReward) onFailedOrCancelled()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                loadRewarded()
                onFailedOrCancelled()
            }
        }

        activity.runOnUiThread {
            ad.show(activity) { _ ->
                earnedReward = true
                onReward()
            }
        }
    }

    override fun showInterstitialIfDue(levelsCompletedSinceLastAd: Int, onDone: () -> Unit) {
        if (adsRemoved || levelsCompletedSinceLastAd < AdManager.NO_INTERSTITIAL_BEFORE_LEVEL) {
            onDone()
            return
        }

        levelsSinceLastInterstitial++
        if (levelsSinceLastInterstitial < AdManager.MIN_LEVELS_BETWEEN_INTERSTITIALS) {
            onDone()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            onDone()
            if (initialized) loadInterstitial()
            return
        }

        levelsSinceLastInterstitial = 0
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitial()
                onDone()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                loadInterstitial()
                onDone()
            }
        }
        activity.runOnUiThread { ad.show(activity) }
    }

    override fun isAdsRemoved(): Boolean = adsRemoved

    override fun purchaseRemoveAds(onResult: (Boolean) -> Unit) {
        // Real Play Billing wiring needs an in-app product configured in Play Console first.
        // Stubbed as an immediate success so the settings/store UI flow is testable end to end;
        // wire com.android.billingclient here once that product exists.
        adsRemoved = true
        onResult(true)
    }
}
