package com.sortescape.game.meta

/** Section 14: coins (soft currency) and gems (premium currency), backed by SaveManager. */
class CurrencyManager(private val save: SaveManager) {

    val coins: Int get() = save.data.coins
    val gems: Int get() = save.data.gems

    fun addCoins(amount: Int) = save.mutate { it.coins += amount }

    fun spendCoins(amount: Int): Boolean {
        if (save.data.coins < amount) return false
        save.mutate { it.coins -= amount }
        return true
    }

    fun addGems(amount: Int) = save.mutate { it.gems += amount }

    fun spendGems(amount: Int): Boolean {
        if (save.data.gems < amount) return false
        save.mutate { it.gems -= amount }
        return true
    }

    /** Section 13/14: base reward formula for finishing a level. */
    fun rewardForLevelCompletion(stars: Int, score: Int, isPerfect: Boolean): Int {
        var coinsEarned = 50 + stars * 25 + score / 20
        if (isPerfect) coinsEarned += 100
        addCoins(coinsEarned)
        return coinsEarned
    }
}
