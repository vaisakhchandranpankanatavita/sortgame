package com.sortescape.game.meta

data class Achievement(val id: String, val name: String, val description: String)

/** Section 35 examples, implemented against stats already tracked in SaveData. */
object Achievements {
    val ALL = listOf(
        Achievement("first_sort", "First Sort", "Complete your first level."),
        Achievement("speed_sorter", "Speed Sorter", "Complete a level in under 10 seconds."),
        Achievement("perfect_10", "Perfect", "Complete 10 perfect levels."),
        Achievement("combo_master", "Combo Master", "Get a x10 combo."),
        Achievement("collector", "Collector", "Unlock 5 themes."),
        Achievement("addict", "Addict", "Play 100 levels.")
    )
}

class AchievementManager(private val save: SaveManager) {

    fun unlocked(): Set<String> = save.data.unlockedAchievements.toSet()

    private fun unlock(id: String) {
        save.mutate { if (!it.unlockedAchievements.contains(id)) it.unlockedAchievements.add(id) }
    }

    /** Call after each level completes; returns newly unlocked achievements to celebrate in the UI. */
    fun onLevelCompleted(levelDurationSec: Float, isPerfect: Boolean, maxCombo: Int): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        val already = unlocked()

        fun grant(id: String) {
            if (id !in already) {
                unlock(id)
                newlyUnlocked.add(Achievements.ALL.first { it.id == id })
            }
        }

        save.mutate { it.levelsPlayed += 1 }
        if (save.data.levelsPlayed >= 1) grant("first_sort")
        if (levelDurationSec < 10f) grant("speed_sorter")
        if (isPerfect) {
            save.mutate { it.perfectLevelCount += 1 }
            if (save.data.perfectLevelCount >= 10) grant("perfect_10")
        }
        if (maxCombo >= 10) grant("combo_master")
        if (save.data.unlockedThemes.size >= 5) grant("collector")
        if (save.data.levelsPlayed >= 100) grant("addict")

        save.mutate { it.bestCombo = maxOf(it.bestCombo, maxCombo) }
        return newlyUnlocked
    }
}
