package io.github.plinkoTest

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.plinkoTest.screen.LoadingScreen
import io.github.plinkoTest.utils.ScreenManager

class PlinkoGame : Game() {
    lateinit var batch: SpriteBatch

    override fun create() {
        batch = SpriteBatch()
        ScreenManager.initialize(this)
        ScreenManager.clearAndSet(LoadingScreen(this))
    }

    override fun dispose() {
        batch.dispose()
//        ScreenManager.current()?.dispose()
    }
}
