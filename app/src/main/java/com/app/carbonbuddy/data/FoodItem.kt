package com.app.carbonbuddy.data

data class FoodItem(
    val name: String,
    val emissionFactor: Double, // in grams
    val category: String,
    val icon: String = ""
)

object FoodDatabase {
    val foodItems = listOf(
        FoodItem("Dal", 600.0, "Veg", "🍲"),
        FoodItem("Rice", 1200.0, "Veg", "🍚"),
        FoodItem("Paneer", 2400.0, "Dairy", "🧀"),
        FoodItem("Chicken", 3200.0, "Non-Veg", "🍗"),
        FoodItem("Tofu", 800.0, "Veg", "🧆"),
        FoodItem("Egg", 2000.0, "Dairy", "🍳"),
        FoodItem("Salad", 400.0, "Veg", "🥗"),
        FoodItem("Milk", 1800.0, "Dairy", "🥛"),
        FoodItem("Roti", 700.0, "Veg", "🍞"),
        FoodItem("Mixed Veg Curry", 900.0, "Veg", "🍛"),
        FoodItem("Fish", 2800.0, "Non-Veg", "🐟"),
        FoodItem("Pasta", 1100.0, "Veg", "🍝"),
        FoodItem("Bread", 850.0, "Veg", "🥖"),
        FoodItem("Fruits", 300.0, "Veg", "🍎"),
        FoodItem("Vegetables", 450.0, "Veg", "🥦"),
        FoodItem("Butter", 2200.0, "Dairy", "🧈"),
        FoodItem("Cheese", 2600.0, "Dairy", "🧀"),
        FoodItem("Beef", 3500.0, "Non-Veg", "🥩"),
        FoodItem("Lamb", 3300.0, "Non-Veg", "🐑"),
        FoodItem("Pork", 2900.0, "Non-Veg", "🐖"),
        FoodItem("Soy Milk", 400.0, "Dairy", "🥛"),
        FoodItem("Almonds", 1500.0, "Veg", "🥜"),
        FoodItem("Nuts", 1600.0, "Veg", "🥜"),
        FoodItem("Yogurt", 1900.0, "Dairy", "🥗"),
        FoodItem("Coffee", 210.0, "Veg", "☕"),
        FoodItem("Tea", 180.0, "Veg", "🍵"),
        FoodItem("Chocolate", 1700.0, "Veg", "🍫"),
        FoodItem("Ice Cream", 2300.0, "Dairy", "🍦"),
        FoodItem("Pizza", 2500.0, "Veg", "🍕"),
        FoodItem("Burger", 2700.0, "Non-Veg", "🍔"),
        FoodItem("Sushi", 2400.0, "Non-Veg", "🍣"),
        FoodItem("Pasta", 1100.0, "Veg", "🍝"),
        FoodItem("Bread", 850.0, "Veg", "🥖"),
        FoodItem("Fruits", 300.0, "Veg", "🍎"),
        FoodItem("Vegetables", 450.0, "Veg", "🥦")
    )

    fun searchFoodItems(query: String): List<FoodItem> {
        return foodItems.filter { it.name.lowercase().contains(query.lowercase()) }
    }
}
