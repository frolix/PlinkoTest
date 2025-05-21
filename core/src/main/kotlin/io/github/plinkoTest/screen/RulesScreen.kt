package io.github.plinkoTest.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.plinkoTest.PlinkoGame
import io.github.plinkoTest.utils.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align

class RulesScreen(private val game: PlinkoGame) : ScreenAdapter() {
    private lateinit var stage: Stage
    private lateinit var backgroundRenderer: BackgroundRenderer
    private lateinit var headerTexture: Texture
    private lateinit var settingsBg: Texture
    private lateinit var closeBtnTexture: Texture
    private lateinit var settingsGroup: Group

    override fun show() {
        val viewport = ExtendViewport(1080f, 1920f)
        stage = Stage(viewport)
        Gdx.input.inputProcessor = stage

        backgroundRenderer = BackgroundRenderer(viewport)
        backgroundRenderer.loadAssets()
        FontManager.loadFonts()

        headerTexture = Texture("balls/ball_view.png") // або замінити
        settingsBg = Texture("setting_rules_bcg.png")
        closeBtnTexture = Texture("close_menu_btn.png")

        val viewportWidth = stage.viewport.worldWidth
        val viewportHeight = stage.viewport.worldHeight

        // Кнопка звуку
        val soundOnTexture = TextureRegionDrawable(TextureRegion(Texture("sound_toggle.png")))
        val soundButton = ImageButton(soundOnTexture).apply {
            setSize(120f, 120f)
            setPosition(viewportWidth - width - 30f, viewportHeight - height - 30f)
        }
        stage.addActor(soundButton)

//        // Центральний фон
//        val settingsBgImage = Image(settingsBg).apply {
//            setSize(768f, 768f)
//            setPosition((viewportWidth - width) / 2f, (viewportHeight - height) / 2f)
//        }
//        stage.addActor(settingsBgImage)

        // Центральний фон + текст у Group
        settingsGroup = Group().apply {
            setSize(768f, 768f)
            setPosition((viewportWidth - width) / 2f, (viewportHeight - height) / 2f)
        }

// Фон
        val bgImage = Image(settingsBg).apply {
            setSize(768f, 768f)
            setPosition(0f, 0f)
        }
        settingsGroup.addActor(bgImage)

// Створюємо текст
        val font = FontManager.defaultFont
        font.data.setScale(0.7f) // адаптивно, під 768x768

        val rulesText = """
    Welcome!

    In this game you have to collect
    the most points to be the best
    among the best!

    Everything is very easy and simple!

    Good luck
""".trimIndent()

        val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellGreatPrimer-Regular.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 47 // розмір шрифту, обери який підходить
            color = Color.WHITE
        }
        val smallFont = generator.generateFont(parameter)
        generator.dispose() // обов’язково

        val labelStyle = Label.LabelStyle(smallFont, Color.WHITE)

        val rulesLabel = Label(rulesText, labelStyle).apply {
            width = 768f - 80f
            height = 768f - 160f
            setWrap(true)
            setAlignment(Align.top or Align.center)
            setPosition(40f, settingsGroup.height - height-160f) // ✅ правильна локальна позиція
        }
        settingsGroup.addActor(rulesLabel)

// Додаємо все на сцену
        stage.addActor(settingsGroup)


        // Кнопка закриття
        val closeButton = ImageButton(TextureRegionDrawable(TextureRegion(closeBtnTexture))).apply {
            setSize(200f, 200f)
            setPosition(
                settingsGroup.x + settingsGroup.width - width + 30f,
                settingsGroup.y + settingsGroup.height - height + 30f
            )
            addListener(object : ClickListener() {
                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    super.touchUp(event, x, y, pointer, button)
                    ScreenManager.pop()
                }
            })
        }
        stage.addActor(closeButton)

        // Кнопка PLAY
        val playTexture = Texture("button_bg.png")
        val playButtonDrawable = TextureRegionDrawable(TextureRegion(playTexture))
        val playButton = ImageButton(playButtonDrawable).apply {
            setSize(638.76f, 219.62f)
            setPosition((viewportWidth - width) / 2f, 150f)
            addListener(object : ClickListener() {
                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    super.touchUp(event, x, y, pointer, button)
                    println("PLAY button clicked")
                    ScreenManager.push(GameScreen(game))
                }
            })
        }
        stage.addActor(playButton)
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            ScreenManager.pop()
            return
        }

        stage.viewport.apply()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val batch = stage.batch as SpriteBatch
        batch.begin()
        backgroundRenderer.render(batch, delta)

        // Заголовок
        val font = FontManager.defaultFont
        val layout = GlyphLayout(font, "Rules")
        val targetWidth = 638.76f
        val targetHeight = 219.62f
        val x = (stage.viewport.worldWidth - targetWidth) / 2f
        val y = stage.viewport.worldHeight - 258f - targetHeight
        batch.draw(headerTexture, x, y, targetWidth, targetHeight)

        val textX = x + (targetWidth - layout.width) / 2f
        val textY = y + (targetHeight + layout.height) / 2f
        FontManager.drawTextWithShadow(batch, font, "Rules", textX, textY)

        // 🔻 ДОДАЙ .end()
        batch.end()

        // Малюємо Stage
        stage.act(delta)
        stage.draw()

        // Тепер поверх stage — малюємо текст "PLAY"
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
        stage.dispose()
        backgroundRenderer.dispose()
        headerTexture.dispose()
        settingsBg.dispose()
        closeBtnTexture.dispose()
    }
}
