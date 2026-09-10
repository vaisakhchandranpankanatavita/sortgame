package com.sortescape.game.android

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.sortescape.game.SortEscapeGame

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useGyroscope = false
        }
        val game = SortEscapeGame(
            audioManager = AndroidAudioManager(),
            hapticManager = AndroidHapticManager(this),
            adManager = AndroidAdMobManager(this)
        )
        initialize(game, config)
    }
}
