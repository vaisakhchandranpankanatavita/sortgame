package com.sortescape.game.physics

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*

/**
 * Real Box2D simulation backing the board (section 25's "Move / Bounce / Snap" and the
 * "Shake left / Shake right / Return" wrong-sort animation are genuine physics responses
 * here, not scripted tweens). Coordinates are in "board units": a portrait board roughly
 * 10 units wide by 17.8 units tall (matches a 720x1280 virtual viewport at 72 px/unit).
 */
class PhysicsWorld(val boardWidth: Float = 10f, val boardHeight: Float = 17.8f) {

    val world: World = World(Vector2(0f, -9.8f), true)

    /** Pile area (top) where loose objects rest under gravity before being sorted. */
    val pileTop = boardHeight - 1.2f
    val pileBottom = boardHeight * 0.42f
    val pileLeft = 0.6f
    val pileRight = boardWidth - 0.6f

    init {
        createWalls()
    }

    private fun createWalls() {
        val groundDef = BodyDef().apply { type = BodyDef.BodyType.StaticBody; position.set(0f, 0f) }
        val ground = world.createBody(groundDef)
        addEdge(ground, pileLeft, pileBottom, pileRight, pileBottom)
        addEdge(ground, pileLeft, pileBottom, pileLeft, pileTop)
        addEdge(ground, pileRight, pileBottom, pileRight, pileTop)
    }

    private fun addEdge(body: Body, x1: Float, y1: Float, x2: Float, y2: Float) {
        val shape = EdgeShape().apply { set(x1, y1, x2, y2) }
        val fixture = FixtureDef().apply {
            this.shape = shape
            friction = 0.6f
            restitution = 0.15f
        }
        body.createFixture(fixture)
        shape.dispose()
    }

    /** Spawns a dynamic circular body for one board object, dropped from a random spot above the pile. */
    fun spawnObjectBody(radius: Float, spawnX: Float, spawnY: Float): Body {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(spawnX, spawnY)
            linearDamping = 0.6f
            angularDamping = 0.9f
        }
        val body = world.createBody(bodyDef)
        val shape = CircleShape().apply { this.radius = radius }
        val fixture = FixtureDef().apply {
            this.shape = shape
            density = 1f
            friction = 0.5f
            restitution = 0.35f
        }
        body.createFixture(fixture)
        shape.dispose()
        return body
    }

    /** Steers a body toward a target with a spring-like force (used to fly a sorted object into its container). */
    fun applyGuidedForce(body: Body, target: Vector2, strength: Float = 40f) {
        val toTarget = target.cpy().sub(body.position)
        val force = toTarget.scl(strength)
        val damping = body.linearVelocity.cpy().scl(-2f * strength.let { 6f })
        body.applyForceToCenter(force.add(damping), true)
    }

    /** Section 19 "Wrong Sort: Shake left / Shake right / Return" - a real impulse, not an animation curve. */
    fun applyShake(body: Body) {
        val dir = if (body.position.x > boardWidth / 2f) -1f else 1f
        body.applyLinearImpulse(Vector2(dir * 2.2f, 3.2f), body.worldCenter, true)
    }

    fun step(delta: Float) {
        val clamped = if (delta > 0.25f) 0.25f else delta
        world.step(clamped, 6, 2)
    }

    fun destroyBody(body: Body) {
        world.destroyBody(body)
    }

    fun dispose() {
        world.dispose()
    }
}
