package com.sortescape.game.meta

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter

/**
 * Section 43: "Use a robust save format rather than relying on individual PlayerPrefs values
 * for everything." Serializes the whole SaveData as one JSON blob into a single libGDX
 * Preferences key, so all progress round-trips atomically.
 */
class SaveManager(private val prefsName: String = "sort_escape_save") {

    private val json = Json().apply { setOutputType(JsonWriter.OutputType.json) }
    private val PREF_KEY = "save_blob"

    var data: SaveData = load()
        private set

    private fun load(): SaveData {
        return try {
            val prefs = Gdx.app.getPreferences(prefsName)
            val blob = prefs.getString(PREF_KEY, "")
            if (blob.isNullOrBlank()) SaveData() else json.fromJson(SaveData::class.java, blob)
        } catch (e: Exception) {
            SaveData()
        }
    }

    fun save() {
        val prefs = Gdx.app.getPreferences(prefsName)
        prefs.putString(PREF_KEY, json.toJson(data))
        prefs.flush()
    }

    fun mutate(block: (SaveData) -> Unit) {
        block(data)
        save()
    }
}
