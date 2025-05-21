import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram

object BlurManager {
    lateinit var fboA: FrameBuffer
    lateinit var fboB: FrameBuffer
    lateinit var shader: ShaderProgram
    lateinit var batch: SpriteBatch
    var isInitialized = false
        private set

    fun init(width: Int, height: Int) {
        if (isInitialized) return

        fboA = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        fboB = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)

        shader = ShaderProgram(Gdx.files.internal("shaders/blur.vert"), Gdx.files.internal("shaders/blur.frag"))
        if (!shader.isCompiled) Gdx.app.error("Shader", shader.log)

        batch = SpriteBatch()
        isInitialized = true

    }

    fun renderWithBlur(drawScene: () -> Unit, blurRadius: Float): com.badlogic.gdx.graphics.g2d.TextureRegion {
        Gdx.gl.glDisable(GL20.GL_BLEND)

        // 1. Render scene to fboA
        fboA.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        drawScene()
        fboA.end()

        // 2. Blur horizontally → fboB
        fboB.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.shader = shader
        shader.setUniformf("dir", 1f, 0f)
        shader.setUniformf("radius", blurRadius)
        shader.setUniformf("resolution", Gdx.graphics.width.toFloat())
        batch.begin()
        batch.draw(fboA.colorBufferTexture, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat(), 0f, 0f, 1f, 1f)
        batch.end()
        fboB.end()

        // 3. Blur vertically → back to fboA
        fboA.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        shader.setUniformf("dir", 0f, 1f)
        shader.setUniformf("radius", blurRadius)
        shader.setUniformf("resolution", Gdx.graphics.height.toFloat())
        batch.begin()
        batch.draw(fboB.colorBufferTexture, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat(), 0f, 0f, 1f, 1f)
        batch.end()
        fboA.end()

        batch.shader = null
        Gdx.gl.glEnable(GL20.GL_BLEND)

        // Flip vertically
        val region = com.badlogic.gdx.graphics.g2d.TextureRegion(fboA.colorBufferTexture)
        region.flip(false, true)
        return region
    }

}
