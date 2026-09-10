package com.sortescape.game.level

import com.sortescape.game.data.*
import kotlin.random.Random

/**
 * Procedural level configuration by band, mirroring section 7 of the design doc.
 * Levels are never hand-authored: everything comes from these rules plus a seeded RNG.
 */
data class LevelBandConfig(
    val range: IntRange,
    val categoryCount: IntRange,
    val objectCount: IntRange,
    val maxMovesSlack: Int, // extra moves allowed above the theoretical minimum (0 = unlimited)
    val unlimitedMoves: Boolean,
    val lockedChance: Double,
    val wildcardChance: Double,
    val mysteryChance: Double
)

object LevelBands {
    val BANDS = listOf(
        LevelBandConfig(1..10, 2..2, 4..8, 0, unlimitedMoves = true, lockedChance = 0.0, wildcardChance = 0.0, mysteryChance = 0.0),
        LevelBandConfig(11..30, 3..3, 10..15, 6, unlimitedMoves = false, lockedChance = 0.0, wildcardChance = 0.10, mysteryChance = 0.0),
        LevelBandConfig(31..60, 4..4, 15..20, 4, unlimitedMoves = false, lockedChance = 0.18, wildcardChance = 0.12, mysteryChance = 0.0),
        LevelBandConfig(61..100, 4..5, 16..22, 3, unlimitedMoves = false, lockedChance = 0.22, wildcardChance = 0.16, mysteryChance = 0.12),
        LevelBandConfig(101..100000, 5..5, 18..26, 2, unlimitedMoves = false, lockedChance = 0.28, wildcardChance = 0.20, mysteryChance = 0.18)
    )

    fun forLevel(levelId: Int): LevelBandConfig = BANDS.first { levelId in it.range }
}

class LevelGenerator(private val validator: LevelValidator = LevelValidator()) {

    /** Deterministic: the same levelId always produces the same level. */
    fun generate(levelId: Int): LevelData {
        val band = LevelBands.forLevel(levelId)
        var attempt = 0
        var candidate: LevelData
        do {
            val seed = levelId * 7919L + attempt * 104729L
            candidate = build(levelId, band, Random(seed))
            attempt++
        } while (!validator.validate(candidate).solvable && attempt < 25)
        return candidate
    }

    private fun build(levelId: Int, band: LevelBandConfig, rng: Random): LevelData {
        val categoryCount = band.categoryCount.randomIn(rng)
        val categories = ObjectDatabase.SORTABLE_CATEGORIES.shuffled(rng).take(categoryCount)
        val totalObjects = band.objectCount.randomIn(rng)

        // Distribute object count roughly evenly across the chosen categories.
        val perCategory = IntArray(categories.size) { totalObjects / categories.size }
        repeat(totalObjects % categories.size) { perCategory[it]++ }

        val objects = mutableListOf<LevelObjectInstance>()
        var idx = 0
        categories.forEachIndexed { ci, category ->
            val pool = ObjectDatabase.byCategory(category)
            repeat(perCategory[ci]) {
                val base = pool[rng.nextInt(pool.size)]
                objects.add(LevelObjectInstance(instanceId = "obj_${idx++}", objectId = base.id))
            }
        }

        // Wildcard objects: replace a few instances with the wildcard object (belongs anywhere).
        val wildcardCount = (objects.size * band.wildcardChance).toInt()
        repeat(wildcardCount) {
            val i = rng.nextInt(objects.size)
            objects[i] = objects[i].copy(objectId = "wild_gift", special = SpecialType.WILDCARD)
        }

        // Mystery objects: hide identity until revealed (still belongs to its real category).
        val mysteryCount = (objects.size * band.mysteryChance).toInt()
        repeat(mysteryCount) {
            val candidates = objects.indices.filter { objects[it].special == SpecialType.NONE }
            if (candidates.isNotEmpty()) {
                val i = candidates.random(rng)
                objects[i] = objects[i].copy(special = SpecialType.MYSTERY)
            }
        }

        // Locked objects: require another (non-locked, non-wildcard) instance to be sorted first.
        val lockedCount = (objects.size * band.lockedChance).toInt()
        repeat(lockedCount) {
            val lockCandidates = objects.indices.filter { objects[it].special == SpecialType.NONE }
            val prereqCandidates = objects.indices.filter { objects[it].special != SpecialType.LOCKED }
            if (lockCandidates.isNotEmpty() && prereqCandidates.size > 1) {
                val i = lockCandidates.random(rng)
                var prereq = prereqCandidates.random(rng)
                if (prereq == i) prereq = prereqCandidates.first { it != i }
                objects[i] = objects[i].copy(
                    special = SpecialType.LOCKED,
                    requiresInstanceId = objects[prereq].instanceId
                )
            }
        }

        // Containers: one per category. Each container must be able to hold every wildcard in the
        // level in addition to its own real objects - a wildcard can be dropped into ANY container,
        // so if a player piles several into one box before its real objects are sorted, a tighter
        // capacity could fill that box and block a legitimate object (a soft-lock, not a skill issue).
        // Sizing for the worst case (all wildcards in one box) makes that structurally impossible.
        val totalWildcards = objects.count { it.special == SpecialType.WILDCARD }
        val containers = categories.map { category ->
            val realCount = objects.count {
                it.special != SpecialType.WILDCARD && ObjectDatabase.byId(it.objectId).category == category
            }
            val capacity = maxOf(realCount + totalWildcards + 1, 2)
            ContainerData(id = "box_${category.name.lowercase()}", category = category, capacity = capacity)
        }

        val minMoves = objects.size
        val maxMoves = if (band.unlimitedMoves) 0 else minMoves + band.maxMovesSlack

        val difficulty = computeDifficulty(objects.size, categories.size, objects.count { it.special != SpecialType.NONE }, maxMoves, minMoves)

        return LevelData(
            levelId = levelId,
            categories = categories,
            containers = containers,
            objects = objects,
            maxMoves = maxMoves,
            timeLimitSec = 0,
            difficulty = difficulty
        )
    }

    /** Section 11: difficulty combines multiple dimensions, not just object count. */
    private fun computeDifficulty(objectCount: Int, categoryCount: Int, obstacleCount: Int, maxMoves: Int, minMoves: Int): Int {
        val moveRestriction = if (maxMoves == 0) 0 else maxOf(0, 6 - (maxMoves - minMoves))
        val timeRestriction = 0
        return objectCount + categoryCount * 3 + obstacleCount * 2 + moveRestriction * 2 + timeRestriction
    }

    private fun IntRange.randomIn(rng: Random) = if (first == last) first else rng.nextInt(first, last + 1)
}
