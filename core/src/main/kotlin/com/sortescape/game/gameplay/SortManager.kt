package com.sortescape.game.gameplay

import com.badlogic.gdx.math.Vector2
import com.sortescape.game.data.*
import com.sortescape.game.physics.PhysicsWorld
import kotlin.random.Random

interface SortListener {
    fun onObjectSelected(obj: BoardObject) {}
    fun onSelectionCleared() {}
    fun onCorrectSort(obj: BoardObject, container: ContainerSlot, reactionTimeSec: Float) {}
    fun onWrongSort(obj: BoardObject, container: ContainerSlot) {}
    fun onLockedTapBlocked(obj: BoardObject) {}
    fun onMysteryRevealed(obj: BoardObject) {}
    fun onLevelComplete() {}
    fun onLevelFailed() {}
    fun onMoveConsumed(movesRemaining: Int) {}
}

/**
 * The core game loop from section 4/5: observe -> choose object -> sort -> feedback.
 * Owns the physics bodies for the current level and drives them each frame.
 */
class SortManager(
    private val level: LevelData,
    private val physics: PhysicsWorld,
    private val listener: SortListener,
    seed: Long = level.levelId.toLong()
) {
    private val rng = Random(seed)
    val boardObjects: MutableList<BoardObject> = mutableListOf()
    val containers: MutableList<ContainerSlot> = mutableListOf()

    private val sortedInstanceIds = mutableSetOf<String>()
    var selected: BoardObject? = null
        private set

    private var elapsedSec: Float = 0f
    private var movesUsed: Int = 0
    private var failed = false
    private var completed = false

    init {
        spawnContainers()
        spawnObjects()
    }

    private fun spawnContainers() {
        val n = level.containers.size
        val margin = 0.9f
        val usable = physics.boardWidth - margin * 2f
        val slotW = usable / n
        level.containers.forEachIndexed { i, data ->
            val cx = margin + slotW * i + slotW / 2f
            containers.add(ContainerSlot(data, cx, 1.6f, slotW * 0.42f, 1.3f))
        }
    }

    private fun spawnObjects() {
        level.objects.forEach { instance ->
            val definition = ObjectDatabase.byId(instance.objectId)
            val radius = 0.42f
            val x = physics.pileLeft + radius + rng.nextFloat() * (physics.pileRight - physics.pileLeft - radius * 2f)
            val y = physics.pileBottom + physics.boardHeight * 0.15f + rng.nextFloat() * (physics.pileTop - physics.pileBottom - radius * 2f)
            val body = physics.spawnObjectBody(radius, x, y)
            boardObjects.add(BoardObject(instance, definition, body, radius))
        }
    }

    fun update(delta: Float) {
        if (failed || completed) return
        elapsedSec += delta
        physics.step(delta)

        boardObjects.filter { it.state == ObjectVisualState.FLYING }.forEach { obj ->
            val target = Vector2(obj.flyTargetX, obj.flyTargetY)
            val dist = obj.body.position.dst(target)
            if (dist < 0.18f) {
                finishSort(obj)
            } else {
                physics.applyGuidedForce(obj.body, target, strength = 26f)
            }
        }
    }

    /** Section 5.1: first tap selects an object (and reveals mystery objects, section 8.3). */
    fun selectAt(worldX: Float, worldY: Float) {
        val candidate = boardObjects
            .filter { it.state == ObjectVisualState.IN_PILE }
            .minByOrNull { it.body.position.dst(worldX, worldY) }
            ?: return

        val dist = candidate.body.position.dst(worldX, worldY)
        if (dist > candidate.radius * 1.6f) {
            clearSelection()
            return
        }

        if (candidate.isLocked(sortedInstanceIds)) {
            listener.onLockedTapBlocked(candidate)
            return
        }

        selected?.state = ObjectVisualState.IN_PILE
        candidate.state = ObjectVisualState.SELECTED
        candidate.selectedAtSec = elapsedSec
        selected = candidate

        if (candidate.instance.special == SpecialType.MYSTERY && !candidate.revealed) {
            candidate.revealed = true
            listener.onMysteryRevealed(candidate)
        }

        listener.onObjectSelected(candidate)
    }

    fun clearSelection() {
        selected?.let { if (it.state == ObjectVisualState.SELECTED) it.state = ObjectVisualState.IN_PILE }
        selected = null
        listener.onSelectionCleared()
    }

    /** Section 5.1: second tap on a container attempts the sort. */
    fun attemptSortAt(worldX: Float, worldY: Float) {
        val obj = selected ?: return
        val container = containers.firstOrNull { it.contains(worldX, worldY) } ?: return
        attemptSort(obj, container)
    }

    private fun attemptSort(obj: BoardObject, container: ContainerSlot) {
        val matches = obj.instance.special == SpecialType.WILDCARD || obj.definition.category == container.data.category
        val roomAvailable = !container.isFull()
        val reactionTime = elapsedSec - obj.selectedAtSec

        if (matches && roomAvailable) {
            obj.state = ObjectVisualState.FLYING
            val slotIndex = container.sortedCount
            val col = slotIndex % 3
            val row = slotIndex / 3
            obj.flyTargetX = container.centerX + (col - 1) * 0.5f
            obj.flyTargetY = container.centerY + row * 0.45f
            container.sortedCount++
            selected = null
            listener.onCorrectSort(obj, container, reactionTime)
        } else {
            physics.applyShake(obj.body)
            obj.state = ObjectVisualState.IN_PILE
            selected = null
            listener.onWrongSort(obj, container)
        }

        consumeMove()
    }

    private fun finishSort(obj: BoardObject) {
        obj.state = ObjectVisualState.SORTED
        sortedInstanceIds.add(obj.instance.instanceId)
        physics.destroyBody(obj.body)
        checkLevelComplete()
    }

    private fun consumeMove() {
        movesUsed++
        val remaining = if (level.maxMoves == 0) Int.MAX_VALUE else level.maxMoves - movesUsed
        listener.onMoveConsumed(remaining)
        if (level.maxMoves != 0 && remaining <= 0) {
            val stillHasWork = boardObjects.any { it.state != ObjectVisualState.SORTED }
            if (stillHasWork) {
                failed = true
                listener.onLevelFailed()
            }
        }
    }

    private fun checkLevelComplete() {
        val allSorted = boardObjects.all { it.state == ObjectVisualState.SORTED }
        if (allSorted && !completed) {
            completed = true
            listener.onLevelComplete()
        }
    }

    /** Section 15: highlights a valid object/container pair for the hint system. */
    fun findHint(): Pair<BoardObject, ContainerSlot>? {
        val obj = boardObjects.firstOrNull {
            it.state == ObjectVisualState.IN_PILE && !it.isLocked(sortedInstanceIds)
        } ?: return null
        val container = containers.firstOrNull {
            (obj.instance.special == SpecialType.WILDCARD || it.data.category == obj.definition.category) && !it.isFull()
        } ?: return null
        return obj to container
    }

    fun grantBonusMoves(amount: Int) {
        // Bonus moves are applied by adjusting the effective budget the caller tracks;
        // SortManager itself only fails once movesUsed exceeds level.maxMoves, so callers
        // that grant bonus moves should increase level.maxMoves via a fresh level copy,
        // or simpler: this resets the failed flag and rolls back a few consumed moves.
        movesUsed = maxOf(0, movesUsed - amount)
        failed = false
    }

    fun remainingMoves(): Int = if (level.maxMoves == 0) Int.MAX_VALUE else level.maxMoves - movesUsed
    fun isUnlimitedMoves(): Boolean = level.maxMoves == 0
    fun objectsRemaining(): Int = boardObjects.count { it.state != ObjectVisualState.SORTED }
    fun isFailed(): Boolean = failed
    fun isCompleted(): Boolean = completed

    fun dispose() {
        boardObjects.forEach { physics.destroyBody(it.body) }
    }
}
