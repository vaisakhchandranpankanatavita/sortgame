package com.sortescape.game

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.sortescape.game.ads.AdManager
import com.sortescape.game.audio.AudioManager
import com.sortescape.game.audio.HapticManager
import com.sortescape.game.level.LevelRepository
import com.sortescape.game.meta.AchievementManager
import com.sortescape.game.meta.CurrencyManager
import com.sortescape.game.meta.DailyRewardManager
import com.sortescape.game.meta.SaveManager
import com.sortescape.game.meta.ThemeManager
import com.sortescape.game.screens.MainMenuScreen
import com.sortescape.game.ui.UiFactory

/**
 * Top-level libGDX application. Owns every long-lived service so screens can be swapped
 * freely without losing save state, audio, or ad cooldowns.
 */
class SortEscapeGame(
    val audioManager: AudioManager,
    val hapticManager: HapticManager,
    val adManager: AdManager
) : Game() {

    lateinit var batch: SpriteBatch
        private set
    lateinit var saveManager: SaveManager
        private set
    lateinit var currencyManager: CurrencyManager
        private set
    lateinit var themeManager: ThemeManager
        private set
    lateinit var achievementManager: AchievementManager
        private set
    lateinit var dailyRewardManager: DailyRewardManager
        private set
    lateinit var levelRepository: LevelRepository
        private set

    override fun create() {
        com.badlogic.gdx.physics.box2d.Box2D.init()
        batch = SpriteBatch()
        UiFactory.init()

        saveManager = SaveManager()
        currencyManager = CurrencyManager(saveManager)
        themeManager = ThemeManager(saveManager, currencyManager)
        achievementManager = AchievementManager(saveManager)
        dailyRewardManager = DailyRewardManager(saveManager, currencyManager)
        levelRepository = LevelRepository()

        audioManager.setSoundEnabled(saveManager.data.soundEnabled)
        audioManager.setMusicEnabled(saveManager.data.musicEnabled)
        hapticManager.setEnabled(saveManager.data.hapticsEnabled)

        setScreen(MainMenuScreen(this))
    }

    override fun dispose() {
        screen?.dispose()
        UiFactory.dispose()
        batch.dispose()
    }
}
