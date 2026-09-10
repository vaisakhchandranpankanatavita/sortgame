package com.sortescape.game.data

/**
 * Full definition of one level (section 42). Produced by LevelGenerator,
 * checked by LevelValidator, and consumed by the gameplay SortManager.
 */
data class LevelData(
    val levelId: Int,
    val categories: List<Category>,
    val containers: List<ContainerData>,
    val objects: List<LevelObjectInstance>,
    val maxMoves: Int,      // 0 = unlimited
    val timeLimitSec: Int,  // 0 = no timer
    val difficulty: Int,
    val theme: String = "Home"
) {
    val objectCount: Int get() = objects.size
    val categoryCount: Int get() = categories.size
}
