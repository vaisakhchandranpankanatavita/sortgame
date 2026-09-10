package com.sortescape.game.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScoreManagerTest {

    @Test
    fun `combo multiplier scales as documented`() {
        val sm = ScoreManager()
        val p1 = sm.onCorrectSort(2f) // combo 1 -> x1
        val p2 = sm.onCorrectSort(2f) // combo 2 -> x2
        val p3 = sm.onCorrectSort(2f) // combo 3 -> x3
        assertEquals(100, p1)
        assertEquals(200, p2)
        assertEquals(300, p3)
    }

    @Test
    fun `fast sort adds a bonus under 1_5 seconds`() {
        val sm = ScoreManager()
        val fast = sm.onCorrectSort(0.5f)
        assertEquals(120, fast) // (100 + 20) * combo(1)
    }

    @Test
    fun `wrong sort resets combo and counts a mistake`() {
        val sm = ScoreManager()
        sm.onCorrectSort(2f)
        sm.onCorrectSort(2f)
        sm.onWrongSort()
        assertEquals(0, sm.combo)
        assertEquals(1, sm.mistakes)
        val afterReset = sm.onCorrectSort(2f)
        assertEquals(100, afterReset) // back to x1
    }

    @Test
    fun `perfect level grants the 500 bonus only with zero mistakes`() {
        val sm = ScoreManager()
        sm.onCorrectSort(2f)
        assertEquals(500, sm.onLevelComplete())
        assertTrue(sm.isPerfect())
        assertEquals(3, sm.starsEarned())
    }

    @Test
    fun `mistakes reduce stars but not below one`() {
        val sm = ScoreManager()
        repeat(5) { sm.onWrongSort() }
        assertEquals(1, sm.starsEarned())
        assertEquals(0, sm.onLevelComplete())
    }
}
