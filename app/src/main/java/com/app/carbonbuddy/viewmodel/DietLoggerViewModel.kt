package com.app.carbonbuddy.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class DietItem(
    val name: String,
    val emissionGrams: Double,
    val icon: String = "🍽️"
)

data class DietAnalysisResult(
    val totalEmissionGrams: Double,
    val items: List<DietItem>,
    val suggestions: List<String> = emptyList()
)

data class DietLoggerUiState(
    val selectedMealType: String = "Breakfast",
    val mealDescription: String = "",
    val isAnalyzing: Boolean = false,
    val analysisResult: DietAnalysisResult? = null,
    val errorMessage: String? = null,
    val showHighEmissionWarning: Boolean = false
)

class DietLoggerViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(DietLoggerUiState())
    val uiState: StateFlow<DietLoggerUiState> = _uiState.asStateFlow()
    
    private val geminiModel = GenerativeModel(
        modelName = "gemini-2.5-pro",
        apiKey = "AIzaSyA83gnEatNdJbm3otgVvyNOvNqV9_I9bG8" // Replace with your actual API key
    )
    
    fun selectMealType(mealType: String) {
        _uiState.value = _uiState.value.copy(
            selectedMealType = mealType,
            mealDescription = "", // Clear input field
            analysisResult = null,
            errorMessage = null,
            showHighEmissionWarning = false,
            isAnalyzing = false // Reset analyzing state
        )
    }
    
    fun updateMealDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            mealDescription = description,
            analysisResult = null,
            errorMessage = null,
            showHighEmissionWarning = false
        )
    }
    
    fun analyzeMeal() {
        val currentState = _uiState.value
        if (currentState.mealDescription.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please describe what you ate"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(
                    isAnalyzing = true,
                    errorMessage = null
                )
                
                Log.d("Diet Logger", "Analyzing meal: ${currentState.mealDescription}")
                
                val prompt = createGeminiPrompt(
                    currentState.mealDescription,
                    currentState.selectedMealType
                )
                
                Log.d("Diet Logger", "Gemini prompt: $prompt")
                
                val response = geminiModel.generateContent(prompt)
                val responseText = response.text ?: throw Exception("Empty response from AI")
                
                Log.d("Diet Logger", "Gemini response: $responseText")
                
                val analysisResult = parseGeminiResponse(responseText)
                val showWarning = analysisResult.totalEmissionGrams > 3000
                
                _uiState.value = currentState.copy(
                    isAnalyzing = false,
                    analysisResult = analysisResult,
                    showHighEmissionWarning = showWarning
                )
                
                Log.d("Diet Logger", "Analysis complete. Total emission: ${analysisResult.totalEmissionGrams}g CO₂")
                
            } catch (e: Exception) {
                Log.e("Diet Logger", "Error analyzing meal: ${e.message}", e)
                _uiState.value = currentState.copy(
                    isAnalyzing = false,
                    errorMessage = "Failed to analyze meal: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetMeal() {
        _uiState.value = _uiState.value.copy(
            mealDescription = "",
            analysisResult = null,
            errorMessage = null,
            showHighEmissionWarning = false
        )
    }
    
    private fun createGeminiPrompt(mealDescription: String, mealType: String): String {
        return """
            You are an expert nutritionist and carbon footprint analyzer. Analyze the following meal description and calculate carbon emissions.
            
            **MEAL TYPE**: $mealType
            **MEAL DESCRIPTION**: $mealDescription
            
            **REFERENCE DATA - Average CO₂ Emissions per Meal Type (1 Serving):**
            
            **FOOD CATEGORIES:**
            - Red Meat (Beef, Mutton, Lamb): 4000-6500g CO₂
            - White Meat (Chicken, Fish): 1500-2500g CO₂  
            - Dairy Products (Milk, Cheese, Paneer): 1000-1800g CO₂
            - Vegetarian (Dal, Rice, Vegetables): 500-1200g CO₂
            - Vegan (Tofu, Plant-based): 200-800g CO₂
            
            **SPECIFIC FOOD ITEMS (per serving):**
            - Rice (1 cup): 1200g
            - Dal (1 cup): 600g
            - Chicken (100g): 3200g
            - Beef (100g): 3500g
            - Fish (100g): 2800g
            - Egg (1 piece): 2000g
            - Milk (1 glass): 1800g
            - Paneer (100g): 2400g
            - Vegetables (1 cup): 450g
            - Fruits (1 piece): 300g
            - Bread/Roti (1 piece): 700g
            - Tofu (100g): 800g
            - Yogurt (1 cup): 1900g
            - Cheese (50g): 1300g
            
            **INSTRUCTIONS:**
            1. Extract individual food items from the description
            2. Estimate portion sizes based on typical servings
            3. Calculate CO₂ emissions for each item using reference data
            4. Provide total emissions and itemized breakdown
            5. If total > 3000g, suggest plant-based alternatives
            
            **RESPONSE FORMAT** (JSON only):
            {
                "isValid": true,
                "totalEmissionGrams": 2450.0,
                "items": [
                    {
                        "name": "Rice (1 cup)",
                        "emissionGrams": 1200.0,
                        "icon": "🍚"
                    },
                    {
                        "name": "Dal (1 cup)",
                        "emissionGrams": 600.0,
                        "icon": "🍲"
                    },
                    {
                        "name": "Egg (1 piece)",
                        "emissionGrams": 2000.0,
                        "icon": "🍳"
                    }
                ],
                "suggestions": [
                    "Replace egg with tofu for lower emissions",
                    "Add more vegetables to your meal",
                    "Consider plant-based protein sources"
                ]
            }
            
            **CRITICAL INSTRUCTIONS:**
            - Use exact emission values from reference data
            - Extract only actual food items, ignore cooking methods
            - Estimate realistic portion sizes
            - Return ONLY valid JSON
            - If meal description is unclear, return: {"isValid": false, "error": "Please provide clearer meal description"}
        """.trimIndent()
    }
    
    private fun parseGeminiResponse(response: String): DietAnalysisResult {
        try {
            Log.d("Diet Logger", "Raw Gemini response: $response")
            
            // Handle markdown code block
            val trimmedResponse = response.trim()
            val cleanedResponse = if (trimmedResponse.startsWith("```json") && trimmedResponse.endsWith("```")) {
                Log.d("Diet Logger", "Response is a markdown code block, removing block indicators.")
                trimmedResponse.substring(7, trimmedResponse.length - 3).trim()
            } else {
                trimmedResponse
            }
            
            val jsonResponse = JSONObject(cleanedResponse)
            
            // Check if input is valid
            if (!jsonResponse.optBoolean("isValid", true)) {
                val error = jsonResponse.optString("error", "Invalid meal description")
                throw Exception(error)
            }
            
            val totalEmissionGrams = jsonResponse.getDouble("totalEmissionGrams")
            val itemsArray = jsonResponse.getJSONArray("items")
            val suggestionsArray = jsonResponse.optJSONArray("suggestions")
            
            val items = mutableListOf<DietItem>()
            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                items.add(
                    DietItem(
                        name = item.getString("name"),
                        emissionGrams = item.getDouble("emissionGrams"),
                        icon = item.optString("icon", "🍽️")
                    )
                )
            }
            
            val suggestions = mutableListOf<String>()
            suggestionsArray?.let { array ->
                for (i in 0 until array.length()) {
                    suggestions.add(array.getString(i))
                }
            }
            
            Log.d("Diet Logger", "Parsed successfully: ${totalEmissionGrams}g CO₂, ${items.size} items")
            
            return DietAnalysisResult(
                totalEmissionGrams = totalEmissionGrams,
                items = items,
                suggestions = suggestions
            )
            
        } catch (e: Exception) {
            Log.e("Diet Logger", "Failed to parse Gemini response: ${e.message}")
            throw Exception("Failed to analyze meal data: ${e.message}")
        }
    }
}
