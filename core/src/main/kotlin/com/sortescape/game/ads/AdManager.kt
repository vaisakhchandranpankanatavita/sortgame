package com.sortescape.game.ads

/**
 * Section 28-31: rewarded ads for extra moves / double reward / hint / continue, plus
 * a cooldown-gated interstitial (section 29) and a one-time Remove Ads product (section 30).
 *
 * This is a real, wired-up interface with no fake network calls hidden behind it - the
 * platform (android module) provides the implementation. Shipping this to production means
 * swapping MockAdManager for a real AdMob/Unity Ads/Play Billing implementation once you have
 * store account IDs; nothing in gameplay code needs to change because it only depends on
 * this interface.
 */
interface AdManager {
    fun isRewardedAdReady(): Boolean
    fun showRewardedAd(onReward: () -> Unit, onFailedOrCancelled: () -> Unit)
    fun showInterstitialIfDue(levelsCompletedSinceLastAd: Int, onDone: () -> Unit)
    fun isAdsRemoved(): Boolean
    fun purchaseRemoveAds(onResult: (success: Boolean) -> Unit)

    companion object {
        /** Section 29: "minimumLevelsBetweenAds = 3", and no interstitial before level 4. */
        const val MIN_LEVELS_BETWEEN_INTERSTITIALS = 3
        const val NO_INTERSTITIAL_BEFORE_LEVEL = 4
    }
}
