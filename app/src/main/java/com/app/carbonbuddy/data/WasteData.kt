package com.app.carbonbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "waste_entries")
data class WasteEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val inputType: String, // "AI" or "Manual"
    val inputText: String, // Original input text
    val recycledWeight: Double,
    val recycledEmission: Double, // Negative for savings
    val compostedWeight: Double,
    val compostedEmission: Double, // Negative for savings
    val landfillWeight: Double,
    val landfillEmission: Double, // Positive for emissions
    val netImpact: Double, // Sum of all three emissions
    val dateString: String,
    val dayOfMonth: Int,
    val month: Int,
    val year: Int,
    val weekOfYear: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(
            inputType: String,
            inputText: String,
            recycledWeight: Double,
            recycledEmission: Double,
            compostedWeight: Double,
            compostedEmission: Double,
            landfillWeight: Double,
            landfillEmission: Double
        ): WasteEntry {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            return WasteEntry(
                inputType = inputType,
                inputText = inputText,
                recycledWeight = recycledWeight,
                recycledEmission = recycledEmission,
                compostedWeight = compostedWeight,
                compostedEmission = compostedEmission,
                landfillWeight = landfillWeight,
                landfillEmission = landfillEmission,
                netImpact = landfillEmission + recycledEmission + compostedEmission, // Net = Sum of all (recycled/composted are already negative)
                dateString = dateFormat.format(calendar.time),
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                month = calendar.get(Calendar.MONTH) + 1,
                year = calendar.get(Calendar.YEAR),
                weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
            )
        }
    }
}

data class WasteStats(
    val todayNetImpact: Double = 0.0,
    val weeklyNetImpact: Double = 0.0,
    val monthlyNetImpact: Double = 0.0,
    val todayCount: Int = 0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    // Individual category breakdown
    val monthlyRecycledWeight: Double = 0.0,
    val monthlyRecycledEmission: Double = 0.0,
    val monthlyCompostedWeight: Double = 0.0,
    val monthlyCompostedEmission: Double = 0.0,
    val monthlyLandfillWeight: Double = 0.0,
    val monthlyLandfillEmission: Double = 0.0
)

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
        // Plastic - Recycling saves CO₂ (negative), Landfill emits CO₂ (positive)
        "PLASTIC_RECYCLED" to -0.6,   // CO₂ SAVED
        "PLASTIC_LANDFILL" to 2.5,    // CO₂ EMITTED
        "PLASTIC_COMPOSTED" to -0.2,  // CO₂ SAVED (small amount)
        "PLASTIC_INCINERATED" to 1.8, // CO₂ EMITTED
        // Paper
        "PAPER_RECYCLED" to -0.8,     // CO₂ SAVED
        "PAPER_LANDFILL" to 1.8,      // CO₂ EMITTED
        "PAPER_COMPOSTED" to -0.3,    // CO₂ SAVED
        "PAPER_INCINERATED" to 1.2,   // CO₂ EMITTED
        // Food
        "FOOD_RECYCLED" to 0.0,       // Not applicable
        "FOOD_LANDFILL" to 1.5,       // CO₂ EMITTED
        "FOOD_COMPOSTED" to -0.4,     // CO₂ SAVED
        "FOOD_INCINERATED" to 0.8,    // CO₂ EMITTED
        // Glass
        "GLASS_RECYCLED" to -0.3,     // CO₂ SAVED
        "GLASS_LANDFILL" to 1.0,      // CO₂ EMITTED
        "GLASS_COMPOSTED" to 0.0,     // Not applicable
        "GLASS_INCINERATED" to 0.5,   // CO₂ EMITTED
        // Metal
        "METAL_RECYCLED" to -1.2,     // CO₂ SAVED (high savings)
        "METAL_LANDFILL" to 2.0,      // CO₂ EMITTED
        "METAL_COMPOSTED" to 0.0,     // Not applicable
        "METAL_INCINERATED" to 1.5,   // CO₂ EMITTED
        // Electronic
        "ELECTRONIC_RECYCLED" to -1.5, // CO₂ SAVED (high savings)
        "ELECTRONIC_LANDFILL" to 3.0,  // CO₂ EMITTED (high emissions)
        "ELECTRONIC_COMPOSTED" to 0.0, // Not applicable
        "ELECTRONIC_INCINERATED" to 2.5, // CO₂ EMITTED
        // Textile
        "TEXTILE_RECYCLED" to -0.5,   // CO₂ SAVED
        "TEXTILE_LANDFILL" to 1.8,    // CO₂ EMITTED
        "TEXTILE_COMPOSTED" to -0.2,  // CO₂ SAVED (small amount)
        "TEXTILE_INCINERATED" to 1.2, // CO₂ EMITTED
        // Other
        "OTHER_RECYCLED" to -0.4,     // CO₂ SAVED
        "OTHER_LANDFILL" to 2.0,      // CO₂ EMITTED
        "OTHER_COMPOSTED" to -0.2,    // CO₂ SAVED
        "OTHER_INCINERATED" to 1.5    // CO₂ EMITTED
    )

    fun calculateEmission(category: String, disposal: String, quantity: Double): Double {
        return emissionFactors["${category}_${disposal}"]?.let { factor ->
            factor * quantity
        } ?: 0.0
    }
}
