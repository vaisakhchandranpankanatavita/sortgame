package com.sortescape.game.data

/**
 * Static definition of a sortable object type (section 40 of the design doc).
 * Runtime instances on the board wrap one of these plus physics/animation state.
 */
data class ObjectData(
    val id: String,
    val name: String,
    val category: Category,
    val score: Int = 100,
    val rarity: Int = 0
)

/** The 20 base objects (5 categories x 4 items) defined verbatim in section 6. */
object ObjectDatabase {

    val ALL: List<ObjectData> = listOf(
        // Food
        ObjectData("food_apple", "Apple", Category.FOOD),
        ObjectData("food_banana", "Banana", Category.FOOD),
        ObjectData("food_pizza", "Pizza", Category.FOOD),
        ObjectData("food_burger", "Burger", Category.FOOD),

        // Toys
        ObjectData("toy_teddy", "Teddy", Category.TOYS),
        ObjectData("toy_ball", "Ball", Category.TOYS),
        ObjectData("toy_robot", "Robot", Category.TOYS),
        ObjectData("toy_car", "Car", Category.TOYS),

        // Tools
        ObjectData("tool_hammer", "Hammer", Category.TOOLS),
        ObjectData("tool_screwdriver", "Screwdriver", Category.TOOLS),
        ObjectData("tool_wrench", "Wrench", Category.TOOLS),
        ObjectData("tool_saw", "Saw", Category.TOOLS),

        // Electronics
        ObjectData("elec_phone", "Phone", Category.ELECTRONICS),
        ObjectData("elec_camera", "Camera", Category.ELECTRONICS),
        ObjectData("elec_laptop", "Laptop", Category.ELECTRONICS),
        ObjectData("elec_headphones", "Headphones", Category.ELECTRONICS),

        // Bathroom
        ObjectData("bath_soap", "Soap", Category.BATHROOM),
        ObjectData("bath_toothbrush", "Toothbrush", Category.BATHROOM),
        ObjectData("bath_shampoo", "Shampoo", Category.BATHROOM),
        ObjectData("bath_towel", "Towel", Category.BATHROOM),

        // Wildcard (8.4) - can be sorted into any container present in the level
        ObjectData("wild_gift", "Gift", Category.WILDCARD, score = 150, rarity = 2)
    )

    fun byCategory(category: Category): List<ObjectData> = ALL.filter { it.category == category }

    fun byId(id: String): ObjectData = ALL.first { it.id == id }

    /** Real categories a level can draw from, excluding the special wildcard pool. */
    val SORTABLE_CATEGORIES: List<Category> = listOf(
        Category.FOOD, Category.TOYS, Category.TOOLS, Category.ELECTRONICS, Category.BATHROOM
    )
}
