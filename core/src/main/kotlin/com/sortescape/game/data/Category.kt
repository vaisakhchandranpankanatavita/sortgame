package com.sortescape.game.data

/**
 * The five real-world sorting categories from the design doc (section 6),
 * plus WILDCARD for the special object that can satisfy any container (8.4).
 */
enum class Category(val displayName: String) {
    FOOD("Food"),
    TOYS("Toys"),
    TOOLS("Tools"),
    ELECTRONICS("Electronics"),
    BATHROOM("Bathroom"),
    WILDCARD("Wildcard")
}

enum class SpecialType {
    NONE,
    LOCKED,   // 8.1 cannot be moved until its prerequisite object is sorted
    WILDCARD, // 8.4 can be placed into any of the level's containers
    MYSTERY   // 8.3 shown as "?" until revealed by sorting another object first
}
