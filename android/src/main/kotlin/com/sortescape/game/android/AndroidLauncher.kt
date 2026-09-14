package com.sortescape.game.android

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.sortescape.game.SortEscapeGame
import com.sortescape.game.ads.MockAdManager

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useGyroscope = false
        }
        // Ads are disabled in this release: the game ships fully offline with no ad SDK and no
        // network permissions. MockAdManager satisfies the AdManager interface without showing
        // any ad or making any network call, so gameplay proceeds normally between levels.
        val game = SortEscapeGame(
            audioManager = AndroidAudioManager(),
            hapticManager = AndroidHapticManager(this),
            adManager = MockAdManager()
        )
        initialize(game, config)
    }
}
