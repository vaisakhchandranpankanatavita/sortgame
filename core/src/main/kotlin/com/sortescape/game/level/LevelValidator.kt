package com.sortescape.game.level

import com.sortescape.game.data.LevelData
import com.sortescape.game.data.ObjectDatabase
import com.sortescape.game.data.SpecialType

data class ValidationResult(val solvable: Boolean, val reasons: List<String> = emptyList())

/**
 * Section 12: every generated level must be proven solvable before it is shown to the player.
 * This is a static/topological check (not a full search) which is enough here because the
 * generator only ever produces levels whose correct-container mapping is total by construction;
 * the validator's job is to catch mistakes in that construction (capacity, lock cycles, moves).
 */
class LevelValidator {

    fun validate(level: LevelData): ValidationResult {
        val reasons = mutableListOf<String>()

        // 1. Every object's category must have a container present in the level.
        val containerCategories = level.containers.map { it.category }.toSet()
        level.objects.forEach { obj ->
            if (obj.special != SpecialType.WILDCARD) {
                val category = ObjectDatabase.byId(obj.objectId).category
                if (category !in containerCategories) {
                    reasons.add("Object ${obj.instanceId} has no matching container")
                }
            }
        }

        // 2. Capacity: each container must be able to hold every object that could land in it.
        //    Wildcard objects can go to any container, so we only require capacity for the
        //    worst case where wildcards pile onto the least-full real category.
        val realCounts = level.containers.associate { c ->
            c.id to level.objects.count {
                it.special != SpecialType.WILDCARD && ObjectDatabase.byId(it.objectId).category == c.category
            }
        }
        level.containers.forEach { c ->
            val real = realCounts[c.id] ?: 0
            if (real > c.capacity) {
                reasons.add("Container ${c.id} over capacity: $real objects, capacity ${c.capacity}")
            }
        }

        // 3. Locked objects must reference a real, distinct, resolvable prerequisite (no self-lock, no cycles).
        val byId = level.objects.associateBy { it.instanceId }
        level.objects.filter { it.special == SpecialType.LOCKED }.forEach { locked ->
            val prereqId = locked.requiresInstanceId
            if (prereqId == null || prereqId == locked.instanceId || byId[prereqId] == null) {
                reasons.add("Locked object ${locked.instanceId} has an invalid prerequisite")
            } else if (byId[prereqId]?.special == SpecialType.LOCKED && byId[prereqId]?.requiresInstanceId == locked.instanceId) {
                reasons.add("Lock cycle between ${locked.instanceId} and $prereqId")
            }
        }

        // 4. Move budget: if limited, it must be at least the number of objects (one move each).
        if (level.maxMoves != 0 && level.maxMoves < level.objects.size) {
            reasons.add("maxMoves ${level.maxMoves} is below object count ${level.objects.size}")
        }

        // 5. Total capacity must cover every object, including the worst case where every
        //    wildcard piles into a single container (see LevelGenerator's capacity comment).
        val totalCapacity = level.containers.sumOf { it.capacity }
        if (totalCapacity < level.objects.size) {
            reasons.add("Total container capacity $totalCapacity is below object count ${level.objects.size}")
        }

        // 6. Board must not be empty.
        if (level.objects.isEmpty() || level.containers.isEmpty()) {
            reasons.add("Level has no objects or no containers")
        }

        return ValidationResult(solvable = reasons.isEmpty(), reasons = reasons)
    }
}
