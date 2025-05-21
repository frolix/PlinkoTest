package io.github.plinkoTest.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

object GamePreferences {
    private val prefs: Preferences = Gdx.app.getPreferences("PlinkoPrefs")

    fun saveSelectedBall(ballIndex: Int) {
        prefs.putInteger("selected_ball", ballIndex)
        prefs.flush()
    }

    fun getSelectedBall(): Int {
        return prefs.getInteger("selected_ball", 1) // 1 — за замовчуванням
    }
}
