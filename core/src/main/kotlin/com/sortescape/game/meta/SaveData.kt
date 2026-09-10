package com.sortescape.game.meta

/**
 * Section 43: everything that must survive an app restart. Plain data class so it can be
 * serialized with libGDX's built-in Json class (no extra dependency needed).
 */
class SaveData {
    @JvmField var currentLevel: Int = 1
    @JvmField var coins: Int = 0
    @JvmField var gems: Int = 0
    @JvmField var totalStars: Int = 0
    @JvmField var unlockedThemes: MutableList<String> = mutableListOf("Home")
    @JvmField var equippedTheme: String = "Home"
    @JvmField var unlockedAchievements: MutableList<String> = mutableListOf()
    @JvmField var dailyRewardDate: String = ""
    @JvmField var dailyStreak: Int = 0
    @JvmField var soundEnabled: Boolean = true
    @JvmField var musicEnabled: Boolean = true
    @JvmField var hapticsEnabled: Boolean = true
    @JvmField var adsRemoved: Boolean = false
    @JvmField var perfectLevelCount: Int = 0
    @JvmField var levelsPlayed: Int = 0
    @JvmField var bestCombo: Int = 0
}
