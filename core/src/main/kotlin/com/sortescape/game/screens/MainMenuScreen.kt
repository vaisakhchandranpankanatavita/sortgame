package com.sortescape.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.sortescape.game.SortEscapeGame
import com.sortescape.game.meta.Achievements
import com.sortescape.game.meta.DailyReward
import com.sortescape.game.ui.UiFactory

/** Section 19: main menu with Play / Levels / Daily Challenge / Themes / Achievements / Settings. */
class MainMenuScreen(private val game: SortEscapeGame) : ScreenAdapter() {

    private val stage = Stage(ScreenViewport(), game.batch)
    private val coinsLabel = Label("", UiFactory.labelStyle(Color.GOLD))
    private val starsLabel = Label("", UiFactory.labelStyle(Color(0.95f, 0.85f, 0.35f, 1f)))

    override fun show() {
        Gdx.input.inputProcessor = stage
        rebuild()
    }

    private fun rebuild() {
        stage.clear()
        val save = game.saveManager.data

        val root = Table()
        root.setFillParent(true)
        stage.addActor(root)

        root.add(Label("SORT ESCAPE", UiFactory.labelStyle(Color.WHITE, big = true))).padTop(60f).row()
        root.add(Label("Level ${save.currentLevel}", UiFactory.labelStyle())).padTop(8f).row()

        val statsRow = Table()
        coinsLabel.setText("Coins: ${save.coins}")
        starsLabel.setText("Stars: ${save.totalStars}")
        statsRow.add(coinsLabel).padRight(24f)
        statsRow.add(starsLabel)
        root.add(statsRow).padTop(6f).padBottom(30f).row()

        val playButton = TextButton("PLAY", UiFactory.buttonStyle(Color(0.25f, 0.70f, 0.45f, 1f)))
        playButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.audioManager.playButtonClick()
                game.setScreen(GameplayScreen(game, save.currentLevel))
            }
        })
        root.add(playButton).width(320f).height(100f).padBottom(16f).row()

        val levelsButton = TextButton("LEVELS", UiFactory.buttonStyle())
        levelsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.audioManager.playButtonClick()
                game.setScreen(LevelSelectScreen(game))
            }
        })
        root.add(levelsButton).width(280f).height(80f).padBottom(12f).row()

        val dailyButton = TextButton("DAILY CHALLENGE", UiFactory.buttonStyle(Color(0.85f, 0.55f, 0.15f, 1f)))
        dailyButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.audioManager.playButtonClick()
                showDailyRewardDialog()
            }
        })
        root.add(dailyButton).width(280f).height(80f).padBottom(12f).row()

        val themesButton = TextButton("THEMES", UiFactory.buttonStyle(Color(0.55f, 0.40f, 0.85f, 1f)))
        themesButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.audioManager.playButtonClick()
                game.setScreen(ThemesScreen(game))
            }
        })
        root.add(themesButton).width(280f).height(80f).padBottom(12f).row()

        val bottomRow = Table()
        val achievementsButton = TextButton("Achievements", UiFactory.buttonStyle(Color.DARK_GRAY))
        achievementsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) = showAchievementsDialog()
        })
        val settingsButton = TextButton("Settings", UiFactory.buttonStyle(Color.DARK_GRAY))
        settingsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) = showSettingsDialog()
        })
        bottomRow.add(achievementsButton).width(200f).height(60f).padRight(12f)
        bottomRow.add(settingsButton).width(160f).height(60f)
        root.add(bottomRow).padTop(20f).row()
    }

    private fun showDailyRewardDialog() {
        val window = Window("Daily Challenge", UiFactory.windowStyle())
        window.isMovable = false
        val save = game.saveManager.data
        window.add(Label("Streak: ${save.dailyStreak} day(s)", UiFactory.labelStyle())).padBottom(10f).row()

        if (game.dailyRewardManager.canClaimToday()) {
            val claimButton = TextButton("CLAIM REWARD", UiFactory.buttonStyle(Color(0.85f, 0.55f, 0.15f, 1f)))
            claimButton.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    val reward = game.dailyRewardManager.claim()
                    window.clear()
                    window.add(Label(describe(reward), UiFactory.labelStyle(Color.GOLD))).padBottom(10f).row()
                    val close = TextButton("CLOSE", UiFactory.buttonStyle(Color.DARK_GRAY))
                    close.addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            window.remove()
                            rebuild()
                        }
                    })
                    window.add(close).width(160f).height(60f)
                    window.pack()
                    window.setPosition((stage.width - window.width) / 2f, (stage.height - window.height) / 2f)
                }
            })
            window.add(claimButton).width(280f).height(80f).row()
        } else {
            window.add(Label("Come back tomorrow!", UiFactory.labelStyle())).padBottom(10f).row()
            val close = TextButton("CLOSE", UiFactory.buttonStyle(Color.DARK_GRAY))
            close.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) { window.remove() }
            })
            window.add(close).width(160f).height(60f)
        }

        window.pack()
        window.setPosition((stage.width - window.width) / 2f, (stage.height - window.height) / 2f)
        stage.addActor(window)
    }

    private fun describe(reward: com.sortescape.game.meta.DailyReward?): String = when (reward) {
        is DailyReward.Coins -> "+${reward.amount} coins!"
        is DailyReward.Gems -> "+${reward.amount} gems!"
        is DailyReward.Theme -> "Unlocked theme: ${reward.themeId}!"
        null -> "Already claimed today."
    }

    private fun showAchievementsDialog() {
        val window = Window("Achievements", UiFactory.windowStyle())
        window.isMovable = false
        val unlocked = game.achievementManager.unlocked()
        Achievements.ALL.forEach { a ->
            val color = if (a.id in unlocked) Color.GOLD else Color.GRAY
            val prefix = if (a.id in unlocked) "✓ " else "○ "
            window.add(Label("$prefix${a.name} - ${a.description}", UiFactory.labelStyle(color))).left().padBottom(6f).row()
        }
        val close = TextButton("CLOSE", UiFactory.buttonStyle(Color.DARK_GRAY))
        close.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) { window.remove() }
        })
        window.add(close).width(160f).height(60f).padTop(12f)
        window.pack()
        window.setPosition((stage.width - window.width) / 2f, (stage.height - window.height) / 2f)
        stage.addActor(window)
    }

    private fun showSettingsDialog() {
        val window = Window("Settings", UiFactory.windowStyle())
        window.isMovable = false
        val save = game.saveManager.data

        val soundBox = CheckBox(" Sound", UiFactory.checkBoxStyle())
        soundBox.isChecked = save.soundEnabled
        soundBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                game.saveManager.mutate { it.soundEnabled = soundBox.isChecked }
                game.audioManager.setSoundEnabled(soundBox.isChecked)
            }
        })

        val musicBox = CheckBox(" Music", UiFactory.checkBoxStyle())
        musicBox.isChecked = save.musicEnabled
        musicBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                game.saveManager.mutate { it.musicEnabled = musicBox.isChecked }
                game.audioManager.setMusicEnabled(musicBox.isChecked)
            }
        })

        val hapticsBox = CheckBox(" Haptics", UiFactory.checkBoxStyle())
        hapticsBox.isChecked = save.hapticsEnabled
        hapticsBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: com.badlogic.gdx.scenes.scene2d.Actor?) {
                game.saveManager.mutate { it.hapticsEnabled = hapticsBox.isChecked }
                game.hapticManager.setEnabled(hapticsBox.isChecked)
            }
        })

        window.add(soundBox).left().padBottom(8f).row()
        window.add(musicBox).left().padBottom(8f).row()
        window.add(hapticsBox).left().padBottom(16f).row()

        val close = TextButton("CLOSE", UiFactory.buttonStyle(Color.DARK_GRAY))
        close.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) { window.remove() }
        })
        window.add(close).width(160f).height(60f)

        window.pack()
        window.setPosition((stage.width - window.width) / 2f, (stage.height - window.height) / 2f)
        stage.addActor(window)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.10f, 0.11f, 0.16f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) = stage.viewport.update(width, height, true)
    override fun dispose() = stage.dispose()
}
