package com.app.carbonbuddy.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.carbonbuddy.data.WasteCategory
import com.app.carbonbuddy.data.WasteConstants
import com.app.carbonbuddy.data.DisposalMethod
import com.app.carbonbuddy.data.WasteEntry
import com.app.carbonbuddy.data.WasteStats
import com.app.carbonbuddy.repository.WasteRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

// Simple data classes for waste analysis
data class WasteAnalysisResult(
    val detectedItems: List<EditableWasteItem>,
    val totalCO2Impact: Double,
    val ecoTip: String
)

data class EditableWasteItem(
    val id: String = UUID.randomUUID().toString(),
    val wasteType: String,
    val category: WasteCategory,
    val quantity: Double,
    val unit: String = "kg",
    val disposalMethod: DisposalMethod,
    val estimatedCO2: Double,
    val description: String = "",
    val isEdited: Boolean = false
)

data class WasteManagementUiState(
    val userInput: String = "",
    val isLoading: Boolean = false,
    val editableItems: List<EditableWasteItem> = emptyList(),
    val totalCO2Impact: Double = 0.0,
    val ecoTip: String = "",
    val errorMessage: String = "",
    val showResults: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val stats: WasteStats = WasteStats()
)

class WasteManagementViewModel(private val context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WasteManagementUiState())
    val uiState: StateFlow<WasteManagementUiState> = _uiState
    
    private val repository = WasteRepository(context)

    // Gemini AI Model - same as other screens
    private val geminiModel = GenerativeModel(
        modelName = "gemini-2.5-pro",
        apiKey = "AIzaSyA83gnEatNdJbm3otgVvyNOvNqV9_I9bG8"
    )
    
    fun updateUserInput(input: String) {
        _uiState.value = _uiState.value.copy(userInput = input, errorMessage = "")
    }
    
    fun analyzeWaste() {
        if (_uiState.value.userInput.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please describe your waste first")
            return
        }
        
        viewModelScope.launch {
            try {
                // Clear previous results when starting new analysis
                _uiState.value = _uiState.value.copy(
                    isLoading = true, 
                    errorMessage = "",
                    showResults = false,
                    editableItems = emptyList(),
                    totalCO2Impact = 0.0,
                    ecoTip = ""
                )
                
                val prompt = createWasteAnalysisPrompt(_uiState.value.userInput)
                val response = geminiModel.generateContent(prompt)
                val responseText = response.text ?: ""
                
                Log.d("WasteManagement", "Analysis Response: $responseText")
                
                parseWasteAnalysisResponse(responseText)
                
            } catch (e: Exception) {
                Log.e("WasteManagement", "Error analyzing waste: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Analysis failed. Please try again.",
                    showResults = false
                )
            }
        }
    }
    

    
    private fun createWasteAnalysisPrompt(userInput: String): String {
        return """
            You are an expert waste management specialist and carbon footprint analyzer. Analyze the following waste description and calculate environmental impact.
            
            **WASTE DESCRIPTION**: $userInput
            
            **EXACT CO₂ EMISSION FACTORS (kg CO₂ per kg - USE THESE EXACT VALUES):**
            
            **ORGANIC WASTE:**
            - Food Scraps Cooked: Landfill +0.89 | Compost -0.12 | Biogas -0.45
            - Food Scraps Raw: Landfill +0.76 | Compost -0.18 | Biogas -0.52
            - Vegetable Peels: Landfill +0.45 | Compost -0.25 | Biogas -0.38
            - Fruit Peels: Landfill +0.52 | Compost -0.22 | Biogas -0.41
            - Meat Scraps: Landfill +1.23 | Compost +0.15 | Biogas -0.28
            - Dairy Waste: Landfill +1.08 | Compost +0.08 | Biogas -0.31
            - Bread Waste: Landfill +0.78 | Compost -0.15 | Biogas -0.42
            - Rice Waste: Landfill +0.82 | Compost -0.13 | Biogas -0.45
            - Yard Trimmings: Landfill +0.15 | Compost -0.27 | Mulch -0.18
            - Grass Clippings: Landfill +0.12 | Compost -0.31 | Mulch -0.22
            - Leaves: Landfill +0.18 | Compost -0.29 | Mulch -0.19
            - Wood Untreated: Landfill +0.73 | Recycle -1.85 | Compost -0.32
            
            **PAPER PRODUCTS:**
            - Newspaper: Landfill +0.38 | Recycle -2.85 | Compost -0.12
            - Office Paper: Landfill +0.89 | Recycle -3.89 | Incinerate +0.15
            - Cardboard Corrugated: Landfill +0.67 | Recycle -3.14 | Compost -0.18
            - Cardboard Mixed: Landfill +0.58 | Recycle -2.89 | Compost -0.15
            - Magazines: Landfill +0.38 | Recycle -2.39 | Incinerate +0.12
            - Books: Landfill +0.45 | Recycle -2.67 | Donate -1.89
            - Tissue Paper: Landfill +0.56 | Compost -0.22
            - Paper Towels: Landfill +0.78 | Compost -0.18
            - Paper Cups: Landfill +0.84 | Recycle -1.23 | Compost -0.15
            
            **PLASTIC MATERIALS:**
            - PET Bottles: Landfill +0.04 | Recycle -1.52 | Incinerate +2.28
            - HDPE Containers: Landfill +0.04 | Recycle -1.31 | Incinerate +2.89
            - LDPE Bags: Landfill +0.04 | Recycle -0.71 | Incinerate +2.56
            - PP Containers: Landfill +0.04 | Recycle -1.18 | Incinerate +2.67
            - Mixed Plastics: Landfill +0.04 | Recycle -1.24 | Incinerate +2.45
            - Plastic Bags Grocery: Landfill +0.04 | Recycle -0.68 | Incinerate +2.89
            - Plastic Wrap: Landfill +0.04 | Incinerate +2.23
            - Plastic Utensils: Landfill +0.04 | Recycle -0.89 | Incinerate +2.78
            
            **METAL MATERIALS:**
            - Aluminum Cans: Landfill +0.04 | Recycle -8.11 | Incinerate +0.02
            - Aluminum Foil: Landfill +0.04 | Recycle -7.85 | Incinerate +0.02
            - Steel Cans: Landfill +0.04 | Recycle -1.57 | Incinerate +0.01
            - Copper Wire: Landfill +0.04 | Recycle -3.94
            - Mixed Metals: Landfill +0.04 | Recycle -2.65
            - Stainless Steel: Landfill +0.04 | Recycle -1.89
            
            **GLASS MATERIALS:**
            - Clear Glass Bottles: Landfill +0.02 | Recycle -0.31
            - Brown Glass Bottles: Landfill +0.02 | Recycle -0.28
            - Green Glass Bottles: Landfill +0.02 | Recycle -0.26
            - Mixed Glass: Landfill +0.02 | Recycle -0.21
            - Window Glass: Landfill +0.02 | Recycle -0.18
            
            **TEXTILE WASTE:**
            - Cotton Clothing: Landfill +3.14 | Donate -4.85 | Recycle -2.67
            - Wool Clothing: Landfill +4.23 | Donate -6.78 | Recycle -3.45
            - Synthetic Clothing: Landfill +3.78 | Donate -3.21 | Recycle -1.89
            - Denim Clothing: Landfill +4.56 | Donate -5.67 | Recycle -3.12
            - Footwear: Landfill +4.12 | Donate -5.23 | Recycle -2.78
            - Carpets: Landfill +5.67 | Recycle -2.34
            
            **ELECTRONIC WASTE (per unit):**
            - Smartphones: Landfill +12.4 | Recycle -45.2 | Refurbish -89.5
            - Laptops: Landfill +42.3 | Recycle -156.8 | Refurbish -450.2
            - Tablets: Landfill +28.9 | Recycle -89.4 | Refurbish -189.7
            - Flat Screen TVs: Landfill +89.4 | Recycle -267.8 | Refurbish -445.3
            - Batteries Lithium: Landfill +8.9 | Recycle -12.4 (per kg)
            - Mobile Chargers: Landfill +3.4 | Recycle -8.9
            
            **RESPONSE FORMAT** (JSON only):
            {
                "isValid": true,
                "totalCO2Impact": 1.2,
                "detectedItems": [
                    {
                        "wasteType": "PET Bottles",
                        "category": "PLASTIC",
                        "estimatedQuantity": 0.05,
                        "unit": "kg",
                        "suggestedDisposal": "RECYCLED",
                        "estimatedCO2": -0.076,
                        "description": "500ml plastic bottle"
                    }
                ],
                "ecoTip": "Use reusable water bottles to reduce plastic waste",
                "overallAssessment": "Good recycling potential"
            }
            
            **CRITICAL INSTRUCTIONS:**
            - Use EXACT emission factors from above - NO estimates
            - Calculate: (emission_factor_per_kg * weight_kg) for total CO₂
            - Match closest item from database (e.g., "water bottle" → "PET Bottles")
            - Negative values = CO₂ saved, Positive values = CO₂ emitted
            - Return ONLY valid JSON
        """.trimIndent()
    }
    
    private fun parseWasteAnalysisResponse(responseText: String) {
        try {
            Log.d("WasteManagement", "Raw AI Response: $responseText")
            
            // Clean the response to extract JSON
            val cleanedResponse = responseText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            Log.d("WasteManagement", "Cleaned Response: $cleanedResponse")
            
            // Parse JSON response like Diet Logger
            val jsonObject = org.json.JSONObject(cleanedResponse)
            
            // Check if response is valid
            val isValid = jsonObject.optBoolean("isValid", false)
            if (!isValid) {
                throw Exception("Invalid waste description")
            }
            
            val ecoTip = jsonObject.optString("ecoTip", "Consider recycling to reduce environmental impact")
            
            // Parse detected items
            val detectedItems = mutableListOf<EditableWasteItem>()
            val itemsArray = jsonObject.optJSONArray("detectedItems")
            
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    
                    val wasteType = itemObj.optString("wasteType", "Unknown Item")
                    val categoryId = itemObj.optString("category", "OTHER")
                    val quantity = itemObj.optDouble("estimatedQuantity", 1.0)
                    val disposalId = itemObj.optString("suggestedDisposal", "LANDFILL")
                    val description = itemObj.optString("description", "")
                    
                    // Find category and disposal method
                    val category = WasteConstants.categories.find { it.id == categoryId } 
                        ?: WasteConstants.categories.first()
                    val disposal = WasteConstants.disposalMethods.find { it.id == disposalId } 
                        ?: WasteConstants.disposalMethods.first()
                    
                    // Calculate CO2 using our constants (always positive)
                    val estimatedCO2 = WasteConstants.calculateEmission(categoryId, disposalId, quantity)
                    
                    val wasteItem = EditableWasteItem(
                        wasteType = wasteType,
                        category = category,
                        quantity = quantity,
                        disposalMethod = disposal,
                        estimatedCO2 = estimatedCO2,
                        description = description
                    )
                    
                    detectedItems.add(wasteItem)
                }
            }
            
            // If no items detected, create a fallback
            if (detectedItems.isEmpty()) {
                detectedItems.add(createWasteItem("General Waste", "OTHER", 1.0, "LANDFILL"))
            }
            
            // Calculate total CO2 impact with proper logic
            val recycledItems = detectedItems.filter { it.disposalMethod.id == "RECYCLED" }
            val compostedItems = detectedItems.filter { it.disposalMethod.id == "COMPOSTED" }
            val landfillItems = detectedItems.filter { it.disposalMethod.id == "LANDFILL" || it.disposalMethod.id == "INCINERATED" }
            
            val recycledEmission = recycledItems.sumOf { it.estimatedCO2 } // Positive (saved)
            val compostedEmission = compostedItems.sumOf { it.estimatedCO2 } // Positive (saved)
            val landfillEmission = landfillItems.sumOf { it.estimatedCO2 } // Positive (emitted)
            
            // Net Impact = Emitted - Saved
            val totalCO2Impact = landfillEmission - recycledEmission - compostedEmission
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                editableItems = detectedItems,
                totalCO2Impact = totalCO2Impact,
                ecoTip = ecoTip,
                showResults = true,
                errorMessage = ""
            )
            
            Log.d("WasteManagement", "Parsing successful. Items: ${detectedItems.size}, CO2: $totalCO2Impact")
            
            // Automatically save the analyzed waste data
            saveWasteData(detectedItems)
            
        } catch (e: Exception) {
            Log.e("WasteManagement", "Error parsing response: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Analysis failed. Please try again.",
                showResults = false
            )
        }
    }
    
    private fun createWasteItem(wasteType: String, categoryId: String, quantity: Double, disposalId: String): EditableWasteItem {
        val category = WasteConstants.categories.find { it.id == categoryId } ?: WasteConstants.categories.first()
        val disposal = WasteConstants.disposalMethods.find { it.id == disposalId } ?: WasteConstants.disposalMethods.first()
        
        return EditableWasteItem(
            wasteType = wasteType,
            category = category,
            quantity = quantity,
            disposalMethod = disposal,
            estimatedCO2 = WasteConstants.calculateEmission(categoryId, disposalId, quantity),
            description = "Detected from your description"
        )
    }
    

    
    // Editing functions removed - data is automatically saved after AI analysis
    
    private fun saveWasteData(items: List<EditableWasteItem>) {
        viewModelScope.launch {
            try {
                // Group items by disposal method and calculate totals
                val recycledItems = items.filter { it.disposalMethod.id == "RECYCLED" }
                val compostedItems = items.filter { it.disposalMethod.id == "COMPOSTED" }
                val landfillItems = items.filter { it.disposalMethod.id == "LANDFILL" || it.disposalMethod.id == "INCINERATED" }
                
                val recycledWeight = recycledItems.sumOf { it.quantity }
                val recycledEmission = recycledItems.sumOf { it.estimatedCO2 } // Positive value (CO₂ saved)
                
                val compostedWeight = compostedItems.sumOf { it.quantity }
                val compostedEmission = compostedItems.sumOf { it.estimatedCO2 } // Positive value (CO₂ saved)
                
                val landfillWeight = landfillItems.sumOf { it.quantity }
                val landfillEmission = landfillItems.sumOf { it.estimatedCO2 } // Positive value (CO₂ emitted)
                
                // Save to repository
                repository.saveWasteEntry(
                    inputType = "AI", // Since it's AI analyzed
                    inputText = _uiState.value.userInput,
                    recycledWeight = recycledWeight,
                    recycledEmission = recycledEmission,
                    compostedWeight = compostedWeight,
                    compostedEmission = compostedEmission,
                    landfillWeight = landfillWeight,
                    landfillEmission = landfillEmission
                )
                
                // Load updated stats
                val updatedStats = repository.getWasteStats()
                
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = true,
                    stats = updatedStats
                )
                
                Log.d("WasteManagement", "Waste data saved successfully")
                
            } catch (e: Exception) {
                Log.e("WasteManagement", "Error saving waste entries: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save waste entries. Please try again."
                )
            }
        }
    }
    
    fun loadStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getWasteStats()
                _uiState.value = _uiState.value.copy(stats = stats)
            } catch (e: Exception) {
                Log.e("WasteManagement", "Error loading stats: ${e.message}", e)
            }
        }
    }
    
    fun dismissSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
    
    fun resetState() {
        _uiState.value = WasteManagementUiState()
    }
    
    // Call this when user navigates away from screen
    fun clearScreenData() {
        _uiState.value = WasteManagementUiState()
    }
    
    // dismissSuccessDialog removed - no dialog needed
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
