package com.sortescape.game.gameplay

import com.sortescape.game.data.ContainerData

/** Runtime container placed along the bottom of the board. */
class ContainerSlot(
    val data: ContainerData,
    val centerX: Float,
    val centerY: Float,
    val halfWidth: Float,
    val halfHeight: Float
) {
    var sortedCount: Int = 0

    fun contains(x: Float, y: Float): Boolean =
        x in (centerX - halfWidth)..(centerX + halfWidth) && y in (centerY - halfHeight)..(centerY + halfHeight)

    fun isFull(): Boolean = sortedCount >= data.capacity
}
