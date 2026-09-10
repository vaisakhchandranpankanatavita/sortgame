package com.sortescape.game.audio

/**
 * Section 26: satisfying, not annoying, with pitch variation on repeated correct sorts so
 * combos feel progressively rewarding. Platform module supplies the actual synthesis.
 */
interface AudioManager {
    fun playTap()
    fun playCorrectSort(comboLevel: Int)
    fun playWrongSort()
    fun playLevelComplete()
    fun playCoinReward()
    fun playUnlock()
    fun playButtonClick()
    fun setSoundEnabled(enabled: Boolean)
    fun setMusicEnabled(enabled: Boolean)
}
