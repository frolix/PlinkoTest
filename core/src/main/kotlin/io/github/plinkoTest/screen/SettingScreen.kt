package io.github.plinkoTest.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.plinkoTest.PlinkoGame
import io.github.plinkoTest.utils.*

class SettingScreen(private val game: PlinkoGame) : ScreenAdapter() {
    private lateinit var stage: Stage
    private lateinit var backgroundRenderer: BackgroundRenderer
    private lateinit var headerTexture: Texture
    private lateinit var settingsBg: Texture
    private lateinit var closeBtnTexture: Texture


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
        val backSoundTexture = Texture("settings/sound_back.png")
        val backMusicTexture = Texture("settings/music_back.png")
        val switchOnTexture = Texture("settings/turn_on.png")
        val switchOffTexture = Texture("settings/turn_off.png")

        val viewportWidth = stage.viewport.worldWidth
        val viewportHeight = stage.viewport.worldHeight

        // Кнопка звуку
        val soundOnTexture = TextureRegionDrawable(TextureRegion(Texture("sound_toggle.png")))
        val soundButton = ImageButton(soundOnTexture).apply {
            setSize(120f, 120f)
            setPosition(viewportWidth - width - 30f, viewportHeight - height - 30f)
        }
        stage.addActor(soundButton)

        // Центральний фон
        val settingsBgImage = Image(settingsBg).apply {
            setSize(768f, 768f)
            setPosition((viewportWidth - width) / 2f, (viewportHeight - height) / 2f)
        }
        stage.addActor(settingsBgImage)

        // Розмір фону back_buttons (приблизно зі скріну)
        val buttonBgWidth = 580f
        val buttonBgHeight = 140f

// Y центру першого блока: трохи нижче від верху
        val firstY = settingsBgImage.y + settingsBgImage.height - 340f

// Другий — нижче від першого
        val secondY = firstY - 180f

// Центр по ширині
        val centerX = settingsBgImage.x + (settingsBgImage.width - buttonBgWidth) / 2f

// Перший
        val backButton1 = Image(backSoundTexture).apply {
            setSize(buttonBgWidth, buttonBgHeight)
            setPosition(centerX, firstY)
        }
        stage.addActor(backButton1)

// Другий
        val backButton2 = Image(backMusicTexture).apply {
            setSize(buttonBgWidth, buttonBgHeight)
            setPosition(centerX, secondY)
        }
        stage.addActor(backButton2)

        val switchSoundButton = ImageButton(TextureRegionDrawable(TextureRegion(switchOnTexture))).apply {
            setSize(250f, 100f) // підлаштуй за розміром перемикача
            setPosition(
                backButton1.x + backButton1.width - width - 30f,
                backButton1.y + (backButton1.height - height) / 2f
            )
        }
        stage.addActor(switchSoundButton)

        val switchMusicButton = ImageButton(TextureRegionDrawable(TextureRegion(switchOffTexture))).apply {
            setSize(250f, 100f)
            setPosition(
                backButton2.x + backButton2.width - width - 30f,
                backButton2.y + (backButton2.height - height) / 2f
            )
        }
        stage.addActor(switchMusicButton)

        var isSoundOn = true
        switchSoundButton.addListener(object : ClickListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                isSoundOn = !isSoundOn
                switchSoundButton.style.imageUp = TextureRegionDrawable(TextureRegion(
                    if (isSoundOn) switchOnTexture else switchOffTexture
                ))
            }
        })

        var isMusicOn = true
        switchMusicButton.addListener(object : ClickListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                isMusicOn = !isMusicOn
                switchMusicButton.style.imageUp = TextureRegionDrawable(TextureRegion(
                    if (isMusicOn) switchOnTexture else switchOffTexture
                ))
            }
        })


        // Кнопка закриття
        val closeButton = ImageButton(TextureRegionDrawable(TextureRegion(closeBtnTexture))).apply {
            setSize(200f, 200f)
            setPosition(
                settingsBgImage.x + settingsBgImage.width - width + 30f,
                settingsBgImage.y + settingsBgImage.height - height + 30f
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
        val layout = GlyphLayout(font, "Settings")
        val targetWidth = 638.76f
        val targetHeight = 219.62f
        val x = (stage.viewport.worldWidth - targetWidth) / 2f
        val y = stage.viewport.worldHeight - 258f - targetHeight
        batch.draw(headerTexture, x, y, targetWidth, targetHeight)

        val textX = x + (targetWidth - layout.width) / 2f
        val textY = y + (targetHeight + layout.height) / 2f
        FontManager.drawTextWithShadow(batch, font, "Settings", textX, textY)
        batch.end()

        // Малюємо Stage після заголовка
        stage.act(delta)
        stage.draw()

        // Тепер поверх усього малюємо текст "PLAY"
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
