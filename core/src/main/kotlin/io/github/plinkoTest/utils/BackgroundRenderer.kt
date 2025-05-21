package io.github.plinkoTest.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import kotlin.math.sin

class BackgroundRenderer(private val viewport: ExtendViewport) {
    private lateinit var bg: Texture
    private lateinit var ball1: Texture
    private lateinit var ball2: Texture
    private lateinit var ball3: Texture
    private lateinit var ball4: Texture
    private lateinit var ball5: Texture
    private lateinit var lightning: Texture
    private lateinit var starsTop: Texture

    private var lightningAlpha = 0f
    private var lightningIncreasing = true
    private var shakeTime = 0f

    fun loadAssets() {
        bg = Texture("dvdvd 1.png")
        ball1 = Texture("ball_animated1.png")
        ball2 = Texture("ball_animated2.png")
        ball3 = Texture("ball_animated3.png")
        ball4 = Texture("ball_animated4.png")
        ball5 = Texture("ball_animated5.png")
        lightning = Texture("lightning.png")
        starsTop = Texture("stars_top.png")
    }

    fun render(batch: SpriteBatch, delta: Float) {
        val screenW = viewport.worldWidth
        val screenH = viewport.worldHeight
        shakeTime += delta

        // Малюємо фон
        batch.draw(bg, 0f, 0f, screenW, screenH)

        // Анімація прозорості блискавки (повільніше)
        val fadeSpeed = 0.5f
        if (lightningIncreasing) {
            lightningAlpha += fadeSpeed * delta
            if (lightningAlpha >= 1f) {
                lightningAlpha = 1f
                lightningIncreasing = false
            }
        } else {
            lightningAlpha -= fadeSpeed * delta
            if (lightningAlpha <= 0.5f) {
                lightningAlpha = 0.5f
                lightningIncreasing = true
            }
        }
        batch.setColor(1f, 1f, 1f, lightningAlpha)
        batch.draw(lightning, 0f, 0f, screenW, screenH)
        batch.setColor(1f, 1f, 1f, 1f)

        // Зірки зверху
        batch.draw(
            starsTop,
            0f,
            screenH - starsTop.height.toFloat(),
            screenW,
            starsTop.height.toFloat()
        )

        // Тряска (невелике коливання по Y)
        val shakeOffset = { frequency: Float, amplitude: Float ->
            (sin(shakeTime * frequency) * amplitude).toFloat()
        }

        // М'ячі з тряскою
        val ball1Y = screenH - 1365.68f - ball1.height + shakeOffset(8f, 3f)
        batch.draw(ball1, 0f, ball1Y)

        val ball2X = screenW - ball2.width
        val ball2Y = screenH - ball2.height + shakeOffset(6f, 3f)
        batch.draw(ball2, ball2X, ball2Y)

        batch.draw(ball3, 95f, screenH - 207f - 255f + shakeOffset(4.5f, 2.5f))
        batch.draw(ball4, 900f, screenH - 1607f - 140f + shakeOffset(5.5f, 2.8f), 139f, 140f)
        batch.draw(ball5, 31.4f, 0f + shakeOffset(4f, 2f), 479.2f, 284.0f)
    }

    fun dispose() {
        bg.dispose()
        ball1.dispose()
        ball2.dispose()
        ball3.dispose()
        ball4.dispose()
        ball5.dispose()
        lightning.dispose()
        starsTop.dispose()
    }
}
