package com.sortescape.game.graphics

import com.badlogic.gdx.graphics.Color
import com.sortescape.game.data.Category

/** Section 22 palette: one accent pair (top/bottom of the gradient) per real-world category. */
object CategoryColors {

    private val map: Map<Category, Pair<Color, Color>> = mapOf(
        Category.FOOD to (Color(0.98f, 0.55f, 0.42f, 1f) to Color(0.90f, 0.35f, 0.28f, 1f)),
        Category.TOYS to (Color(0.45f, 0.75f, 0.98f, 1f) to Color(0.25f, 0.55f, 0.92f, 1f)),
        Category.TOOLS to (Color(0.75f, 0.75f, 0.80f, 1f) to Color(0.50f, 0.52f, 0.58f, 1f)),
        Category.ELECTRONICS to (Color(0.55f, 0.50f, 0.95f, 1f) to Color(0.35f, 0.30f, 0.80f, 1f)),
        Category.BATHROOM to (Color(0.55f, 0.88f, 0.80f, 1f) to Color(0.30f, 0.70f, 0.62f, 1f)),
        Category.WILDCARD to (Color(0.98f, 0.85f, 0.35f, 1f) to Color(0.92f, 0.65f, 0.15f, 1f))
    )

    fun top(category: Category): Color = map.getValue(category).first
    fun bottom(category: Category): Color = map.getValue(category).second

    val mysteryTop: Color = Color(0.65f, 0.65f, 0.70f, 1f)
    val mysteryBottom: Color = Color(0.45f, 0.45f, 0.50f, 1f)

    val selectionRing: Color = Color(1f, 0.92f, 0.35f, 0.85f)
    val panelTop: Color = Color(1f, 1f, 1f, 0.92f)
    val panelBottom: Color = Color(0.88f, 0.90f, 0.95f, 0.92f)
}
