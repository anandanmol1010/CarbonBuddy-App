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
                netImpact = landfillEmission - recycledEmission - compostedEmission, // Net = Emitted - Saved
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
