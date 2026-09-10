package com.sortescape.game.audio

/** Section 27: light tap, medium correct-sort, stronger combo, success pattern on completion. */
interface HapticManager {
    fun tap()
    fun correctSort()
    fun combo()
    fun levelComplete()
    fun setEnabled(enabled: Boolean)
}
