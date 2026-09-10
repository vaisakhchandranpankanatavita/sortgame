package com.sortescape.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.sortescape.game.SortEscapeGame
import com.sortescape.game.graphics.ProceduralArt
import com.sortescape.game.meta.Themes

/** Section 23/24: cosmetic-only theme gallery, purchased with coins, never affects gameplay. */
class ThemesScreen(private val game: SortEscapeGame) : ScreenAdapter() {

    private val stage = Stage(ScreenViewport(), game.batch)
    private val cardTextures = mutableListOf<com.badlogic.gdx.graphics.Texture>()

    override fun show() {
        Gdx.input.inputProcessor = InputMultiplexer(stage, object : InputAdapter() {
            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    game.setScreen(MainMenuScreen(game))
                    return true
                }
                return false
            }
        })
        rebuild()
    }

    private fun rebuild() {
        stage.clear()
        cardTextures.forEach { it.dispose() }
        cardTextures.clear()
        val root = Table()
        root.setFillParent(true)
        stage.addActor(root)

        root.add(Label("THEMES", com.sortescape.game.ui.UiFactory.labelStyle(Color.WHITE, big = true))).padTop(30f).padBottom(20f).row()

        val grid = Table()
        Themes.ALL.forEach { theme ->
            val unlocked = game.themeManager.isUnlocked(theme.id)
            val equipped = game.themeManager.equipped().id == theme.id
            val card = ProceduralArt.roundedGradientPanel(
                260, 160, 24,
                com.badlogic.gdx.graphics.Color(theme.topColor[0], theme.topColor[1], theme.topColor[2], 1f),
                com.badlogic.gdx.graphics.Color(theme.bottomColor[0], theme.bottomColor[1], theme.bottomColor[2], 1f)
            )
            cardTextures.add(card)

            val cell = Table()
            cell.add(com.badlogic.gdx.scenes.scene2d.ui.Image(card)).size(260f, 160f).row()
            val caption = when {
                equipped -> "${theme.displayName} (equipped)"
                unlocked -> theme.displayName
                else -> "${theme.displayName} - ${theme.unlockCost} coins"
            }
            cell.add(Label(caption, com.sortescape.game.ui.UiFactory.labelStyle())).padTop(6f)

            cell.touchable = com.badlogic.gdx.scenes.scene2d.Touchable.enabled
            cell.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    if (unlocked) {
                        game.themeManager.equip(theme.id)
                    } else if (game.themeManager.tryUnlock(theme.id)) {
                        game.audioManager.playUnlock()
                        game.themeManager.equip(theme.id)
                    } else {
                        game.audioManager.playWrongSort()
                    }
                    rebuild()
                }
            })

            grid.add(cell).pad(10f)
            if (Themes.ALL.indexOf(theme) % 2 == 1) grid.row()
        }
        root.add(grid).padBottom(20f).row()

        val back = TextButton("BACK", com.sortescape.game.ui.UiFactory.buttonStyle(Color.DARK_GRAY))
        back.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) = game.setScreen(MainMenuScreen(game))
        })
        root.add(back).width(200f).height(70f)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.14f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) = stage.viewport.update(width, height, true)
    override fun dispose() {
        cardTextures.forEach { it.dispose() }
        stage.dispose()
    }
}
