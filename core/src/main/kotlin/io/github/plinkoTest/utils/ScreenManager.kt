package io.github.plinkoTest.utils

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen

object ScreenManager {
    private lateinit var game: Game
    private val screenStack = mutableListOf<Screen>()

    fun initialize(gameInstance: Game) {
        game = gameInstance
    }

    fun push(screen: Screen) {
        Gdx.app.log("ScreenManager", "PUSH: ${screen::class.simpleName}")

        screenStack.lastOrNull()?.pause()
        screenStack.add(screen)
        game.setScreen(screen)

        Gdx.app.log("ScreenManager", "Stack size after push: ${screenStack.size}")
    }

    fun replace(screen: Screen) {
        Gdx.app.log("ScreenManager", "REPLACE: ${screen::class.simpleName}")

        if (screenStack.isNotEmpty()) {
            val current = screenStack.removeAt(screenStack.lastIndex)
            Gdx.app.postRunnable {
                current.dispose()
            }
        }

        screenStack.add(screen)
        game.setScreen(screen)

        Gdx.app.log("ScreenManager", "Stack size after replace: ${screenStack.size}")
    }

    fun pop() {
        Gdx.app.log("ScreenManager", "Stack size before pop: ${screenStack.size}")

        if (screenStack.size > 1) {
            val current = screenStack.removeAt(screenStack.lastIndex)
            val previous = screenStack.last()

            Gdx.app.log("ScreenManager", "POP -> current: ${current::class.simpleName}, previous: ${previous::class.simpleName}")
            game.setScreen(previous)

            Gdx.app.postRunnable {
                current.dispose()
            }
        } else {
            Gdx.app.log("ScreenManager", "POP -> only one screen left, exiting app")
            clear()
            Gdx.app.exit()
        }

        Gdx.app.log("ScreenManager", "Stack size after pop: ${screenStack.size}")
    }

    fun clear() {
        Gdx.app.log("ScreenManager", "CLEARING all screens")
        screenStack.forEach { it.dispose() }
        screenStack.clear()
    }

    fun clearAndSet(screen: Screen) {
        Gdx.app.log("ScreenManager", "CLEAR AND SET: ${screen::class.simpleName}")
        clear()
        push(screen)
    }

    fun current(): Screen? = screenStack.lastOrNull()
}
