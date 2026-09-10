package com.sortescape.game.core

/** Section 39: explicit game states to avoid scattered state logic. */
enum class GameState {
    LOADING,
    MAIN_MENU,
    LEVEL_LOADING,
    PLAYING,
    PAUSED,
    COMPLETED,
    FAILED
}
