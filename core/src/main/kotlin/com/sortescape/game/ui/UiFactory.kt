package com.sortescape.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

/**
 * Builds Scene2D building blocks from a single 1x1 white pixel, tinted per use.
 * No image/skin-json assets required, which keeps the android module asset-free for now.
 */
object UiFactory {

    lateinit var font: BitmapFont
        private set
    lateinit var titleFont: BitmapFont
        private set
    private lateinit var whiteTexture: Texture

    // TextureRegionDrawable.tint() returns the base Drawable interface (it may hand back a
    // different concrete Drawable), so this can't be typed as TextureRegionDrawable.
    private fun white(color: Color): Drawable =
        TextureRegionDrawable(TextureRegion(whiteTexture)).tint(color)

    fun init() {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        whiteTexture = Texture(pixmap)
        pixmap.dispose()

        font = BitmapFont()
        font.data.setScale(1.6f)
        titleFont = BitmapFont()
        titleFont.data.setScale(2.6f)
    }

    fun buttonStyle(base: Color = Color(0.25f, 0.45f, 0.85f, 1f)): TextButton.TextButtonStyle =
        TextButton.TextButtonStyle().apply {
            up = white(base)
            down = white(base.cpy().mul(0.7f, 0.7f, 0.7f, 1f))
            over = white(base.cpy().mul(1.15f, 1.15f, 1.15f, 1f))
            disabled = white(Color(0.4f, 0.4f, 0.4f, 1f))
            font = this@UiFactory.font
            fontColor = Color.WHITE
            disabledFontColor = Color.LIGHT_GRAY
        }

    fun labelStyle(color: Color = Color.WHITE, big: Boolean = false): Label.LabelStyle =
        Label.LabelStyle(if (big) titleFont else font, color)

    fun windowStyle(): Window.WindowStyle =
        Window.WindowStyle(titleFont, Color.WHITE, white(Color(0.08f, 0.08f, 0.12f, 0.94f)))

    fun panelDrawable(color: Color): Drawable = white(color)

    fun checkBoxStyle(): com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle =
        com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle().apply {
            checkboxOff = white(Color(0.35f, 0.35f, 0.40f, 1f)).apply { setMinWidth(48f); setMinHeight(48f) }
            checkboxOn = white(Color(0.30f, 0.75f, 0.45f, 1f)).apply { setMinWidth(48f); setMinHeight(48f) }
            font = this@UiFactory.font
            fontColor = Color.WHITE
        }

    fun dispose() {
        whiteTexture.dispose()
        font.dispose()
        titleFont.dispose()
    }
}
