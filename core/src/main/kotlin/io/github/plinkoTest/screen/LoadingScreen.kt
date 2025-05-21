package io.github.plinkoTest.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Timer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.plinkoTest.PlinkoGame
import io.github.plinkoTest.utils.BackgroundRenderer
import io.github.plinkoTest.utils.FontManager
import io.github.plinkoTest.utils.ScreenManager

class LoadingScreen(private val game: PlinkoGame) : ScreenAdapter() {
    private lateinit var batch: SpriteBatch
    private lateinit var viewport: ExtendViewport
    private lateinit var stage: Stage
    private lateinit var backgroundRenderer: BackgroundRenderer
    private lateinit var centerBall: Texture
    private lateinit var loaderRing: Texture

    private var loaderAngle = 0f
    private var timeSinceLastStep = 0f
    private val rotationStep = 36f
    private val stepInterval = 0.2f

    private var dotState = 0
    private var timeSinceDotUpdate = 0f
    private val dotUpdateInterval = 0.5f

    private var isTransitionScheduled = false

    override fun show() {
        viewport = ExtendViewport(1080f, 1920f)
        stage = Stage(viewport)
        Gdx.input.inputProcessor = stage
        batch = game.batch

        backgroundRenderer = BackgroundRenderer(viewport)
        backgroundRenderer.loadAssets()

        FontManager.loadFonts()

        centerBall = Texture("center_ball.png")
        loaderRing = Texture("loader.png")

        // Перехід на MainMenuScreen через 1 секунди
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                Gdx.app.postRunnable {
                    ScreenManager.clear()
                    ScreenManager.push(MainMenuScreen(game))
                }
            }
        }, 1f)
    }

    override fun render(delta: Float) {
        viewport.apply()
        batch.projectionMatrix = viewport.camera.combined

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val screenW = viewport.worldWidth
        val screenH = viewport.worldHeight

        val centerBallX = (screenW - centerBall.width) / 2f
        val centerBallY = (screenH - centerBall.height) / 2f

        val loaderX = (screenW - loaderRing.width) / 2f
        val loaderY = (screenH - loaderRing.height) / 2f
        val originX = loaderRing.width / 2f
        val originY = loaderRing.height / 2f

        timeSinceLastStep += delta
        if (timeSinceLastStep >= stepInterval) {
            loaderAngle = (loaderAngle + rotationStep) % 360f
            timeSinceLastStep = 0f
        }

        timeSinceDotUpdate += delta
        if (timeSinceDotUpdate >= dotUpdateInterval) {
            dotState = (dotState + 1) % 4
            timeSinceDotUpdate = 0f
        }

        batch.begin()
        backgroundRenderer.render(batch, delta)

        batch.draw(
            loaderRing,
            loaderX, loaderY,
            originX, originY,
            loaderRing.width.toFloat(), loaderRing.height.toFloat(),
            1f, 1f,
            -loaderAngle,
            0, 0,
            loaderRing.width, loaderRing.height,
            false, false
        )

        batch.draw(centerBall, centerBallX, centerBallY)

        val font = FontManager.defaultFont
        val baseText = "LOADING"
        val dots = ".".repeat(dotState)
        val layoutBase = GlyphLayout(font, baseText)
        val layoutDots = GlyphLayout(font, dots)

        val textWidth = layoutBase.width + layoutDots.width + 10f
        val textHeight = layoutBase.height

        val textX = (screenW - textWidth) / 2f
        val dotsX = textX + layoutBase.width + 10f
        val textY = centerBallY / 2f + textHeight / 2f

        FontManager.drawTextWithShadow(batch, font, baseText, textX, textY)
        FontManager.drawTextWithShadow(batch, font, dots, dotsX, textY)

        batch.end()
    }

    override fun dispose() {
        backgroundRenderer.dispose()
        centerBall.dispose()
        loaderRing.dispose()
        FontManager.dispose()
        stage.dispose()
    }
}
