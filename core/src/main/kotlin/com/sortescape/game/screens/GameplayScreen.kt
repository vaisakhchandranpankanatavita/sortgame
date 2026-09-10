package com.sortescape.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.sortescape.game.SortEscapeGame
import com.sortescape.game.data.Category
import com.sortescape.game.data.SpecialType
import com.sortescape.game.gameplay.BoardObject
import com.sortescape.game.gameplay.ContainerSlot
import com.sortescape.game.gameplay.ObjectVisualState
import com.sortescape.game.gameplay.SortListener
import com.sortescape.game.gameplay.SortManager
import com.sortescape.game.graphics.CategoryColors
import com.sortescape.game.graphics.ProceduralArt
import com.sortescape.game.physics.PhysicsWorld
import com.sortescape.game.score.ScoreManager

private enum class Phase { PLAYING, COMPLETED, FAILED }

/**
 * The actual play screen: renders the Box2D board driven by SortManager and turns
 * taps into select/sort calls. One board unit == PhysicsWorld's unit, camera matches
 * PhysicsWorld.boardWidth x boardHeight exactly so world coords == render coords.
 */
class GameplayScreen(private val game: SortEscapeGame, private val levelId: Int) : Screen, SortListener {

    private val physics = PhysicsWorld()
    private val level = game.levelRepository.get(levelId)
    private val sortManager = SortManager(level, physics, this)
    private val scoreManager = ScoreManager()

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(physics.boardWidth, physics.boardHeight, camera)
    private val hudViewport: Viewport = ScreenViewport()

    private val batch = SpriteBatch()
    private val hudFont = BitmapFont().apply { data.setScale(1.6f) }
    private val layout = GlyphLayout()

    private var phase = Phase.PLAYING
    private var comboFlash = 0f
    private var coinsAwarded = 0
    private var starsEarned = 0

    private val panelCache = HashMap<Category, Texture>()
    private val objectCache = HashMap<Category, Texture>()
    private val mysteryTexture: Texture by lazy { ProceduralArt.softCircle(128, CategoryColors.mysteryTop) }
    private val selectionRing: Texture by lazy { ProceduralArt.softCircle(160, CategoryColors.selectionRing) }

    // Overlay art is drawn every frame while the win/fail screen is up, so it must be created
    // once and reused - regenerating a ~300x90 gradient+shadow texture per frame (as this did
    // originally) redoes hundreds of thousands of pixel computations 60 times a second.
    private val overlayDim: Texture by lazy { ProceduralArt.roundedGradientPanel(4, 4, 0, Color(0f, 0f, 0f, 0.55f), Color(0f, 0f, 0f, 0.55f), withShadow = false) }
    private val overlayButtonTexture: Texture by lazy {
        ProceduralArt.roundedGradientPanel(300, 90, 20, CategoryColors.top(Category.ELECTRONICS), CategoryColors.bottom(Category.ELECTRONICS))
    }

    private var nextButtonBounds = Rectangle()
    private var retryButtonBounds = Rectangle()
    private var menuButtonBounds = Rectangle()

    override fun show() {}

    private fun panelFor(category: Category): Texture = panelCache.getOrPut(category) {
        ProceduralArt.roundedGradientPanel(220, 160, 24, CategoryColors.top(category), CategoryColors.bottom(category))
    }

    private fun iconFor(category: Category): Texture = objectCache.getOrPut(category) {
        ProceduralArt.softCircle(128, CategoryColors.top(category))
    }

    override fun render(delta: Float) {
        if (phase == Phase.PLAYING) {
            sortManager.update(delta)
        }
        comboFlash = (comboFlash - delta).coerceAtLeast(0f)
        handleInput()

        Gdx.gl.glClearColor(0.93f, 0.95f, 0.98f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()
        drawContainers()
        drawObjects()
        batch.end()

        drawHud()

        if (phase != Phase.PLAYING) drawOverlay()
    }

    private fun drawContainers() {
        sortManager.containers.forEach { c ->
            val tex = panelFor(c.data.category)
            batch.draw(tex, c.centerX - c.halfWidth, c.centerY - c.halfHeight, c.halfWidth * 2f, c.halfHeight * 2f)
        }
    }

    private fun drawObjects() {
        sortManager.boardObjects.forEach { obj ->
            if (obj.state == ObjectVisualState.SORTED) return@forEach
            val pos = obj.body.position
            val size = obj.radius * 2f

            if (obj.state == ObjectVisualState.SELECTED) {
                val ringSize = size * 1.35f
                batch.draw(selectionRing, pos.x - ringSize / 2f, pos.y - ringSize / 2f, ringSize, ringSize)
            }

            val hideIdentity = obj.instance.special == SpecialType.MYSTERY && !obj.revealed
            val tex = if (hideIdentity) mysteryTexture else iconFor(obj.definition.category)
            batch.draw(tex, pos.x - obj.radius, pos.y - obj.radius, size, size)
        }
    }

    private fun drawHud() {
        hudViewport.apply()
        batch.projectionMatrix = hudViewport.camera.combined
        batch.begin()
        val screenH = hudViewport.worldHeight

        val movesText = if (sortManager.isUnlimitedMoves()) "Moves: unlimited" else "Moves: ${sortManager.remainingMoves()}"
        hudFont.draw(batch, "Level $levelId", 24f, screenH - 24f)
        hudFont.draw(batch, movesText, 24f, screenH - 64f)
        hudFont.draw(batch, "Score: ${scoreManager.score}", 24f, screenH - 104f)
        if (scoreManager.combo > 1 && comboFlash > 0f) {
            hudFont.draw(batch, "Combo x${scoreManager.combo}!", 24f, screenH - 144f)
        }
        batch.end()
    }

    private fun drawOverlay() {
        hudViewport.apply()
        batch.projectionMatrix = hudViewport.camera.combined
        batch.begin()

        val w = hudViewport.worldWidth
        val h = hudViewport.worldHeight
        batch.draw(overlayDim, 0f, 0f, w, h)

        val title = if (phase == Phase.COMPLETED) "Level Complete!" else "Out of Moves"
        layout.setText(hudFont, title)
        hudFont.draw(batch, layout, w / 2f - layout.width / 2f, h * 0.62f)

        if (phase == Phase.COMPLETED) {
            layout.setText(hudFont, "Stars: $starsEarned   Score: ${scoreManager.score}   +$coinsAwarded coins")
            hudFont.draw(batch, layout, w / 2f - layout.width / 2f, h * 0.54f)

            nextButtonBounds = Rectangle(w / 2f - 150f, h * 0.35f, 300f, 90f)
            drawButton(nextButtonBounds, "NEXT LEVEL")
        } else {
            retryButtonBounds = Rectangle(w / 2f - 150f, h * 0.42f, 300f, 90f)
            drawButton(retryButtonBounds, "RETRY")
        }

        menuButtonBounds = Rectangle(w / 2f - 150f, h * 0.26f, 300f, 90f)
        drawButton(menuButtonBounds, "MAIN MENU")

        batch.end()
    }

    private fun drawButton(bounds: Rectangle, label: String) {
        batch.draw(overlayButtonTexture, bounds.x, bounds.y, bounds.width, bounds.height)
        layout.setText(hudFont, label)
        hudFont.draw(batch, layout, bounds.x + bounds.width / 2f - layout.width / 2f, bounds.y + bounds.height / 2f + layout.height / 2f)
    }

    private fun handleInput() {
        if (!Gdx.input.justTouched()) return

        if (phase != Phase.PLAYING) {
            val touch = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            hudViewport.unproject(touch)
            when {
                phase == Phase.COMPLETED && nextButtonBounds.contains(touch.x, touch.y) -> {
                    game.setScreen(GameplayScreen(game, levelId + 1))
                    dispose()
                }
                phase == Phase.FAILED && retryButtonBounds.contains(touch.x, touch.y) -> {
                    game.setScreen(GameplayScreen(game, levelId))
                    dispose()
                }
                menuButtonBounds.contains(touch.x, touch.y) -> {
                    game.setScreen(MainMenuScreen(game))
                    dispose()
                }
            }
            return
        }

        val touch = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        viewport.unproject(touch)
        val container = sortManager.containers.firstOrNull { it.contains(touch.x, touch.y) }
        if (container != null && sortManager.selected != null) {
            sortManager.attemptSortAt(touch.x, touch.y)
        } else {
            sortManager.selectAt(touch.x, touch.y)
        }
    }

    // ---- SortListener ----

    override fun onObjectSelected(obj: BoardObject) {
        game.audioManager.playTap()
        game.hapticManager.tap()
    }

    override fun onSelectionCleared() {}

    override fun onCorrectSort(obj: BoardObject, container: ContainerSlot, reactionTimeSec: Float) {
        scoreManager.onCorrectSort(reactionTimeSec)
        comboFlash = 1.2f
        game.audioManager.playCorrectSort(scoreManager.combo)
        if (scoreManager.combo > 1) game.hapticManager.combo() else game.hapticManager.correctSort()
    }

    override fun onWrongSort(obj: BoardObject, container: ContainerSlot) {
        scoreManager.onWrongSort()
        game.audioManager.playWrongSort()
    }

    override fun onLockedTapBlocked(obj: BoardObject) {
        game.audioManager.playWrongSort()
    }

    override fun onMysteryRevealed(obj: BoardObject) {
        game.audioManager.playTap()
    }

    override fun onLevelComplete() {
        scoreManager.onLevelComplete()
        starsEarned = scoreManager.starsEarned()
        coinsAwarded = game.currencyManager.rewardForLevelCompletion(starsEarned, scoreManager.score, scoreManager.isPerfect())
        game.saveManager.mutate {
            it.levelsPlayed++
            it.totalStars += starsEarned
            if (scoreManager.isPerfect()) it.perfectLevelCount++
            it.bestCombo = maxOf(it.bestCombo, scoreManager.maxCombo)
            if (levelId >= it.currentLevel) it.currentLevel = levelId + 1
        }
        phase = Phase.COMPLETED
        game.audioManager.playLevelComplete()
        game.adManager.showInterstitialIfDue(game.saveManager.data.levelsPlayed) {}
    }

    override fun onLevelFailed() {
        phase = Phase.FAILED
        game.audioManager.playWrongSort()
    }

    override fun onMoveConsumed(movesRemaining: Int) {}

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        hudViewport.update(width, height)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        sortManager.dispose()
        physics.dispose()
        batch.dispose()
        hudFont.dispose()
        panelCache.values.forEach { it.dispose() }
        objectCache.values.forEach { it.dispose() }
        mysteryTexture.dispose()
        selectionRing.dispose()
    }
}
