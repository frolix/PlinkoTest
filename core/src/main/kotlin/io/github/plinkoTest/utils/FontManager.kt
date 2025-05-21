package io.github.plinkoTest.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

object FontManager {
    lateinit var defaultFont: BitmapFont

    fun loadFonts() {
        val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellGreatPrimer-Regular.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 132
            color = Color.WHITE
            spaceX = (-0.06f * size).toInt()
        }
        defaultFont = generator.generateFont(parameter)
        generator.dispose()
    }

    fun dispose() {
        defaultFont.dispose()
    }

    fun drawTextWithShadow(
        batch: SpriteBatch,
        font: BitmapFont = defaultFont,
        text: String,
        x: Float,
        y: Float,
        shadowOffset: Float = 8f
    ) {
        font.color = Color.BLACK
        font.draw(batch, text, x - shadowOffset, y)

        font.color = Color.WHITE
        font.draw(batch, text, x, y)
    }
}
