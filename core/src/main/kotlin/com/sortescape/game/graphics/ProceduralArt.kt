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

    /**
     * A distinct flat-icon glyph per object id (section 1.1's mixed-objects example shows an
     * apple, wrench, teddy, etc. as visually different pieces - a single colored dot per
     * category isn't enough). Badge color still carries the category, so sorting-by-color is
     * still a valid strategy, but each object also reads as itself. New object packs (section
     * 67) fall back to a generic dot glyph until a specific shape is added here - never crash.
     */
    fun objectIcon(objectId: String, badgeColor: Color, size: Int = 128): Texture {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(0f, 0f, 0f, 0f)
        pixmap.fill()

        val r = size / 2f
        pixmap.setColor(badgeColor)
        pixmap.fillCircle(size / 2, size / 2, (r * 0.94f).toInt())

        val glyph = badgeColor.cpy().mul(0.45f, 0.45f, 0.45f, 1f).apply { a = 1f }
        val light = Color(1f, 1f, 1f, 0.85f)

        fun frect(x0: Float, y0: Float, x1: Float, y1: Float, color: Color) {
            pixmap.setColor(color)
            val px0 = (x0 * size).toInt()
            val py0 = ((1f - y1) * size).toInt()
            val w = ((x1 - x0) * size).toInt().coerceAtLeast(1)
            val h = ((y1 - y0) * size).toInt().coerceAtLeast(1)
            pixmap.fillRectangle(px0, py0, w, h)
        }

        fun fcirc(cx: Float, cy: Float, radius: Float, color: Color) {
            pixmap.setColor(color)
            pixmap.fillCircle((cx * size).toInt(), ((1f - cy) * size).toInt(), (radius * size).toInt().coerceAtLeast(1))
        }

        fun ftri(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, color: Color) {
            pixmap.setColor(color)
            pixmap.fillTriangle(
                (x1 * size).toInt(), ((1f - y1) * size).toInt(),
                (x2 * size).toInt(), ((1f - y2) * size).toInt(),
                (x3 * size).toInt(), ((1f - y3) * size).toInt()
            )
        }

        when (objectId) {
            // --- Food ---
            "food_apple" -> {
                fcirc(0.5f, 0.42f, 0.26f, glyph)
                frect(0.48f, 0.68f, 0.53f, 0.80f, glyph)
                ftri(0.53f, 0.76f, 0.66f, 0.82f, 0.58f, 0.66f, light)
            }
            "food_banana" -> {
                fcirc(0.35f, 0.30f, 0.15f, glyph)
                fcirc(0.45f, 0.40f, 0.15f, glyph)
                fcirc(0.55f, 0.50f, 0.15f, glyph)
                fcirc(0.63f, 0.62f, 0.15f, glyph)
                fcirc(0.68f, 0.70f, 0.08f, light)
            }
            "food_pizza" -> {
                ftri(0.5f, 0.78f, 0.22f, 0.24f, 0.78f, 0.24f, glyph)
                fcirc(0.42f, 0.44f, 0.045f, light)
                fcirc(0.58f, 0.44f, 0.045f, light)
                fcirc(0.5f, 0.32f, 0.045f, light)
            }
            "food_burger" -> {
                frect(0.24f, 0.60f, 0.76f, 0.72f, glyph)
                frect(0.24f, 0.44f, 0.76f, 0.56f, light)
                frect(0.24f, 0.28f, 0.76f, 0.40f, glyph)
            }

            // --- Toys ---
            "toy_teddy" -> {
                fcirc(0.36f, 0.72f, 0.12f, glyph)
                fcirc(0.64f, 0.72f, 0.12f, glyph)
                fcirc(0.5f, 0.48f, 0.26f, glyph)
                fcirc(0.42f, 0.50f, 0.035f, light)
                fcirc(0.58f, 0.50f, 0.035f, light)
            }
            "toy_ball" -> {
                fcirc(0.5f, 0.5f, 0.30f, glyph)
                frect(0.48f, 0.20f, 0.52f, 0.80f, badgeColor)
                frect(0.20f, 0.48f, 0.80f, 0.52f, badgeColor)
            }
            "toy_robot" -> {
                frect(0.30f, 0.28f, 0.70f, 0.66f, glyph)
                frect(0.48f, 0.66f, 0.52f, 0.76f, glyph)
                fcirc(0.5f, 0.80f, 0.05f, glyph)
                fcirc(0.40f, 0.48f, 0.045f, light)
                fcirc(0.60f, 0.48f, 0.045f, light)
            }
            "toy_car" -> {
                frect(0.20f, 0.42f, 0.80f, 0.58f, glyph)
                frect(0.34f, 0.54f, 0.66f, 0.66f, glyph)
                fcirc(0.34f, 0.36f, 0.09f, glyph)
                fcirc(0.66f, 0.36f, 0.09f, glyph)
                fcirc(0.34f, 0.36f, 0.03f, light)
                fcirc(0.66f, 0.36f, 0.03f, light)
            }

            // --- Tools ---
            "tool_hammer" -> {
                frect(0.46f, 0.20f, 0.54f, 0.62f, glyph)
                frect(0.28f, 0.62f, 0.72f, 0.78f, glyph)
            }
            "tool_screwdriver" -> {
                frect(0.46f, 0.20f, 0.54f, 0.46f, light)
                frect(0.42f, 0.46f, 0.58f, 0.80f, glyph)
            }
            "tool_wrench" -> {
                frect(0.44f, 0.24f, 0.56f, 0.70f, glyph)
                fcirc(0.42f, 0.74f, 0.10f, glyph)
                fcirc(0.58f, 0.74f, 0.10f, glyph)
                fcirc(0.5f, 0.74f, 0.07f, badgeColor)
            }
            "tool_saw" -> {
                ftri(0.22f, 0.30f, 0.78f, 0.44f, 0.22f, 0.58f, glyph)
                fcirc(0.30f, 0.36f, 0.03f, light)
                fcirc(0.42f, 0.39f, 0.03f, light)
                fcirc(0.54f, 0.42f, 0.03f, light)
            }

            // --- Electronics ---
            "elec_phone" -> {
                frect(0.36f, 0.18f, 0.64f, 0.82f, glyph)
                fcirc(0.5f, 0.26f, 0.035f, light)
            }
            "elec_camera" -> {
                frect(0.22f, 0.32f, 0.78f, 0.66f, glyph)
                frect(0.40f, 0.66f, 0.56f, 0.74f, glyph)
                fcirc(0.5f, 0.49f, 0.14f, light)
                fcirc(0.5f, 0.49f, 0.08f, glyph)
            }
            "elec_laptop" -> {
                frect(0.26f, 0.46f, 0.74f, 0.78f, glyph)
                frect(0.18f, 0.30f, 0.82f, 0.44f, glyph)
                frect(0.22f, 0.33f, 0.78f, 0.41f, light)
            }
            "elec_headphones" -> {
                fcirc(0.28f, 0.44f, 0.10f, glyph)
                fcirc(0.72f, 0.44f, 0.10f, glyph)
                frect(0.28f, 0.55f, 0.34f, 0.68f, glyph)
                frect(0.66f, 0.55f, 0.72f, 0.68f, glyph)
                frect(0.28f, 0.64f, 0.72f, 0.70f, glyph)
            }

            // --- Bathroom ---
            "bath_soap" -> {
                frect(0.26f, 0.36f, 0.74f, 0.64f, glyph)
                fcirc(0.5f, 0.5f, 0.05f, light)
            }
            "bath_toothbrush" -> {
                frect(0.46f, 0.20f, 0.54f, 0.60f, glyph)
                frect(0.38f, 0.60f, 0.62f, 0.76f, light)
            }
            "bath_shampoo" -> {
                frect(0.34f, 0.30f, 0.66f, 0.78f, glyph)
                frect(0.42f, 0.20f, 0.58f, 0.30f, glyph)
            }
            "bath_towel" -> {
                frect(0.22f, 0.22f, 0.78f, 0.78f, glyph)
                frect(0.22f, 0.34f, 0.78f, 0.38f, light)
                frect(0.22f, 0.50f, 0.78f, 0.54f, light)
                frect(0.22f, 0.66f, 0.78f, 0.70f, light)
            }

            // --- Wildcard ---
            "wild_gift" -> {
                frect(0.24f, 0.24f, 0.76f, 0.68f, glyph)
                frect(0.24f, 0.42f, 0.76f, 0.50f, light)
                frect(0.46f, 0.24f, 0.54f, 0.68f, light)
                ftri(0.5f, 0.68f, 0.40f, 0.82f, 0.50f, 0.76f, glyph)
                ftri(0.5f, 0.68f, 0.60f, 0.82f, 0.50f, 0.76f, glyph)
            }

            else -> fcirc(0.5f, 0.5f, 0.22f, glyph)
        }

        val texture = Texture(pixmap)
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        pixmap.dispose()
        return texture
    }
}
