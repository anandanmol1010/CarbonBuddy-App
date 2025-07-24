package com.app.carbonbuddy.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

data class ShoppingItem(
    val name: String,
    val category: String,
    val co2Emission: Double,
    val icon: String = "🛍️"
)

data class ShoppingEstimatorUiState(
    val isLoading: Boolean = false,
    val isOcrProcessing: Boolean = false,
    val isAiProcessing: Boolean = false,
    val selectedImageUri: Uri? = null,
    val extractedText: String = "",
    val manualInput: String = "",
    val showManualInput: Boolean = false,
    val showCalculateButton: Boolean = false,
    val totalEmission: Double = 0.0,
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val ecoTips: List<String> = emptyList(),
    val errorMessage: String? = null
)

class ShoppingEstimatorViewModel(private val context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ShoppingEstimatorUiState())
    val uiState: StateFlow<ShoppingEstimatorUiState> = _uiState.asStateFlow()
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // TODO: Replace with secure API key management
    private val geminiModel = GenerativeModel(
        modelName = "gemini-2.0-flash-exp",
        apiKey = "AIzaSyA83gnEatNdJbm3otgVvyNOvNqV9_I9bG8" // Replace with your actual API key
    )
    
    fun onImageSelected(uri: Uri) {
        Log.d("Shopping Estimator", "Image selected: $uri")
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            errorMessage = null,
            // Clear previous results
            extractedText = "",
            totalEmission = 0.0,
            shoppingItems = emptyList(),
            ecoTips = emptyList(),
            showCalculateButton = false,
            showManualInput = false
        )
        processReceiptWithOCR(uri)
    }
    
    private fun processReceiptWithOCR(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isOcrProcessing = true,
                    errorMessage = null
                )
                
                Log.d("Shopping Estimator", "Starting OCR processing...")
                
                val extractedText = extractTextFromImage(uri)
                
                Log.d("Shopping Estimator", "OCR completed. Extracted text length: ${extractedText.length}")
                Log.d("Shopping Estimator", "Extracted text: $extractedText")
                
                // Let AI validate the receipt - no manual validation
                _uiState.value = _uiState.value.copy(
                    isOcrProcessing = false,
                    extractedText = extractedText,
                    showCalculateButton = extractedText.isNotEmpty()
                )
                
            } catch (e: Exception) {
                Log.e("Shopping Estimator", "OCR processing failed: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isOcrProcessing = false,
                    errorMessage = "Failed to extract text from image: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun extractTextFromImage(uri: Uri): String {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            Log.e("Shopping Estimator", "Error extracting text from image: ${e.message}", e)
            throw e
        }
    }
    
    fun toggleManualInput() {
        val currentState = _uiState.value
        if (currentState.showManualInput) {
            // Reset state when hiding manual input
            _uiState.value = ShoppingEstimatorUiState()
        } else {
            _uiState.value = _uiState.value.copy(
                showManualInput = true,
                showCalculateButton = true,
                // Clear OCR data when switching to manual
                selectedImageUri = null,
                extractedText = "",
                errorMessage = null
            )
        }
    }
    
    fun updateManualInput(input: String) {
        _uiState.value = _uiState.value.copy(
            manualInput = input,
            showCalculateButton = input.isNotBlank()
        )
    }
    
    fun calculateEmissions() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isAiProcessing = true,
                    errorMessage = null
                )
                
                val inputText = if (_uiState.value.showManualInput) {
                    _uiState.value.manualInput
                } else {
                    _uiState.value.extractedText
                }
                
                // Let Gemini AI validate the input - no manual validation needed
                
                Log.d("Shopping Estimator", "Sending to Gemini AI: $inputText")
                
                val prompt = createGeminiPrompt(inputText, _uiState.value.showManualInput)
                Log.d("Shopping Estimator", "Gemini prompt: $prompt")
                
                val response = geminiModel.generateContent(prompt)
                val responseText = response.text ?: throw Exception("Empty response from AI")
                
                Log.d("Shopping Estimator", "Gemini response: $responseText")
                
                val analysisResult = parseGeminiResponse(responseText)
                
                _uiState.value = _uiState.value.copy(
                    isAiProcessing = false,
                    totalEmission = analysisResult.totalEmission,
                    shoppingItems = analysisResult.items,
                    ecoTips = analysisResult.ecoTips,
                    showCalculateButton = false
                )
                
                Log.d("Shopping Estimator", "Analysis complete. Total emission: ${analysisResult.totalEmission} kg CO₂")
                
            } catch (e: Exception) {
                Log.e("Shopping Estimator", "Error calculating emissions: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isAiProcessing = false,
                    errorMessage = "Failed to calculate emissions: ${e.message}"
                )
            }
        }
    }
    
    fun resetState() {
        _uiState.value = ShoppingEstimatorUiState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun handleManualInputClick() {
        val currentState = _uiState.value
        if (currentState.showManualInput) {
            // If already in manual mode, reset everything and start fresh
            Log.d("Shopping Estimator", "Resetting manual input for fresh start")
            _uiState.value = ShoppingEstimatorUiState(
                showManualInput = true,
                showCalculateButton = true
            )
        } else {
            // If not in manual mode, clear previous data and enter manual mode
            Log.d("Shopping Estimator", "Entering manual input mode")
            _uiState.value = ShoppingEstimatorUiState(
                showManualInput = true,
                showCalculateButton = true
            )
        }
    }
    
    // Receipt validation removed - now handled by Gemini AI
    
    // Manual validation removed - now handled by Gemini AI
    
    // Gemini AI Integration
    // private val geminiModel = GenerativeModel(
    //     modelName = "gemini-2.0-flash-exp",
    //     apiKey = "AIzaSyDGJJQMVXaGMjKJfOJKNOJKNOJKNOJKNOJ" // Replace with your actual API key
    // )
    
    private fun createGeminiPrompt(inputText: String, isManualInput: Boolean): String {
        return """
            You are an expert shopping carbon footprint analyzer. Analyze ONLY actual products purchased.
            
            **INPUT TYPE**: ${if (isManualInput) "Manual user input" else "OCR extracted from receipt"}
            **INPUT TEXT**: $inputText
            
            **CRITICAL RULES**:
            1. **ONLY INCLUDE ACTUAL PRODUCTS** - Ignore tax, shipping, discounts, store names, dates, payment methods
            2. **NO ESTIMATES** - Use exact emission factors provided below
            3. **VALIDATE INPUT** - Must contain real shopping items/products
            4. **REJECT NON-SHOPPING** - Utility bills, medical records, random text, greetings
            
            **EXACT EMISSION FACTORS** (kg CO₂ per item - USE THESE EXACT VALUES):
            
            **CLOTHING:**
            - T-shirt/Top: 5.5
            - Jeans/Pants: 33.4
            - Shoes/Footwear: 14.0
            - Jacket/Coat: 25.0
            - Dress: 15.8
            - Shirt: 6.2
            - Sweater: 18.5
            - Underwear: 2.1
            - Socks: 1.8
            
            **ELECTRONICS:**
            - Smartphone: 85.0
            - Laptop: 350.0
            - Tablet: 130.0
            - Headphones: 15.0
            - Charger: 8.0
            - TV: 500.0
            - Watch: 25.0
            - Camera: 120.0
            
            **HOME & FURNITURE:**
            - Bed: 180.0
            - Pillow: 12.0
            - Bedsheet: 8.5
            - Blanket: 15.0
            - Chair: 45.0
            - Table: 85.0
            - Lamp: 18.0
            - Curtains: 22.0
            - Towel: 6.8
            
            **FOOD (per kg):**
            - Beef: 27.0
            - Chicken: 6.9
            - Fish: 5.4
            - Rice: 2.7
            - Vegetables: 2.0
            - Fruits: 1.1
            - Milk: 3.2
            - Bread: 0.9
            - Cheese: 13.5
            
            **OTHER:**
            - Books: 2.5
            - Toys: 8.0
            - Cosmetics: 4.0
            - Cleaning products: 3.0
            - Bags: 12.0
            
            **RESPONSE FORMAT** (JSON only):
            {
                "isValid": true,
                "totalEmission": 45.2,
                "items": [
                    {
                        "name": "Blue T-shirt",
                        "category": "Clothing",
                        "co2Emission": 5.5,
                        "icon": "👕"
                    }
                ],
                "ecoTips": [
                    "Choose organic cotton clothing",
                    "Buy local products",
                    "Consider second-hand items",
                    "Look for energy-efficient electronics",
                    "Reduce meat consumption"
                ]
            }
            
            **CRITICAL INSTRUCTIONS**:
            - Use EXACT emission factors from above - NO estimates or approximations
            - Skip tax, shipping, discounts, store info, payment details
            - If no actual products found, return: {"isValid": false, "error": "No shopping products detected"}
            - Return ONLY valid JSON
            - Keep eco tips short (4-6 words each)
        """.trimIndent()
    }
    
    private fun parseGeminiResponse(response: String): GeminiAnalysisResult {
        try {
            Log.d("Shopping Estimator", "Raw Gemini response: $response")
            
            // Check for markdown code block
            val trimmedResponse = response.trim()
            Log.d("Shopping Estimator", "Trimmed response: $trimmedResponse")
            val cleanedResponse = if (trimmedResponse.startsWith("```json") && trimmedResponse.endsWith("```")) {
                Log.d("Shopping Estimator", "Response is a markdown code block, removing block indicators.")
                trimmedResponse.substring(7, trimmedResponse.length - 3).trim()
            } else {
                trimmedResponse
            }
            
            val jsonResponse = JSONObject(cleanedResponse)
            return parseJsonResponse(jsonResponse)
        } catch (e: Exception) {
            Log.e("Shopping Estimator", "Failed to parse Gemini response: ${e.message}")
            throw Exception("Failed to analyze shopping data: ${e.message}")
        }
    }
    
    private fun parseJsonResponse(jsonResponse: JSONObject): GeminiAnalysisResult {
        // Check if input is valid
        if (!jsonResponse.optBoolean("isValid", true)) {
            val error = jsonResponse.optString("error", "Invalid shopping input")
            throw Exception(error)
        }
        
        val totalEmission = jsonResponse.getDouble("totalEmission")
        val itemsArray = jsonResponse.getJSONArray("items")
        val tipsArray = jsonResponse.getJSONArray("ecoTips")
        
        val items = mutableListOf<ShoppingItem>()
        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            items.add(
                ShoppingItem(
                    name = item.getString("name"),
                    category = item.getString("category"),
                    co2Emission = item.getDouble("co2Emission"),
                    icon = item.optString("icon", "🛍️")
                )
            )
        }
        
        val ecoTips = mutableListOf<String>()
        for (i in 0 until tipsArray.length()) {
            ecoTips.add(tipsArray.getString(i))
        }
        
        Log.d("Shopping Estimator", "Parsed successfully: $totalEmission kg CO₂, ${items.size} items")
        
        return GeminiAnalysisResult(
            totalEmission = totalEmission,
            items = items,
            ecoTips = ecoTips
        )
    }
    
    data class GeminiAnalysisResult(
        val totalEmission: Double,
        val items: List<ShoppingItem>,
        val ecoTips: List<String>
    )
}
