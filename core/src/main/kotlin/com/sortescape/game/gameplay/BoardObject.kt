package com.sortescape.game.gameplay

import com.badlogic.gdx.physics.box2d.Body
import com.sortescape.game.data.LevelObjectInstance
import com.sortescape.game.data.ObjectData

enum class ObjectVisualState { IN_PILE, SELECTED, FLYING, SORTED, REVEALED_MYSTERY }

/** Runtime instance of one object on the board: definition + physics body + current state. */
class BoardObject(
    val instance: LevelObjectInstance,
    val definition: ObjectData,
    val body: Body,
    val radius: Float
) {
    var state: ObjectVisualState = ObjectVisualState.IN_PILE
    var flyTargetX: Float = 0f
    var flyTargetY: Float = 0f
    var revealed: Boolean = false
    var selectedAtSec: Float = 0f

    fun isLocked(sortedInstanceIds: Set<String>): Boolean {
        val req = instance.requiresInstanceId ?: return false
        return req !in sortedInstanceIds
    }
}
