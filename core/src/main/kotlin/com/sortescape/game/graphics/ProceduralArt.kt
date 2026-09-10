package com.sortescape.game.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture

/**
 * Section 22 "Premium Minimal Casual" style, drawn entirely at runtime: rounded corners,
 * soft shadows, subtle vertical gradients. No external art assets are needed, which also
 * means the content pipeline in section 67 (new object packs) never blocks on an artist -
 * a new ObjectData just needs an id/name/category and one small icon-drawing function below.
 */
object ProceduralArt {

    private fun inRoundedRect(x: Int, y: Int, w: Int, h: Int, r: Int): Boolean {
        val cx = when {
            x < r -> r
            x > w - r -> w - r
            else -> x
        }
        val cy = when {
            y < r -> r
            y > h - r -> h - r
            else -> y
        }
        val dx = (x - cx).toFloat()
        val dy = (y - cy).toFloat()
        return dx * dx + dy * dy <= r.toFloat() * r
    }

    /** A rounded, vertically gradiented panel with a soft drop shadow baked in - used for object
     * chips, containers, buttons and cards throughout the UI. */
    fun roundedGradientPanel(
        w: Int, h: Int, radius: Int,
        top: Color, bottom: Color,
        withShadow: Boolean = true
    ): Texture {
        val padding = if (withShadow) 10 else 0
        val pw = w + padding * 2
        val ph = h + padding * 2
        val pixmap = Pixmap(pw, ph, Pixmap.Format.RGBA8888)
        pixmap.setColor(0f, 0f, 0f, 0f)
        pixmap.fill()

        if (withShadow) {
            val shadowLayers = 6
            for (i in shadowLayers downTo 1) {
                val grow = i * 2
                val alpha = 0.05f * (shadowLayers - i + 1)
                pixmap.setColor(0.05f, 0.08f, 0.15f, alpha)
                for (y in -grow..h + grow) {
                    for (x in -grow..w + grow) {
                        if (inRoundedRect(x + grow, y + grow, w + grow * 2, h + grow * 2, radius + grow / 2)) {
                            pixmap.drawPixel(padding + x + 3, padding + y + 4)
                        }
                    }
                }
            }
        }

        for (y in 0 until h) {
            val t = y / h.toFloat()
            val r = top.r + (bottom.r - top.r) * t
            val g = top.g + (bottom.g - top.g) * t
            val b = top.b + (bottom.b - top.b) * t
            pixmap.setColor(r, g, b, 1f)
            for (x in 0 until w) {
                if (inRoundedRect(x, y, w, h, radius)) {
                    pixmap.drawPixel(padding + x, padding + y)
                }
            }
        }

        // Subtle top highlight for a soft glossy premium feel.
        pixmap.setColor(1f, 1f, 1f, 0.12f)
        for (y in 0 until (h * 0.28f).toInt()) {
            for (x in 0 until w) {
                if (inRoundedRect(x, y, w, h, radius)) pixmap.drawPixel(padding + x, padding + y)
            }
        }

        val texture = Texture(pixmap)
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        pixmap.dispose()
        return texture
    }

    fun softCircle(diameter: Int, color: Color): Texture {
        val pixmap = Pixmap(diameter, diameter, Pixmap.Format.RGBA8888)
        pixmap.setColor(0f, 0f, 0f, 0f)
        pixmap.fill()
        val r = diameter / 2f
        pixmap.setColor(color)
        pixmap.fillCircle(diameter / 2, diameter / 2, (r * 0.94f).toInt())
        val texture = Texture(pixmap)
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        pixmap.dispose()
        return texture
    }

    /** A small radial-falloff dot used for confetti and sparkle particles. */
    fun particleDot(size: Int, color: Color): Texture {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        val cx = size / 2f
        val cy = size / 2f
        val maxR = size / 2f
        for (y in 0 until size) {
            for (x in 0 until size) {
                val d = Math.hypot((x - cx).toDouble(), (y - cy).toDouble()).toFloat()
                val a = (1f - (d / maxR)).coerceIn(0f, 1f)
                pixmap.setColor(color.r, color.g, color.b, a * a)
                pixmap.drawPixel(x, y)
            }
        }
        val texture = Texture(pixmap)
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        pixmap.dispose()
        return texture
    }

    /** A single flat-white pixel, tinted per-draw - used for shape fills (e.g. ShapeRenderer alternatives). */
    fun whitePixel(): Texture {
        val pixmap = Pixmap(2, 2, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }
}
