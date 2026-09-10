package com.sortescape.game.score

/** Section 13 scoring rules. Pure logic, no engine dependency, so it's cheap to unit test. */
class ScoreManager {
    var score: Int = 0
        private set
    var combo: Int = 0
        private set
    var mistakes: Int = 0
        private set
    var maxCombo: Int = 0
        private set

    private fun comboMultiplier(combo: Int): Int = when {
        combo >= 5 -> 5
        combo >= 4 -> 4
        combo >= 3 -> 3
        combo >= 2 -> 2
        else -> 1
    }

    /** @return points awarded for this single correct sort (including combo multiplier + fast bonus). */
    fun onCorrectSort(reactionTimeSec: Float): Int {
        combo++
        maxCombo = maxOf(maxCombo, combo)
        val fastBonus = if (reactionTimeSec < 1.5f) 20 else 0
        val points = (100 + fastBonus) * comboMultiplier(combo)
        score += points
        return points
    }

    fun onWrongSort() {
        combo = 0
        mistakes++
    }

    /** Section 13 "Perfect Level" bonus, applied once when a level finishes with zero mistakes. */
    fun onLevelComplete(): Int {
        if (mistakes == 0) {
            score += 500
            return 500
        }
        return 0
    }

    fun isPerfect(): Boolean = mistakes == 0

    /** Section 14 stars: 1 star for completion, up to 3 for a clean/near-clean run. */
    fun starsEarned(): Int = when {
        mistakes == 0 -> 3
        mistakes <= 2 -> 2
        else -> 1
    }

    fun reset() {
        score = 0
        combo = 0
        mistakes = 0
        maxCombo = 0
    }
}
