package com.sortescape.game.level

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class LevelGeneratorTest {

    private val generator = LevelGenerator()
    private val validator = LevelValidator()

    @Test
    fun `every MVP level 1 through 150 is solvable`() {
        for (levelId in 1..150) {
            val level = generator.generate(levelId)
            val result = validator.validate(level)
            assertTrue(result.solvable, "Level $levelId not solvable: ${result.reasons}")
        }
    }

    @Test
    fun `generation is deterministic for a given level id`() {
        val a = generator.generate(42)
        val b = generator.generate(42)
        assertEquals(a.objects.map { it.objectId }, b.objects.map { it.objectId })
        assertEquals(a.containers.map { it.id }, b.containers.map { it.id })
    }

    @Test
    fun `early levels have unlimited moves and only two categories`() {
        val level = generator.generate(1)
        assertEquals(0, level.maxMoves)
        assertEquals(2, level.categoryCount)
        assertTrue(level.objectCount in 4..8)
    }

    @Test
    fun `late levels introduce move limits and more categories`() {
        val level = generator.generate(120)
        assertTrue(level.maxMoves > 0)
        assertTrue(level.categoryCount >= 5)
    }

    @Test
    fun `difficulty generally trends upward across bands`() {
        val early = generator.generate(5).difficulty
        val mid = generator.generate(45).difficulty
        val late = generator.generate(140).difficulty
        assertTrue(early < mid, "expected early ($early) < mid ($mid)")
        assertTrue(mid < late, "expected mid ($mid) < late ($late)")
    }
}
