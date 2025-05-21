package io.github.plinkoTest.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align

class ResultWindow(
    private val yourScore: Int,
    private val bestScore: Int,
    private val onClose: () -> Unit,
    private val onRetry: () -> Unit,
    private val parentStage: Stage
) : Group() {

    init {
        val viewportWidth = parentStage.viewport.worldWidth
        val viewportHeight = parentStage.viewport.worldHeight
        val designWidth = 1080f
        val scale = viewportWidth / designWidth

        val windowWidth = 718.15f * scale
        val windowHeight = 716.92f * scale

        val resultBgTexture = Texture("game/result_bcg.png")
        val background = Image(Texture("game/result_bg.png")).apply {
            setSize(windowWidth, windowHeight)
        }

        val titleHeight = 50f * scale
        val resultBgHeight = 100f * scale
        val verticalSpacing = 24f * scale
        val resultBgWidth = windowWidth * 0.75f

        val contentHeight = titleHeight + verticalSpacing + resultBgHeight +
            verticalSpacing + titleHeight + verticalSpacing + resultBgHeight

        fun generateFontWithSize(size: Int, fontPath: String): BitmapFont {
            val generator = FreeTypeFontGenerator(Gdx.files.internal(fontPath))
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                this.size = size
                color = Color.WHITE
                shadowColor = Color(0f, 0f, 0f, 0.75f)
                shadowOffsetX = 3
                shadowOffsetY = 3
                magFilter = Texture.TextureFilter.Linear
                minFilter = Texture.TextureFilter.Linear
            }
            val font = generator.generateFont(parameter)
            generator.dispose()
            return font
        }


        // --- FONT HELPERS ---
        fun generateFontToFit(text: String, maxWidth: Float, maxHeight: Float, fontPath: String, startSize: Int = 96): BitmapFont {
            var fontSize = startSize
            var font = generateFontWithSize(fontSize, fontPath)
            val layout = GlyphLayout()

            while (fontSize > 10) {
                layout.setText(font, text)
                if (layout.width <= maxWidth && layout.height <= maxHeight) break
                font.dispose()
                fontSize -= 2
                font = generateFontWithSize(fontSize, fontPath)
            }

            // ⚠ Тут найважливіше – виставити scale для world-координат:
            val scale = maxHeight / font.capHeight
            font.data.setScale(scale)

            return font
        }


//        fun generateFontToFit(text: String, maxWidth: Float, maxHeight: Float, fontPath: String, startSize: Int = 96): BitmapFont {
//            var fontSize = startSize
//            var font = generateFontWithSize(fontSize, fontPath)
//            val layout = GlyphLayout()
//            while (fontSize > 10) {
//                layout.setText(font, text)
//                if (layout.width <= maxWidth && layout.height <= maxHeight) break
//                font.dispose()
//                fontSize -= 2
//                font = generateFontWithSize(fontSize, fontPath)
//            }
//            return font
//        }

        val titleFont = generateFontToFit("Your result:", windowWidth, titleHeight, "fonts/IMFellGreatPrimer-Regular.ttf")
        val scoreFont = generateFontToFit("%,d".format(yourScore), resultBgWidth, resultBgHeight, "fonts/Inter-Bold.ttf")
        val bestFont = generateFontToFit("%,d".format(bestScore), resultBgWidth, resultBgHeight, "fonts/Inter-Bold.ttf")

        val styleTitle = Label.LabelStyle(titleFont, Color.WHITE)
        val styleScore = Label.LabelStyle(scoreFont, Color.WHITE)
        val styleBestScore = Label.LabelStyle(bestFont, Color.GOLD)

        // --- BLOCK UI ---
        val block = Group()
        var currentY = contentHeight

        currentY -= titleHeight
        val title = Label("Your result:", styleTitle).apply {
            setSize(windowWidth, titleHeight)
            setAlignment(Align.center)
            setPosition(0f, currentY)
        }

        currentY -= (verticalSpacing + resultBgHeight)
        val scoreBg = Image(resultBgTexture).apply {
            setSize(resultBgWidth, resultBgHeight)
            setPosition((windowWidth - resultBgWidth) / 2f, currentY)
        }

        val scoreLabel = Label("%,d".format(yourScore), styleScore).apply {
            setSize(resultBgWidth, resultBgHeight)
            setAlignment(Align.center)
            setPosition(scoreBg.x, scoreBg.y)
        }

        currentY -= (verticalSpacing + titleHeight)
        val bestTitle = Label("Best result:", styleTitle).apply {
            setSize(windowWidth, titleHeight)
            setAlignment(Align.center)
            setPosition(0f, currentY)
        }

        currentY -= (verticalSpacing + resultBgHeight)
        val bestBg = Image(resultBgTexture).apply {
            setSize(resultBgWidth, resultBgHeight)
            setPosition((windowWidth - resultBgWidth) / 2f, currentY)
        }

        val bestLabel = Label("%,d".format(bestScore), styleBestScore).apply {
            setSize(resultBgWidth, resultBgHeight)
            setAlignment(Align.center)
            setPosition(bestBg.x, bestBg.y)
        }

        block.addActor(title)
        block.addActor(scoreBg)
        block.addActor(scoreLabel)
        block.addActor(bestTitle)
        block.addActor(bestBg)
        block.addActor(bestLabel)
        block.setSize(windowWidth, contentHeight)
        block.setPosition(0f, (windowHeight - contentHeight) / 2f)

        // --- BUTTONS ---
        val retryTexture = Texture("game/retry.png")
        val closeTexture = Texture("game/close_agree.png")
        val buttonWidth = 180f * scale
        val retryAspect = retryTexture.height.toFloat() / retryTexture.width
        val closeAspect = closeTexture.height.toFloat() / closeTexture.width

        val retryButton = Image(retryTexture).apply {
            val buttonHeight = buttonWidth * retryAspect
            setSize(buttonWidth, buttonHeight)
            setPosition(40f * scale, -15f * scale)
            addListener(object : InputListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    onRetry()
                    return true
                }
            })
        }

        val closeButton = Image(closeTexture).apply {
            val buttonHeight = buttonWidth * closeAspect
            setSize(buttonWidth, buttonHeight)
            setPosition(windowWidth - buttonWidth - 40f * scale, -15f * scale)
            addListener(object : InputListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    onClose()
                    return true
                }
            })
        }

        // --- ADD TO STAGE ---
        addActor(background)
        addActor(block)
        addActor(retryButton)
        addActor(closeButton)
        setSize(windowWidth, windowHeight)
        setPosition((viewportWidth - width) / 2f, (viewportHeight - height) / 2f)
    }
}
