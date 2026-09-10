package com.sortescape.game.level

import com.sortescape.game.data.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LevelValidatorTest {

    private val validator = LevelValidator()

    private fun baseLevel(
        objects: List<LevelObjectInstance>,
        containers: List<ContainerData>,
        maxMoves: Int = 0
    ) = LevelData(
        levelId = 1,
        categories = containers.map { it.category },
        containers = containers,
        objects = objects,
        maxMoves = maxMoves,
        timeLimitSec = 0,
        difficulty = 1
    )

    @Test
    fun `rejects an object whose category has no container`() {
        val level = baseLevel(
            objects = listOf(LevelObjectInstance("o1", "food_apple")),
            containers = listOf(ContainerData("box_toys", Category.TOYS, capacity = 4))
        )
        assertFalse(validator.validate(level).solvable)
    }

    @Test
    fun `rejects a container over its real-object capacity`() {
        val objects = (1..5).map { LevelObjectInstance("o$it", "food_apple") }
        val level = baseLevel(objects, listOf(ContainerData("box_food", Category.FOOD, capacity = 2)))
        assertFalse(validator.validate(level).solvable)
    }

    @Test
    fun `rejects a lock cycle between two objects`() {
        val objects = listOf(
            LevelObjectInstance("o1", "food_apple", SpecialType.LOCKED, requiresInstanceId = "o2"),
            LevelObjectInstance("o2", "food_banana", SpecialType.LOCKED, requiresInstanceId = "o1")
        )
        val level = baseLevel(objects, listOf(ContainerData("box_food", Category.FOOD, capacity = 4)))
        assertFalse(validator.validate(level).solvable)
    }

    @Test
    fun `rejects a move budget smaller than the object count`() {
        val objects = (1..5).map { LevelObjectInstance("o$it", "food_apple") }
        val level = baseLevel(objects, listOf(ContainerData("box_food", Category.FOOD, capacity = 10)), maxMoves = 3)
        assertFalse(validator.validate(level).solvable)
    }

    @Test
    fun `accepts a well-formed level`() {
        val objects = listOf(
            LevelObjectInstance("o1", "food_apple"),
            LevelObjectInstance("o2", "toy_ball")
        )
        val containers = listOf(
            ContainerData("box_food", Category.FOOD, capacity = 2),
            ContainerData("box_toys", Category.TOYS, capacity = 2)
        )
        assertTrue(validator.validate(baseLevel(objects, containers)).solvable)
    }
}
