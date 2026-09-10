package com.sortescape.game.ads

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Timer

/**
 * Default no-network implementation so the full monetization flow (section 31) is playable
 * and testable end to end today. It simulates a short "ad" delay then always rewards, which
 * lets you verify every rewarded-ad hook in the game before an AdMob account exists.
 * Swap this out (see AdManager) once real ad unit IDs are available.
 */
class MockAdManager(private var adsRemoved: Boolean = false) : AdManager {

    private var levelsSinceLastInterstitial = 0

    override fun isRewardedAdReady(): Boolean = true

    override fun showRewardedAd(onReward: () -> Unit, onFailedOrCancelled: () -> Unit) {
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                Gdx.app.postRunnable { onReward() }
            }
        }, 1.2f)
    }

    override fun showInterstitialIfDue(levelsCompletedSinceLastAd: Int, onDone: () -> Unit) {
        if (adsRemoved) {
            onDone()
            return
        }
        levelsSinceLastInterstitial++
        val due = levelsSinceLastInterstitial >= AdManager.MIN_LEVELS_BETWEEN_INTERSTITIALS
        if (due) levelsSinceLastInterstitial = 0
        // No real network ad shown in mock mode; still respects the cooldown so timing is testable.
        onDone()
    }

    override fun isAdsRemoved(): Boolean = adsRemoved

    override fun purchaseRemoveAds(onResult: (Boolean) -> Unit) {
        adsRemoved = true
        onResult(true)
    }
}
