package io.github.plinkoTest.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.plinkoTest.PlinkoGame
import io.github.plinkoTest.utils.BackgroundRenderer
import io.github.plinkoTest.utils.FontManager
import io.github.plinkoTest.utils.ScreenManager
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.Color
import io.github.plinkoTest.utils.GamePreferences

class BallSelectionScreen(private val game: PlinkoGame) : ScreenAdapter() {
    private lateinit var stage: Stage
    private val ballTextures = mutableListOf<Texture>()
    private lateinit var backgroundRenderer: BackgroundRenderer
    private lateinit var ballViewTexture: Texture
    private val ballButtons = mutableListOf<ImageButton>()


    override fun show() {
        val viewport = ExtendViewport(1080f, 1920f)
        stage = Stage(viewport)
        Gdx.input.inputProcessor = stage

        backgroundRenderer = BackgroundRenderer(viewport)
        backgroundRenderer.loadAssets()
        FontManager.loadFonts()

        ballViewTexture = Texture("balls/ball_view.png")

        // Кнопка звуку
        val soundOnTexture = TextureRegionDrawable(TextureRegion(Texture("sound_toggle.png")))
        val viewportWidth = stage.viewport.worldWidth
        val viewportHeight = stage.viewport.worldHeight

        val soundButton = ImageButton(soundOnTexture).apply {
            setSize(120f, 120f)

            setPosition(
                viewportWidth - width - 30f,
                viewportHeight - height - 30f
            )
        }
        stage.addActor(soundButton)

        val table = Table()
        table.setFillParent(true)
        table.center()
        table.defaults().pad(20f)
        val selectedBall = GamePreferences.getSelectedBall()


        for (i in 1..6) {
            val texture = Texture("balls/ball$i.png")
            ballTextures.add(texture)

            val drawable = TextureRegionDrawable(TextureRegion(texture))
            val button = ImageButton(drawable).apply {
                setSize(288f, 288f)
                isTransform = true
                setScale(if (i == selectedBall) 1.1f else 1.0f)
            }

            // ✅ Додай в список, інакше не оновиться при виборі
            ballButtons.add(button)

            button.addListener(object : ClickListener() {
                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, buttonIndex: Int) {
                    super.touchUp(event, x, y, pointer, buttonIndex)
                    println("Selected ball $i")
                    GamePreferences.saveSelectedBall(i)
                    highlightSelectedBall(i)
                }
            })

            table.add(button).size(288f, 288f)
            if (i % 3 == 0) table.row()
        }

        // Завантажуємо текстуру кнопки
// Завантаження текстури кнопки
        val playTexture = Texture("button_bg.png") // або ball_view.png, якщо однакове оформлення
        val playButtonDrawable = TextureRegionDrawable(TextureRegion(playTexture))

// Створюємо кнопку
        val playButton = ImageButton(playButtonDrawable)
        playButton.setSize(638.76f, 219.62f)

// Безпечно отримуємо ширину вьюпорту для позиціонування
        val x = (viewportWidth - playButton.width) / 2f
        val y = 150f // відступ від низу

        playButton.setPosition(x, y)

// Додаємо слухача
        playButton.addListener(object : ClickListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                println("PLAY button clicked")
                ScreenManager.push(GameScreen(game))
            }
        })

// Додаємо кнопку на сцену
        stage.addActor(playButton)
        stage.addActor(table)


    }

    private fun highlightSelectedBall(index: Int) {
        for ((i, button) in ballButtons.withIndex()) {
            button.isTransform = true
            if (i == index - 1) {
                button.setScale(1.1f) // виділяємо — збільшуємо
            } else {
                button.setScale(1.0f) // звичайний розмір
            }
        }
    }


    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.log("BACK", "Back button pressed")
            ScreenManager.pop()
            return
        }

        stage.viewport.apply()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = stage.batch as SpriteBatch
        batch.begin()
        backgroundRenderer.render(batch, delta)

        // Малюємо кнопку "Ball"
        val targetWidth = 638.76f
        val targetHeight = 219.62f

        val screenWidth = stage.viewport.worldWidth
        val screenHeight = stage.viewport.worldHeight

        val x = (screenWidth - targetWidth) / 2f
        val y = screenHeight - 258f - targetHeight // 258px від верху

        batch.draw(ballViewTexture, x, y, targetWidth, targetHeight)

        val font = FontManager.defaultFont
        val layout = GlyphLayout(font, "Ball")
        val textX = x + (targetWidth - layout.width) / 2f
        val textY = y + (targetHeight + layout.height) / 2f
        FontManager.drawTextWithShadow(batch, font, "Ball", textX, textY)

        batch.end()

        // Малюємо Stage
        stage.act(delta)
        stage.draw()

        // Тепер — поверх усього — малюємо текст "PLAY"
        batch.begin()

        val playText = "PLAY"
        val layoutPlay = GlyphLayout(font, playText)

        val playX = (stage.viewport.worldWidth - 638.76f) / 2f
        val playY = 150f

        val playTextX = playX + (638.76f - layoutPlay.width) / 2f
        val playTextY = playY + (219.62f + layoutPlay.height) / 2f

        FontManager.drawTextWithShadow(batch, font, playText, playTextX, playTextY)

        batch.end()
    }


    override fun dispose() {
        Gdx.app.log("BallSelectionScreen", "dispose")
        stage.dispose()
        backgroundRenderer.dispose()
        ballViewTexture.dispose()
        ballTextures.forEach { it.dispose() }
    }
}
