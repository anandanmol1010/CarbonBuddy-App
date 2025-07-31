package com.app.carbonbuddy.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.carbonbuddy.data.DietStats
import com.app.carbonbuddy.repository.DietRepository
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
    val showHighEmissionWarning: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val stats: DietStats = DietStats()
)

class DietLoggerViewModel(context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DietLoggerUiState())
    val uiState: StateFlow<DietLoggerUiState> = _uiState.asStateFlow()
    
    private val repository = DietRepository(context)
    
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
                
                // Save to database
                repository.saveDietEntry(
                    mealType = currentState.selectedMealType,
                    mealDescription = currentState.mealDescription,
                    totalEmissionGrams = analysisResult.totalEmissionGrams,
                    items = analysisResult.items,
                    suggestions = analysisResult.suggestions
                )
                
                // Load updated stats
                val updatedStats = repository.getDietStats()
                
                _uiState.value = currentState.copy(
                    isAnalyzing = false,
                    analysisResult = analysisResult,
                    showHighEmissionWarning = showWarning,
                    showSuccessMessage = true,
                    stats = updatedStats
                )
                
                Log.d("Diet Logger", "Analysis complete and saved. Total emission: ${analysisResult.totalEmissionGrams}g CO₂")
                
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
            showHighEmissionWarning = false,
            showSuccessMessage = false
        )
    }
    
    fun loadStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getDietStats()
                _uiState.value = _uiState.value.copy(stats = stats)
            } catch (e: Exception) {
                Log.e("Diet Logger", "Error loading stats: ${e.message}", e)
            }
        }
    }
    
    fun dismissSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
    
    private fun createGeminiPrompt(mealDescription: String, mealType: String): String {
        return """
            You are an expert nutritionist and carbon footprint analyzer. Analyze the following meal description and calculate carbon emissions.
            
            **MEAL TYPE**: $mealType
            **MEAL DESCRIPTION**: $mealDescription
            
            **EXACT CO₂ EMISSION FACTORS (g CO₂ per kg - USE THESE EXACT VALUES):**
            
            **GRAINS & CEREALS:**
            - Basmati Rice: 4000 | Brown Rice: 4200 | White Rice: 4000 | Wheat Flour: 1400 | Maida: 1300
            - Bread White: 1600 | Bread Brown: 1700 | Chapati: 1500 | Naan: 2100 | Paratha: 2800
            - Oats: 2500 | Quinoa: 1800 | Barley: 1200 | Millet: 800 | Pasta: 1700 | Noodles: 1900
            - Poha: 4200 | Upma Rava: 1400
            
            **PULSES & LEGUMES:**
            - Toor Dal: 900 | Moong Dal: 900 | Chana Dal: 900 | Masoor Dal: 900 | Urad Dal: 900
            - Kidney Beans: 2800 | Chickpeas: 1300 | Green Peas: 1200 | Soya Beans: 1200 | Lentils: 900
            
            **VEGETABLES:**
            - Potato: 500 | Sweet Potato: 600 | Onion: 500 | Tomato: 2100 | Carrot: 400 | Cabbage: 500
            - Cauliflower: 700 | Broccoli: 4200 | Spinach: 2000 | Cucumber: 1100 | Bell Pepper: 2800
            - Eggplant: 1200 | Okra: 1100 | Pumpkin: 600 | Mushroom: 3300 | Green Beans: 2000
            - Ginger: 800 | Green Chili: 1500 | Coriander: 1200
            
            **FRUITS:**
            - Banana: 900 | Apple: 600 | Orange: 400 | Mango: 1100 | Papaya: 800 | Pineapple: 1200
            - Watermelon: 300 | Grapes: 2400 | Pomegranate: 1800 | Guava: 900 | Coconut: 1300
            - Lemon: 500 | Strawberry: 1400 | Avocado: 2800
            
            **DAIRY PRODUCTS:**
            - Whole Milk: 3200/L | Toned Milk: 3100/L | Buffalo Milk: 3800/L | Paneer: 8500
            - Curd/Yogurt: 2200 | Butter: 23900 | Ghee: 27300 | Cheddar Cheese: 21000 | Cream: 12400/L
            
            **MEAT, POULTRY & SEAFOOD:**
            - Chicken Broiler: 6900 | Country Chicken: 8200 | Mutton: 39200 | Goat Meat: 35800
            - Beef: 60000 | Buffalo Meat: 52300 | Pork: 7600 | Duck: 9800 | Fish Rohu: 5800
            - Prawns Farmed: 18000 | Chicken Eggs: 4200 | Duck Eggs: 5100
            
            **PROCESSED FOODS:**
            - Biscuits Plain: 3400 | Chocolate Dark: 18700 | Cake: 4200 | Pizza: 3600 | Burger: 12800
            - French Fries: 2900 | Potato Chips: 4500 | Instant Noodles: 3800 | Honey: 3200
            - Pickles: 1800 | Papad: 2300 | Namkeen: 3500
            
            **BEVERAGES:**
            - Black Tea: 9800 | Green Tea: 10200 | Coffee Arabica: 16500 | Instant Coffee: 12300
            - Cola: 700/L | Fruit Juice Apple: 1200/L | Mango Juice: 1300/L | Beer: 740/L | Wine: 1280/L
            
            **TYPICAL SERVING SIZES:**
            - Rice: 150g (1 cup cooked) | Dal: 100g (1 cup) | Roti/Chapati: 30g (1 piece)
            - Vegetables: 100g (1 cup) | Fruits: 150g (1 medium) | Milk: 250ml (1 glass)
            - Chicken: 100g | Fish: 100g | Egg: 50g (1 piece) | Paneer: 50g
            
            **RESPONSE FORMAT** (JSON only):
            {
                "isValid": true,
                "totalEmissionGrams": 2450.0,
                "items": [
                    {
                        "name": "Basmati Rice (150g)",
                        "emissionGrams": 600.0,
                        "icon": "🍚"
                    },
                    {
                        "name": "Toor Dal (100g)",
                        "emissionGrams": 90.0,
                        "icon": "🍲"
                    }
                ],
                "suggestions": [
                    "Replace rice with millet for lower emissions",
                    "Add more vegetables to your meal",
                    "Consider plant-based protein sources"
                ]
            }
            
            **CRITICAL INSTRUCTIONS:**
            - **FIRST PRIORITY**: Use EXACT emission factors from above database if item found
            - **IF EXACT MATCH FOUND**: Use the exact value (e.g., "white rice" → "White Rice: 4000")
            - **IF NO EXACT MATCH**: Use the database as REFERENCE and estimate based on similar items:
              * Similar grains → use average of grain values
              * Similar vegetables → use average of vegetable values
              * Similar proteins → use average of protein values
              * Similar processed foods → use average of processed food values
            - **CALCULATION**: (emission_factor_per_kg * serving_weight_kg) * 1000 for grams
            - **EXAMPLES OF ESTIMATION**:
              * "Jowar" (not in database) → use Millet: 800 (similar grain)
              * "Rajma" (not in database) → use Kidney Beans: 2800 (same item)
              * "Palak" (not in database) → use Spinach: 2000 (same item)
              * "Aloo Gobi" → Potato: 500 + Cauliflower: 700 (combination)
            - Extract only actual food items, ignore cooking methods
            - Return ONLY valid JSON
            - If completely unclear, return: {"isValid": false, "error": "Please provide clearer meal description"}
        """.trimIndent()
    }
    
    private fun parseGeminiResponse(response: String): DietAnalysisResult {
        try {
            Log.d("Diet Logger", "Raw Gemini response: $response")
            
            // Handle markdown code block2
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
