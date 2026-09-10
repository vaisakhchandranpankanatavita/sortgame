package com.sortescape.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.sortescape.game.SortEscapeGame
import com.sortescape.game.ui.UiFactory

class LevelSelectScreen(private val game: SortEscapeGame) : ScreenAdapter() {

    private val stage = Stage(ScreenViewport(), game.batch)

    override fun show() {
        val multiplexer = com.badlogic.gdx.InputMultiplexer(stage, object : InputAdapter() {
            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    game.setScreen(MainMenuScreen(game))
                    return true
                }
                return false
            }
        })
        Gdx.input.inputProcessor = multiplexer

        val root = Table()
        root.setFillParent(true)
        stage.addActor(root)

        val title = Label("SELECT LEVEL", UiFactory.labelStyle(Color.WHITE, big = true))
        root.add(title).padTop(30f).padBottom(20f).row()

        val backButton = TextButton("Back", UiFactory.buttonStyle(Color.DARK_GRAY))
        backButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) = game.setScreen(MainMenuScreen(game))
        })
        root.add(backButton).width(160f).height(60f).padBottom(20f).row()

        val grid = Table()
        val highestUnlocked = game.saveManager.data.currentLevel
        val total = game.levelRepository.totalLevels()
        for (levelId in 1..total) {
            val unlocked = levelId <= highestUnlocked
            val style = if (unlocked) UiFactory.buttonStyle(Color(0.25f, 0.6f, 0.35f, 1f)) else UiFactory.buttonStyle(Color(0.3f, 0.3f, 0.33f, 1f))
            val button = TextButton(levelId.toString(), style)
            button.isDisabled = !unlocked
            if (unlocked) {
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        game.audioManager.playButtonClick()
                        game.setScreen(GameplayScreen(game, levelId))
                    }
                })
            }
            grid.add(button).width(100f).height(100f).pad(6f)
            if (levelId % 5 == 0) grid.row()
        }

        val scroll = ScrollPane(grid)
        scroll.setScrollingDisabled(true, false)
        root.add(scroll).expand().fill().row()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.14f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
    }
}
