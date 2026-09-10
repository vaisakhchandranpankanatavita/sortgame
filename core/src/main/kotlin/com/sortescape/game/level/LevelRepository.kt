package com.sortescape.game.level

import com.sortescape.game.data.LevelData

/**
 * Generates and caches levels on demand instead of hand-authoring hundreds of boards
 * (section 10: "Do not manually create every level"). 150 levels covers the MVP scope
 * recommended in section 76; LevelBands already supports levels well beyond 150.
 */
class LevelRepository(private val generator: LevelGenerator = LevelGenerator()) {

    companion object {
        const val MVP_LEVEL_COUNT = 150
    }

    private val cache = HashMap<Int, LevelData>()

    fun get(levelId: Int): LevelData = cache.getOrPut(levelId) { generator.generate(levelId) }

    fun totalLevels(): Int = MVP_LEVEL_COUNT
}
