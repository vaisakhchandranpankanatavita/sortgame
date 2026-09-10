package com.sortescape.game.data

/** A container slot on the board (section 41). */
data class ContainerData(
    val id: String,
    val category: Category,
    val capacity: Int,
    var locked: Boolean = false
)

/** A concrete object placed on the board for one level (section 42, "objects" list). */
data class LevelObjectInstance(
    val instanceId: String,
    val objectId: String,
    val special: SpecialType = SpecialType.NONE,
    /** For SpecialType.LOCKED: the instanceId of the object that must be sorted first. */
    val requiresInstanceId: String? = null
)
