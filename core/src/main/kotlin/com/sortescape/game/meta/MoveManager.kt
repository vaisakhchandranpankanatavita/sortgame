package com.sortescape.game.meta

/** Section 16/17: tracks remaining moves and grants the rewarded-ad "+5 moves" revive. */
class MoveManager(private val maxMoves: Int) {

    var movesUsed: Int = 0
        private set

    private var bonusMoves: Int = 0

    val isUnlimited: Boolean get() = maxMoves == 0

    fun remaining(): Int = if (isUnlimited) Int.MAX_VALUE else (maxMoves + bonusMoves - movesUsed)

    fun useMove() {
        movesUsed++
    }

    fun hasMovesLeft(): Boolean = isUnlimited || remaining() > 0

    /** Section 17/28: "Watch Ad -> +5 Moves". */
    fun grantBonusMoves(amount: Int = 5) {
        bonusMoves += amount
    }
}
