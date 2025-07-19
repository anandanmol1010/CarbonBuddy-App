package com.app.carbonbuddy.data

import androidx.compose.ui.graphics.Color

data class WasteCategory(
    val id: String,
    val name: String,
    val icon: String,
    val color: Int
)

data class DisposalMethod(
    val id: String,
    val name: String,
    val icon: String,
    val description: String
)

data class WasteEntry(
    val id: String = "",
    val category: WasteCategory,
    val disposalMethod: DisposalMethod,
    val quantity: Double,
    val unit: String,
    val emission: Double,
    val timestamp: Long = System.currentTimeMillis()
)

object WasteConstants {
    val categories = listOf(
        WasteCategory("PLASTIC", "Plastic", "🗑️", Color(0xFFE91E63).value.toInt()),
        WasteCategory("PAPER", "Paper", "📄", Color(0xFF4CAF50).value.toInt()),
        WasteCategory("FOOD", "Food", "🍽️", Color(0xFFF44336).value.toInt()),
        WasteCategory("GLASS", "Glass", "💎", Color(0xFF03A9F4).value.toInt()),
        WasteCategory("METAL", "Metal", "🔧", Color(0xFF9C27B0).value.toInt()),
        WasteCategory("ELECTRONIC", "Electronic", "📱", Color(0xFF673AB7).value.toInt()),
        WasteCategory("TEXTILE", "Textile", "👕", Color(0xFFFF9800).value.toInt()),
        WasteCategory("OTHER", "Other", "❓", Color(0xFF795548).value.toInt())
    )

    val disposalMethods = listOf(
        DisposalMethod("RECYCLED", "Recycled", "♻️", "Recycling reduces emissions"),
        DisposalMethod("LANDFILL", "Landfill", "🗑️", "Highest emissions"),
        DisposalMethod("COMPOSTED", "Composted", "🌱", "Low emissions for organic waste"),
        DisposalMethod("INCINERATED", "Incinerated", "🔥", "Medium emissions")
    )

    val emissionFactors = mapOf(
        // Plastic
        "PLASTIC_RECYCLED" to 0.6,
        "PLASTIC_LANDFILL" to 2.5,
        "PLASTIC_COMPOSTED" to 0.0,
        "PLASTIC_INCINERATED" to 1.8,
        // Paper
        "PAPER_RECYCLED" to 0.4,
        "PAPER_LANDFILL" to 1.8,
        "PAPER_COMPOSTED" to 0.3,
        "PAPER_INCINERATED" to 1.2,
        // Food
        "FOOD_RECYCLED" to 0.0,
        "FOOD_LANDFILL" to 1.5,
        "FOOD_COMPOSTED" to 0.3,
        "FOOD_INCINERATED" to 0.8,
        // Glass
        "GLASS_RECYCLED" to 0.2,
        "GLASS_LANDFILL" to 1.0,
        "GLASS_COMPOSTED" to 0.0,
        "GLASS_INCINERATED" to 0.5,
        // Metal
        "METAL_RECYCLED" to 0.3,
        "METAL_LANDFILL" to 2.0,
        "METAL_COMPOSTED" to 0.0,
        "METAL_INCINERATED" to 1.5,
        // Electronic
        "ELECTRONIC_RECYCLED" to 0.5,
        "ELECTRONIC_LANDFILL" to 3.0,
        "ELECTRONIC_COMPOSTED" to 0.0,
        "ELECTRONIC_INCINERATED" to 2.5,
        // Textile
        "TEXTILE_RECYCLED" to 0.4,
        "TEXTILE_LANDFILL" to 1.8,
        "TEXTILE_COMPOSTED" to 0.0,
        "TEXTILE_INCINERATED" to 1.2,
        // Other
        "OTHER_RECYCLED" to 0.5,
        "OTHER_LANDFILL" to 2.0,
        "OTHER_COMPOSTED" to 0.0,
        "OTHER_INCINERATED" to 1.5
    )

    fun calculateEmission(category: String, disposal: String, quantity: Double): Double {
        return emissionFactors["${category}_${disposal}"]?.let { factor ->
            factor * quantity
        } ?: 0.0
    }
}
