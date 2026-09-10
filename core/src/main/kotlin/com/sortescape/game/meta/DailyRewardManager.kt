package com.sortescape.game.meta

import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class DailyReward {
    data class Coins(val amount: Int) : DailyReward()
    data class Gems(val amount: Int) : DailyReward()
    data class Theme(val themeId: String) : DailyReward()
}

/** Section 32: a 7-day reward cycle that resets the streak if a day is missed. */
class DailyRewardManager(private val save: SaveManager, private val currency: CurrencyManager) {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val cycle: List<DailyReward> = listOf(
        DailyReward.Coins(100),
        DailyReward.Coins(150),
        DailyReward.Gems(5),
        DailyReward.Coins(200),
        DailyReward.Theme("Kitchen"),
        DailyReward.Gems(10),
        DailyReward.Coins(500)
    )

    fun canClaimToday(today: LocalDate = LocalDate.now()): Boolean {
        val last = lastClaimDate() ?: return true
        return last != today
    }

    fun currentDay(): Int = (save.data.dailyStreak % cycle.size)

    fun claim(today: LocalDate = LocalDate.now()): DailyReward? {
        if (!canClaimToday(today)) return null
        val last = lastClaimDate()
        val brokeStreak = last != null && last.plusDays(1) != today
        val newStreak = if (brokeStreak) 0 else save.data.dailyStreak
        val reward = cycle[newStreak % cycle.size]
        applyReward(reward)
        save.mutate {
            it.dailyRewardDate = today.format(fmt)
            it.dailyStreak = newStreak + 1
        }
        return reward
    }

    private fun applyReward(reward: DailyReward) {
        when (reward) {
            is DailyReward.Coins -> currency.addCoins(reward.amount)
            is DailyReward.Gems -> currency.addGems(reward.amount)
            is DailyReward.Theme -> save.mutate { s ->
                if (!s.unlockedThemes.contains(reward.themeId)) s.unlockedThemes.add(reward.themeId)
            }
        }
    }

    private fun lastClaimDate(): LocalDate? =
        save.data.dailyRewardDate.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it, fmt) }.getOrNull() }
}
