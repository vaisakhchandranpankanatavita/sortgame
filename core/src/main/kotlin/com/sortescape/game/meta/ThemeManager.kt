package com.sortescape.game.meta

data class ThemeData(val id: String, val displayName: String, val unlockCost: Int, val topColor: FloatArray, val bottomColor: FloatArray)

/** Section 23/24: cosmetic-only backgrounds. Colors are RGB triples used to build a gradient. */
object Themes {
    val ALL = listOf(
        ThemeData("Home", "Home", 0, floatArrayOf(0.93f, 0.95f, 0.98f), floatArrayOf(0.80f, 0.85f, 0.95f)),
        ThemeData("Kitchen", "Kitchen", 300, floatArrayOf(1.00f, 0.93f, 0.80f), floatArrayOf(0.95f, 0.75f, 0.55f)),
        ThemeData("Garage", "Garage", 500, floatArrayOf(0.85f, 0.85f, 0.88f), floatArrayOf(0.55f, 0.55f, 0.60f)),
        ThemeData("Beach", "Beach", 700, floatArrayOf(0.75f, 0.93f, 1.00f), floatArrayOf(0.98f, 0.90f, 0.65f)),
        ThemeData("Space", "Space", 1000, floatArrayOf(0.10f, 0.08f, 0.25f), floatArrayOf(0.02f, 0.02f, 0.08f))
    )

    fun byId(id: String) = ALL.firstOrNull { it.id == id } ?: ALL.first()
}

class ThemeManager(private val save: SaveManager, private val currency: CurrencyManager) {

    fun unlockedThemes(): List<ThemeData> = Themes.ALL.filter { save.data.unlockedThemes.contains(it.id) }
    fun isUnlocked(id: String): Boolean = save.data.unlockedThemes.contains(id)
    fun equipped(): ThemeData = Themes.byId(save.data.equippedTheme)

    fun equip(id: String) {
        if (isUnlocked(id)) save.mutate { it.equippedTheme = id }
    }

    /** Section 14: themes are purchased with coins; cosmetics never affect gameplay (section 24). */
    fun tryUnlock(id: String): Boolean {
        val theme = Themes.byId(id)
        if (isUnlocked(theme.id)) return true
        if (currency.spendCoins(theme.unlockCost)) {
            save.mutate { it.unlockedThemes.add(theme.id) }
            return true
        }
        return false
    }
}
