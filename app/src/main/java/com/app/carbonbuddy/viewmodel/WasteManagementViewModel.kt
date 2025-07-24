package com.app.carbonbuddy.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.carbonbuddy.data.WasteCategory
import com.app.carbonbuddy.data.WasteConstants
import com.app.carbonbuddy.data.DisposalMethod
import com.app.carbonbuddy.data.WasteEntry
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
    val showSuccessDialog: Boolean = false
)

class WasteManagementViewModel(private val context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WasteManagementUiState())
    val uiState: StateFlow<WasteManagementUiState> = _uiState

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
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
                
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
            
            **REFERENCE DATA - CO₂ Emissions by Waste Type & Disposal Method:**
            
            **WASTE CATEGORIES:**
            - PLASTIC: Bottles, bags, containers, packaging
            - PAPER: Newspapers, cardboard, documents, books
            - FOOD: Organic waste, vegetable peels, leftovers
            - GLASS: Bottles, jars, containers
            - METAL: Cans, aluminum, steel items
            - ELECTRONIC: Phones, batteries, cables
            - TEXTILE: Clothes, fabric, shoes
            - OTHER: Mixed or unspecified waste
            
            **DISPOSAL METHODS & CO₂ IMPACT:**
            - RECYCLED: Saves 0.5-2.0 kg CO₂ per kg waste
            - COMPOSTED: Saves 0.3-1.0 kg CO₂ per kg (food waste only)
            - LANDFILL: Generates 0.8-1.5 kg CO₂ per kg waste
            - INCINERATED: Generates 0.6-1.2 kg CO₂ per kg waste
            
            **TYPICAL WASTE QUANTITIES:**
            - Plastic bottle: 0.05kg
            - Paper bag: 0.02kg
            - Food waste (meal): 0.3kg
            - Glass jar: 0.2kg
            - Aluminum can: 0.015kg
            - Cardboard box: 0.1kg
            - Clothing item: 0.5kg
            
            **INSTRUCTIONS:**
            1. Extract individual waste items from description
            2. Categorize each item correctly
            3. Estimate realistic quantities
            4. Suggest best disposal method for environment
            5. Calculate CO₂ impact for each item
            6. Provide eco-friendly tip
            
            **RESPONSE FORMAT** (JSON only):
            {
                "isValid": true,
                "totalCO2Impact": 1.2,
                "detectedItems": [
                    {
                        "wasteType": "Plastic Water Bottle",
                        "category": "PLASTIC",
                        "estimatedQuantity": 0.05,
                        "unit": "kg",
                        "suggestedDisposal": "RECYCLED",
                        "estimatedCO2": -0.1,
                        "description": "500ml plastic bottle"
                    }
                ],
                "ecoTip": "Use reusable water bottles to reduce plastic waste",
                "overallAssessment": "Good recycling potential"
            }
            
            **IMPORTANT**: Return ONLY valid JSON. No extra text or explanations.
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
            
            val totalCO2Impact = jsonObject.optDouble("totalCO2Impact", 0.0)
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
                    val estimatedCO2 = itemObj.optDouble("estimatedCO2", 0.5)
                    val description = itemObj.optString("description", "")
                    
                    // Find category and disposal method
                    val category = WasteConstants.categories.find { it.id == categoryId } 
                        ?: WasteConstants.categories.first()
                    val disposal = WasteConstants.disposalMethods.find { it.id == disposalId } 
                        ?: WasteConstants.disposalMethods.first()
                    
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
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                editableItems = detectedItems,
                totalCO2Impact = totalCO2Impact,
                ecoTip = ecoTip,
                showResults = true,
                errorMessage = ""
            )
            
            Log.d("WasteManagement", "Parsing successful. Items: ${detectedItems.size}, CO2: $totalCO2Impact")
            
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
    

    
    fun updateEditableItem(itemId: String, updatedItem: EditableWasteItem) {
        val currentItems = _uiState.value.editableItems.toMutableList()
        val index = currentItems.indexOfFirst { it.id == itemId }
        
        if (index != -1) {
            currentItems[index] = updatedItem.copy(isEdited = true)
            
            // Recalculate CO2 impact
            val newCO2 = WasteConstants.calculateEmission(
                updatedItem.category.id,
                updatedItem.disposalMethod.id,
                updatedItem.quantity
            )
            currentItems[index] = currentItems[index].copy(estimatedCO2 = newCO2)
            
            val totalCO2 = currentItems.sumOf { it.estimatedCO2 }
            
            _uiState.value = _uiState.value.copy(
                editableItems = currentItems,
                totalCO2Impact = totalCO2
            )
        }
    }
    
    fun removeEditableItem(itemId: String) {
        val currentItems = _uiState.value.editableItems.filter { it.id != itemId }
        val totalCO2 = currentItems.sumOf { it.estimatedCO2 }
        
        _uiState.value = _uiState.value.copy(
            editableItems = currentItems,
            totalCO2Impact = totalCO2
        )
    }
    
    fun confirmAndSaveWaste() {
        viewModelScope.launch {
            try {
                val wasteEntries = _uiState.value.editableItems.map { item ->
                    WasteEntry(
                        id = UUID.randomUUID().toString(),
                        category = item.category,
                        disposalMethod = item.disposalMethod,
                        quantity = item.quantity,
                        unit = item.unit,
                        emission = item.estimatedCO2,
                        timestamp = System.currentTimeMillis()
                    )
                }
                
                // Here you would save to Room database
                // For now, just update the UI state
                _uiState.value = _uiState.value.copy(
                    showSuccessDialog = true
                )
                
            } catch (e: Exception) {
                Log.e("WasteManagement", "Error saving waste entries: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save waste entries. Please try again."
                )
            }
        }
    }
    
    fun resetState() {
        _uiState.value = WasteManagementUiState()
    }
    
    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
        resetState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
