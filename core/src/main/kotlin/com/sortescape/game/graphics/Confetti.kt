package com.sortescape.game.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlin.random.Random

/**
 * Section 21/25's "Objects disappear -> Confetti -> Stars -> Reward" celebration. A small,
 * self-contained particle burst so the win screen doesn't rely on a heavier ParticleEffect
 * asset pipeline - a handful of tinted squares falling and tumbling is enough to read as
 * "confetti" without needing any bundled particle-effect files.
 */
class Confetti {

    private data class Piece(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        var rotation: Float, var rotSpeed: Float,
        val color: Color, val size: Float
    )

    private val pieces = mutableListOf<Piece>()
    private val palette = listOf(
        CategoryColors.top(com.sortescape.game.data.Category.FOOD),
        CategoryColors.top(com.sortescape.game.data.Category.TOYS),
        CategoryColors.top(com.sortescape.game.data.Category.ELECTRONICS),
        CategoryColors.top(com.sortescape.game.data.Category.BATHROOM),
        CategoryColors.top(com.sortescape.game.data.Category.WILDCARD)
    )

    /** Spawns a burst across the top of a [width]x[height] screen area. */
    fun burst(width: Float, height: Float, count: Int = 45) {
        pieces.clear()
        repeat(count) {
            pieces.add(
                Piece(
                    x = Random.nextFloat() * width,
                    y = height + Random.nextFloat() * height * 0.4f,
                    vx = (Random.nextFloat() - 0.5f) * 120f,
                    vy = -(180f + Random.nextFloat() * 220f),
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 540f,
                    color = palette[Random.nextInt(palette.size)],
                    size = 10f + Random.nextFloat() * 10f
                )
            )
        }
    }

    fun update(delta: Float) {
        pieces.forEach { p ->
            p.vy += 260f * delta // gravity
            p.x += p.vx * delta
            p.y += p.vy * delta
            p.rotation += p.rotSpeed * delta
        }
    }

    fun render(batch: SpriteBatch, dotTexture: Texture) {
        pieces.forEach { p ->
            batch.setColor(p.color)
            batch.draw(
                dotTexture,
                p.x - p.size / 2f, p.y - p.size / 2f,
                p.size / 2f, p.size / 2f,
                p.size, p.size,
                1f, 1f,
                p.rotation,
                0, 0, dotTexture.width, dotTexture.height,
                false, false
            )
        }
        batch.setColor(1f, 1f, 1f, 1f)
    }

    fun clear() = pieces.clear()
    fun isActive(): Boolean = pieces.isNotEmpty()
}
