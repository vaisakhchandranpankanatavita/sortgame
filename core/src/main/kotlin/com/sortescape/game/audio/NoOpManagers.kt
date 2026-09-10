package com.sortescape.game.audio

/** Default fallback so [SortEscapeGame] can run (e.g. in tests) before a platform hooks up real sound. */
class NoOpAudioManager : AudioManager {
    override fun playTap() {}
    override fun playCorrectSort(comboLevel: Int) {}
    override fun playWrongSort() {}
    override fun playLevelComplete() {}
    override fun playCoinReward() {}
    override fun playUnlock() {}
    override fun playButtonClick() {}
    override fun setSoundEnabled(enabled: Boolean) {}
    override fun setMusicEnabled(enabled: Boolean) {}
}

/** Default fallback so [SortEscapeGame] can run (e.g. in tests) before a platform hooks up real haptics. */
class NoOpHapticManager : HapticManager {
    override fun tap() {}
    override fun correctSort() {}
    override fun combo() {}
    override fun levelComplete() {}
    override fun setEnabled(enabled: Boolean) {}
}
