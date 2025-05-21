package io.github.plinkoTest.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import io.github.plinkoTest.PlinkoGame
import io.github.plinkoTest.ui.ResultWindow
import io.github.plinkoTest.utils.GamePreferences
import io.github.plinkoTest.utils.ScreenManager

class GameScreen(private val game: PlinkoGame) : ScreenAdapter(), ContactListener {

    private lateinit var stage: Stage
    private lateinit var scoreLabel: Label
    private lateinit var pegCircleTexture: Texture
    private lateinit var backgroundTexture: Texture
    private lateinit var backgroundImage: Image
    private lateinit var world: World
    private lateinit var debugRenderer: Box2DDebugRenderer
    private lateinit var batch: com.badlogic.gdx.graphics.g2d.SpriteBatch
    private lateinit var camera: OrthographicCamera
    private lateinit var holeTexture: Texture
    private lateinit var holeImage: Image
    private var scoreShown = false
    private var finalScoreLabelAdded = false

    private val balls = mutableListOf<Body>()
    private val bodiesToRemove = mutableListOf<Body>()
    private lateinit var ballTexture: Texture

    private val maxBalls = 1 // наприклад
    private var droppedBallCount = 0
    private var gameEnded = false


    private var score = 0
    private var scoreText: String = "0"

    private val WORLD_WIDTH = 1080f
    private val WORLD_HEIGHT = 1920f
    private val PPM = 100f

    private var gridX = 0f
    private var gridY = 0f
    private var gridWidth = 0f
    private var gridHeight = 0f

    private val slotTextures = mutableListOf<Texture>()
    private val slotSensors = mutableListOf<Body>()
    private val slotScores = listOf(10, 20, 30, 40, 40, 30, 20, 10)
    data class Slot(val x: Float, val y: Float, val texture: Texture)
    private val slots = mutableListOf<Slot>()

    override fun show() {
        stage = Stage(ScalingViewport(Scaling.fit, WORLD_WIDTH, WORLD_HEIGHT))
        Gdx.input.inputProcessor = stage

        backgroundTexture = Texture(Gdx.files.internal("dvdvd 1.png"))
        backgroundImage = Image(backgroundTexture)
        backgroundImage.setFillParent(true)
        stage.addActor(backgroundImage)

        // Отримуємо індекс вибраного м’яча
        val selectedBallIndex = GamePreferences.getSelectedBall()
        ballTexture = Texture(Gdx.files.internal("balls/ball$selectedBallIndex.png"))
        camera = OrthographicCamera()
        camera.setToOrtho(false, WORLD_WIDTH / PPM, WORLD_HEIGHT / PPM)
        camera.position.set((WORLD_WIDTH / PPM) / 2f, (WORLD_HEIGHT / PPM) / 2f, 0f)
        camera.update()

        batch = com.badlogic.gdx.graphics.g2d.SpriteBatch()
        pegCircleTexture = Texture(Gdx.files.internal("game/peg_white.png"))
        world = World(Vector2(0f, -9.8f), true)
        debugRenderer = Box2DDebugRenderer()

        world.setContactListener(this)
        resize(Gdx.graphics.width, Gdx.graphics.height) // <-- обов'язково

        // ВАЖЛИВО: спочатку координати
        calculateGridBounds()

        // Потім вже дірка

        createInvisiblePegColliders()
        createSlots()
        createScoreBtn()
        if (!BlurManager.isInitialized) {
            BlurManager.init(Gdx.graphics.width, Gdx.graphics.height)
        }

//        stage.isDebugAll = true
        ballHole()

    }



    private fun ballHole() {
        holeTexture = Texture(Gdx.files.internal("game/ball_hole.png"))
        holeImage = Image(holeTexture)

        val holeSize = 2f // у world units
        val holeX = (gridX + gridWidth / 2f) - holeSize / 2f
        val holeY = gridY + gridHeight + 0.3f

        // Переведення в пікселі
        val pxX = holeX * PPM
        val pxY = holeY * PPM
        val pxSize = holeSize * PPM

        holeImage.setSize(pxSize, pxSize)
        holeImage.setPosition(pxX, pxY)
        holeImage.setOrigin(Align.center)
        holeImage.setZIndex(10)

        Gdx.app.log("HOLE", "X=$pxX, Y=$pxY, W=$pxSize, H=$pxSize")

        stage.addActor(holeImage)
    }

    private fun dropBall() {
        if (droppedBallCount >= maxBalls || gameEnded) return

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

    private fun calculateGridBounds() {
        val worldW = stage.viewport.worldWidth / PPM
        val worldH = stage.viewport.worldHeight / PPM
        gridWidth = worldW * 0.75f
        gridHeight = worldH * 0.35f
        gridX = (worldW - gridWidth) / 2f
        gridY = worldH * 0.35f
    }

    private fun createInvisiblePegColliders() {
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
            }
        }
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

        val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/Inter-Bold.ttf"))
        val fontSize = (buttonHeight * 0.3f).toInt().coerceAtLeast(12)
        val font = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = fontSize
            color = com.badlogic.gdx.graphics.Color.WHITE
        })
        generator.dispose()

        scoreLabel = Label(scoreText, Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE)).apply {
            setSize(buttonWidth, buttonHeight)
            setAlignment(Align.center)
            setPosition(scoreButton.x, scoreButton.y)
            touchable = Touchable.disabled
        }

        stage.addActor(scoreButton)
        stage.addActor(scoreLabel)
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.log("BACK", "Back button pressed")
            ScreenManager.pop()
            return
        }

        for (body in bodiesToRemove) world.destroyBody(body)
        bodiesToRemove.clear()

        world.step(1 / 60f, 6, 2)

        // Очистка екрану
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.act(delta)
        stage.draw()

        // Малюємо пеги/слоти/м’ячі тільки якщо гра не завершена
        if (!gameEnded) {
            batch.projectionMatrix = camera.combined
            batch.begin()

            val spacingX = gridWidth / 9f
            val spacingY = gridHeight / 7f
            val pegsPerRow = listOf(3, 4, 5, 6, 7, 8, 9, 10)

            for ((rowIndex, pegCount) in pegsPerRow.withIndex()) {
                val y = gridY + (7 - rowIndex) * spacingY
                val totalWidth = (pegCount - 1) * spacingX
                val startRowX = gridX + (gridWidth - totalWidth) / 2f
                for (col in 0 until pegCount) {
                    val x = startRowX + col * spacingX
                    batch.draw(pegCircleTexture, x - 0.25f, y - 0.25f, 0.5f, 0.5f)
                }
            }

            val slotCount = 8
            val baseSlotWidth = gridWidth / slotCount
            val slotWidth = baseSlotWidth * 1.11f
            val slotHeight = 0.8f
            val totalWidth = slotWidth * slotCount
            val startX = gridX + (gridWidth - totalWidth) / 2f
            val imageScale = 1.2f

            for ((i, slot) in slots.withIndex()) {
                val texture = slot.texture
                val imageWidth = slotWidth * imageScale
                val imageHeight = slotHeight * imageScale
                val centerX = startX + i * slotWidth + slotWidth / 2f
                val centerY = slot.y + slotHeight / 2f
                val drawX = centerX - imageWidth / 2f
                val drawY = centerY - imageHeight / 2f
                batch.draw(texture, drawX, drawY, imageWidth, imageHeight)
            }

            for (ball in balls) {
                val pos = ball.position
                batch.draw(ballTexture, pos.x - 0.225f, pos.y - 0.225f, 0.45f, 0.45f)
            }

            batch.end()
        }

        // Видалення м’ячів, що впали за межі
        balls.removeIf {
            if (it.position.y < -1f) {
                world.destroyBody(it)
                true
            } else false
        }

        // Показати результат, коли всі м’ячі зникли
        if (!gameEnded && droppedBallCount == maxBalls && balls.isEmpty()) {
            gameEnded = true
            if (!scoreShown) {
                scoreShown = true
                showFinalScore()
            }
        }

        camera.update()
        if (!gameEnded) {
            debugRenderer.render(world, camera.combined)
        }
    }

    private fun showFinalScore() {
        Gdx.app.log("FINAL", "Your Score: $score")
        Gdx.app.log("FINAL", "gameEnded: $gameEnded")
        Gdx.app.log("FINAL", "stage.isInitialized"+::stage.isInitialized)

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
        blurredImage.zIndex = 999 // максимум

        stage.addActor(blurredImage)
//
//        val resultWindow = ResultWindow(score, bestScore = 11245, {
//            ScreenManager.pop()
//        }, {
//            ScreenManager.replace(GameScreen(game))
//        })

//        stage.addActor(resultWindow)

//        resultWindow.toFront() // гарантує, що буде зверху
    }


    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        calculateGridBounds()
    }

    override fun dispose() {
        ballTexture.dispose()
        backgroundTexture.dispose()
        stage.dispose()
        world.dispose()
        debugRenderer.dispose()
        batch.dispose()
        pegCircleTexture.dispose()
        holeTexture.dispose()

        slotTextures.forEach { it.dispose() }
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
            scoreLabel.setText(score)
            bodiesToRemove.add(bodyB)
            balls.remove(bodyB)
        } else if (slotIndexB != null && balls.contains(bodyA)) {
            score += slotScores[slotIndexB]
            scoreLabel.setText(score)
            bodiesToRemove.add(bodyA)
            balls.remove(bodyA)
        }
    }

    override fun endContact(contact: Contact?) {}
    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}
    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}
}
