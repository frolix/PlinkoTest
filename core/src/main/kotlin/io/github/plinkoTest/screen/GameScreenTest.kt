package io.github.plinkoTest.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.github.plinkoTest.PlinkoGame
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import io.github.plinkoTest.ui.ResultWindow
import io.github.plinkoTest.utils.ScreenManager

class GameScreenExtend(private val game: PlinkoGame) : ScreenAdapter(),ContactListener {

    private lateinit var stage: Stage
    private lateinit var camera: OrthographicCamera
    private lateinit var backgroundTexture: Texture
    private lateinit var backgroundImage: Image
    private lateinit var world: World
    private lateinit var debugRenderer: Box2DDebugRenderer
    private lateinit var pegTexture: Texture
    private val pegPositions = mutableListOf<Vector2>()
    private lateinit var batch: com.badlogic.gdx.graphics.g2d.SpriteBatch
    private lateinit var holeTexture: Texture
    private lateinit var holeImage: Image
    private val slots = mutableListOf<Slot>()
    private val slotTextures = mutableListOf<Texture>()
    private val slotSensors = mutableListOf<Body>()
    private lateinit var scoreLabel: Label
    private var scoreText: String = "0"
    private lateinit var font: BitmapFont
    private val layout = GlyphLayout()

    data class Slot(val x: Float, val y: Float, val texture: Texture)
    private var scoreShown = false

    private val balls = mutableListOf<Body>()
    private val maxBalls = 5
    private var droppedBallCount = 0
    private lateinit var ballTexture: Texture

    private var score = 0
    private val slotScores = listOf(10, 20, 30, 40, 40, 30, 20, 10)
    private val bodiesToRemove = mutableListOf<Body>()

    private var gameEnded = false

    private val WORLD_WIDTH = 1080f
    private val WORLD_HEIGHT = 1920f
    private val PPM = 100f

    // Для сітки пегів
    private var gridX = 1f
    private var gridY = 3f
    private var gridWidth = 8f
    private var gridHeight = 6f

    override fun show() {
        camera = OrthographicCamera()
        val viewport = ExtendViewport(WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM, camera)
        stage = Stage(viewport)
        Gdx.input.inputProcessor = stage
        batch = com.badlogic.gdx.graphics.g2d.SpriteBatch()
        createScoreFont() // ← ОБОВ’ЯЗКОВО!
        stage.isDebugAll = true

        // Встановлюємо фон
        backgroundTexture = Texture(Gdx.files.internal("dvdvd 1.png"))
        backgroundImage = Image(backgroundTexture).apply {
            setSize(viewport.worldWidth, viewport.worldHeight)
            setPosition(0f, 0f)
        }
        stage.addActor(backgroundImage)
        pegTexture = Texture(Gdx.files.internal("game/peg_white.png"))
        ballTexture = Texture(Gdx.files.internal("balls/ball1.png")) // або за індексом

        // Фізика
        world = World(Vector2(0f, -9.8f), true)
        debugRenderer = Box2DDebugRenderer()
        world.setContactListener(this)

        // Центруємо поле
        val worldCenterX = viewport.worldWidth / 2f
        val worldCenterY = viewport.worldHeight / 2f
        gridWidth = 8f
        gridHeight = 6f
        gridX = worldCenterX - gridWidth / 2f
        gridY = worldCenterY - gridHeight / 2f
        if (!BlurManager.isInitialized) {
            BlurManager.init(Gdx.graphics.width, Gdx.graphics.height)
        }

        // Створюємо пеги
        createPegGrid()
        ballHole()
        createSlots()
        createScoreBtn()

    }

    private fun createScoreBtn() {
        val buttonTexture = Texture(Gdx.files.internal("game/drop_ball_btn.png"))
        val drawable = TextureRegionDrawable(buttonTexture)
        val buttonStyle = Button.ButtonStyle().apply {
            up = drawable
            down = drawable
            over = drawable
        }

        val viewportWidth = stage.viewport.worldWidth
        val viewportHeight = stage.viewport.worldHeight
        val buttonWidth = viewportWidth * 0.7f
        val buttonHeight = viewportHeight * 0.2f

        val scoreButton = Button(buttonStyle).apply {
            setSize(buttonWidth, buttonHeight)
            setPosition((viewportWidth - buttonWidth) / 2f, viewportHeight * 0.05f)
        }

        scoreButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                dropBall()
            }
        })

        stage.addActor(scoreButton)

    }
    override fun beginContact(contact: Contact) {
        val fixtureA = contact.fixtureA ?: return
        val fixtureB = contact.fixtureB ?: return
        val bodyA = fixtureA.body ?: return
        val bodyB = fixtureB.body ?: return

        val slotIndexA = fixtureA.userData as? Int
        val slotIndexB = fixtureB.userData as? Int

        if (slotIndexA != null && balls.contains(bodyB)) {
            score += slotScores[slotIndexA]
            scoreText = score.toString()
            bodiesToRemove.add(bodyB)
            balls.remove(bodyB) // 🔥 ДОДАЙ ЦЕ
        } else if (slotIndexB != null && balls.contains(bodyA)) {
            score += slotScores[slotIndexB]
            scoreText = score.toString()
            bodiesToRemove.add(bodyA)
            balls.remove(bodyA) // 🔥 ДОДАЙ ЦЕ
        }
    }

    override fun endContact(contact: Contact?) {}
    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}
    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}


    private fun dropBall() {
        if (droppedBallCount >= maxBalls) return

        droppedBallCount++

        val topRowPegCount = 3
        val spacingX = gridWidth / 9f
        val totalWidth = (topRowPegCount - 1) * spacingX
        val startRowX = gridX + (gridWidth - totalWidth) / 2f
        val col = MathUtils.random(0, topRowPegCount - 1)
        val baseX = startRowX + col * spacingX
        val offset = MathUtils.random(-spacingX * 0.1f, spacingX * 0.1f)
        val x = baseX + offset
        val y = gridY + gridHeight + 1f

        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(x, y)
        }

        val shape = CircleShape().apply { radius = 0.18f }

        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            density = 0.6f
            restitution = 0.2f
            friction = 0.1f
        }

        val body = world.createBody(bodyDef)
        val fixture = body.createFixture(fixtureDef)
        fixture.userData = "ball"
        balls.add(body)
        shape.dispose()
    }


    private fun createScoreFont() {
        val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/Inter-Bold.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 256                  // Якість шрифту
            color = Color.WHITE
            kerning = true              // 🟢 ОБОВ’ЯЗКОВО
            magFilter = Texture.TextureFilter.Linear
            minFilter = Texture.TextureFilter.Linear
        }
        font = generator.generateFont(parameter)
        generator.dispose()

        // 🧠 Задаємо розмір у world-одиницях (не в пікселях!)
        val targetFontWorldHeight = stage.viewport.worldHeight * 0.05f // наприклад, 5% від висоти
        val scale = targetFontWorldHeight / font.capHeight
        font.data.setScale(scale)

        Gdx.app.log("FONT", "capHeight=${font.capHeight}, scale=$scale")
    }



    private fun createSlots() {
        val textureOrder = listOf(0, 1, 2, 3, 3, 2, 1, 0)
        val slotCount = textureOrder.size
        val baseSlotWidth = gridWidth / slotCount
        val widthScale = 1.11f
        val slotWidth = baseSlotWidth * widthScale
        val slotHeight = 0.8f
        val totalWidth = slotWidth * slotCount
        val startX = gridX + (gridWidth - totalWidth) / 2f
        val slotY = gridY - slotHeight - 0.3f

        for ((i, index) in textureOrder.withIndex()) {
            val texture = Texture(Gdx.files.internal("slots/slot_$index.png"))
            slotTextures.add(texture)

            val x = startX + i * slotWidth
            val y = slotY
            slots.add(Slot(x, y, texture))

            val bodyDef = BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
                position.set(x + slotWidth / 2f, y + slotHeight / 2f)
            }

            val shape = PolygonShape().apply {
                setAsBox(slotWidth / 2f, slotHeight / 2f)
            }

            val fixtureDef = FixtureDef().apply {
                this.shape = shape
                isSensor = true
            }

            val body = world.createBody(bodyDef)
            val fixture = body.createFixture(fixtureDef)
            fixture.userData = i
            slotSensors.add(body)
            shape.dispose()
        }
    }


    private fun createPegGrid() {
        val pegsPerRow = listOf(3, 4, 5, 6, 7, 8, 9, 10)
        val spacingX = gridWidth / 9f
        val spacingY = gridHeight / 7f

        for ((rowIndex, pegCount) in pegsPerRow.withIndex()) {
            val y = gridY + (7 - rowIndex) * spacingY
            val totalWidth = (pegCount - 1) * spacingX
            val startRowX = gridX + (gridWidth - totalWidth) / 2f

            for (col in 0 until pegCount) {
                val x = startRowX + col * spacingX
                createPeg(x, y)
                pegPositions.add(Vector2(x, y)) // ← ось тут
            }
        }
    }
    private fun ballHole() {
        holeTexture = Texture(Gdx.files.internal("game/ball_hole.png"))
        holeImage = Image(holeTexture)

        val holeSize = 2f // world units
        val holeX = (gridX + gridWidth / 2f) - holeSize / 2f
        val holeY = gridY + gridHeight + 0.3f

        holeImage.setSize(holeSize, holeSize)
        holeImage.setPosition(holeX, holeY)
        holeImage.setOrigin(holeImage.width / 2f, holeImage.height / 2f)
        holeImage.zIndex = 10

        Gdx.app.log("HOLE", "X=$holeX, Y=$holeY, W=$holeSize, H=$holeSize")

        stage.addActor(holeImage)
    }


    private fun createPeg(x: Float, y: Float) {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(x, y)
        }

        val shape = CircleShape().apply { radius = 0.25f }

        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            density = 5f
            restitution = 0.2f
            friction = 0.2f
        }

        world.createBody(bodyDef).createFixture(fixtureDef)
        shape.dispose()
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.log("BACK", "Back button pressed")
            ScreenManager.pop()
            return
        }

        // Видалення тіл з фізичного світу
        for (body in bodiesToRemove) world.destroyBody(body)
        bodiesToRemove.clear()

        // Крок симуляції фізики
        world.step(1 / 60f, 6, 2)

        // Очистка екрану
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Оновлення Stage
        stage.act(delta)
        stage.draw()

        // Малювання всього — тільки якщо гра ще триває
        if (!gameEnded) {
            batch.projectionMatrix = camera.combined
            batch.begin()

            // Рахунок
            val scoreButtonX = stage.viewport.worldWidth / 2f
            val scoreButtonY = stage.viewport.worldHeight * 0.05f + (stage.viewport.worldHeight * 0.2f) / 2f
            layout.setText(font, scoreText)
            font.draw(batch, layout, scoreButtonX - layout.width / 2f, scoreButtonY + layout.height / 2f)

            // Пеги
            for (pos in pegPositions) {
                batch.draw(pegTexture, pos.x - 0.25f, pos.y - 0.25f, 0.5f, 0.5f)
            }

            // Слоти
            for ((i, slot) in slots.withIndex()) {
                val texture = slot.texture
                val slotWidth = gridWidth / 8f * 1.11f
                val slotHeight = 0.8f
                val imageScale = 1.2f
                val imageWidth = slotWidth * imageScale
                val imageHeight = slotHeight * imageScale
                val centerX = slot.x + slotWidth / 2f
                val centerY = slot.y + slotHeight / 2f
                val drawX = centerX - imageWidth / 2f
                val drawY = centerY - imageHeight / 2f
                batch.draw(texture, drawX, drawY, imageWidth, imageHeight)
            }

            // М’ячі
            for (ball in balls) {
                val pos = ball.position
                batch.draw(ballTexture, pos.x - 0.225f, pos.y - 0.225f, 0.45f, 0.45f)
            }

            batch.end()
        }

        // Видалення м’ячів, що вийшли за межі
        balls.removeIf { ball ->
            if (ball.position.y < -1f) {
                world.destroyBody(ball)
                true
            } else false
        }

        // Показати вікно результату
        if (!gameEnded && droppedBallCount == maxBalls && balls.isEmpty()) {
            Gdx.app.log("DEBUG", "Calling showFinalScore(), balls=${balls.size}, dropped=$droppedBallCount")
            gameEnded = true
            if (!scoreShown) {
                scoreShown = true
                showFinalScore()
            }
        }

        camera.update()

        // Малюємо debugRenderer тільки якщо гра не завершена
        if (!gameEnded) {
            debugRenderer.render(world, camera.combined)
        }
    }

    private fun showFinalScore() {
        Gdx.app.log("FINAL", "Your Score: $score")
        Gdx.app.log("FINAL", "gameEnded: $gameEnded")
        Gdx.app.log("FINAL", "stage.isInitialized=" + ::stage.isInitialized)

        if (!::stage.isInitialized) {
            Gdx.app.log("FINAL", "return")
            return
        }

        Gdx.app.log("FINAL", "noreturn")

        val blurredRegion = BlurManager.renderWithBlur({
            stage.act(Gdx.graphics.deltaTime)
            stage.draw()
        }, blurRadius = 15f)

        val blurredImage = Image(TextureRegionDrawable(blurredRegion))
        blurredImage.setSize(stage.viewport.worldWidth, stage.viewport.worldHeight)
        blurredImage.setPosition(0f, 0f)
        blurredImage.zIndex = 999

        stage.addActor(blurredImage)

        val resultWindow = ResultWindow(score, bestScore = 11245, {
            ScreenManager.pop()
        }, {
            ScreenManager.replace(GameScreenExtend(game))
        }, stage)


        stage.addActor(resultWindow)
        resultWindow.toFront()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        backgroundTexture.dispose()
        batch.dispose()
        holeTexture.dispose()
        slotTextures.forEach { it.dispose() }
        ballTexture.dispose()

        pegTexture.dispose()
        world.dispose()
        debugRenderer.dispose()
    }
}
