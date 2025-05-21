package io.github.plinkoTest.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.plinkoTest.PlinkoGame
import io.github.plinkoTest.utils.BackgroundRenderer
import io.github.plinkoTest.utils.FontManager
import io.github.plinkoTest.utils.ScreenManager

class MainMenuScreen(private val game: PlinkoGame) : ScreenAdapter() {
    private lateinit var stage: Stage
    private lateinit var backgroundRenderer: BackgroundRenderer
    private var isSoundEnabled = true

    override fun show() {
        Gdx.app.log("MainMenuScreen", "SHOW")

        val viewport = ExtendViewport(1080f, 1920f)
        stage = Stage(viewport)
        Gdx.input.inputProcessor = stage

        backgroundRenderer = BackgroundRenderer(viewport)
        backgroundRenderer.loadAssets()

        FontManager.loadFonts()
        Gdx.app.log("MainMenuScreen", "SHOW2")


        initUI()

    }

    override fun resume() {
        super.resume()
        Gdx.app.log("MainMenuScreen", "resume")

    }

    private fun initUI() {


        val buttonDrawable = TextureRegionDrawable(TextureRegion(Texture("button_bg.png")))

        val table = Table().apply {
            setFillParent(true)
            defaults().pad(20f)
        }


        val buttonLabels = listOf("PLAY", "BALL", "RULES", "SETTINGS", "EXIT")
        for (labelText in buttonLabels) {
            val maxTextWidth = 640f - 40f
            val maxTextHeight = 180f

            val startFontSize = if (labelText == "SETTINGS") 100 else 132
            val fittingFont = generateFontWithSizeFit(labelText, maxTextWidth, maxTextHeight, startFontSize)

            val style = Button.ButtonStyle().apply {
                up = buttonDrawable
                down = buttonDrawable
            }

            val button = Button(style).apply {
                setSize(640f, 220f)
            }

            val label = Label(labelText, Label.LabelStyle(fittingFont, Color.WHITE)).apply {
                setAlignment(1)
            }

            button.add(label).center().expand()

            button.addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button_: Int): Boolean {
                    button.color = Color(0.7f, 0.7f, 0.7f, 1f)
                    return true
                }

                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button_: Int) {
                    button.color = Color.WHITE
                    when (labelText) {
                        "PLAY" -> ScreenManager.push(GameScreenExtend(game))
                        "BALL" -> ScreenManager.push(BallSelectionScreen(game))
                        "RULES" -> ScreenManager.push(RulesScreen(game))
                        "SETTINGS" -> ScreenManager.push(SettingScreen(game))

                        "EXIT" -> Gdx.app.exit()
                    }
                }
            })

            table.add(button).width(button.width).height(button.height)
            table.row()
        }

        stage.addActor(table)

        // Кнопка звуку
        val soundOnTexture = TextureRegionDrawable(TextureRegion(Texture("sound_toggle.png")))
        val soundOffTexture = TextureRegionDrawable(TextureRegion(Texture("sound_toggle_off.png")))
        val viewportWidth = stage.viewport.worldWidth
        val viewportHeight = stage.viewport.worldHeight

        val soundButton = ImageButton(soundOnTexture).apply {
            setSize(120f, 120f)

            setPosition(
                viewportWidth - width - 30f,
                viewportHeight - height - 30f
            )
        }

        soundButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                isSoundEnabled = !isSoundEnabled
                soundButton.style.imageUp = if (isSoundEnabled) soundOnTexture else soundOffTexture
            }
        })

        stage.addActor(soundButton)

    }


    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACK)) {
            Gdx.app.log("MainMenuScreen", "BACK pressed — exiting")
            // Очищаємо стек і виходимо
            ScreenManager.clear()
            Gdx.app.exit()
            return
        }

        stage.viewport.apply()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        (stage.batch as SpriteBatch).begin()
        backgroundRenderer.render(stage.batch as SpriteBatch, delta)
        (stage.batch as SpriteBatch).end()

        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        Gdx.app.log("MainMenuScreen", "dispose main menu")

        stage.dispose()
        backgroundRenderer.dispose()
        FontManager.dispose()
    }

    private fun generateFontWithSize(size: Int): BitmapFont {
        val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellGreatPrimer-Regular.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            this.size = size
            color = Color.WHITE
            spaceX = (-0.06f * size).toInt()
        }
        val font = generator.generateFont(parameter)
        generator.dispose()
        return font
    }

    private fun generateFontWithSizeFit(text: String, maxWidth: Float, maxHeight: Float, startSize: Int): BitmapFont {
        var fontSize = startSize
        var font = generateFontWithSize(fontSize)
        val layout = GlyphLayout()

        while (fontSize > 10) {
            layout.setText(font, text)
            if (layout.width <= maxWidth && layout.height <= maxHeight) break
            font.dispose()
            fontSize -= 2
            font = generateFontWithSize(fontSize)
        }

        return font
    }

}
