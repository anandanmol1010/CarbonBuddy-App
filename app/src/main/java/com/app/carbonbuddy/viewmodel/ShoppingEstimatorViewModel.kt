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
import com.app.carbonbuddy.data.ShoppingStats
import com.app.carbonbuddy.repository.ShoppingRepository
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
    val errorMessage: String? = null,
    val showSuccessMessage: Boolean = false,
    val stats: ShoppingStats = ShoppingStats()
)

class ShoppingEstimatorViewModel(private val context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ShoppingEstimatorUiState())
    val uiState: StateFlow<ShoppingEstimatorUiState> = _uiState.asStateFlow()
    
    private val repository = ShoppingRepository(context)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    private val geminiModel = GenerativeModel(
        modelName = "gemini-2.5-pro",
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
            Log.d("Shopping Estimator", "Starting text extraction from URI: $uri")
            
            // Check if file exists and has content
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null || inputStream.available() == 0) {
                inputStream?.close()
                throw Exception("Image file is empty or cannot be read")
            }
            inputStream.close()
            
            val bitmap = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                Log.e("Shopping Estimator", "Failed to decode image: ${e.message}")
                // Try alternative method
                val inputStream2 = context.contentResolver.openInputStream(uri)
                android.graphics.BitmapFactory.decodeStream(inputStream2).also {
                    inputStream2?.close()
                    if (it == null) throw Exception("Could not decode image from any method")
                }
            }
            
            Log.d("Shopping Estimator", "Bitmap created successfully: ${bitmap.width}x${bitmap.height}")
            
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(image).await()
            
            Log.d("Shopping Estimator", "Text extraction completed. Text length: ${result.text.length}")
            result.text
        } catch (e: Exception) {
            Log.e("Shopping Estimator", "Error extracting text from image: ${e.message}", e)
            throw Exception("Failed to extract text from image: ${e.message}")
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
            
            // Save to database
            val inputType = if (_uiState.value.showManualInput) "Manual" else "OCR"
            repository.saveShoppingEntry(
                inputType = inputType,
                inputText = inputText,
                totalEmission = analysisResult.totalEmission,
                items = analysisResult.items,
                ecoTips = analysisResult.ecoTips
            )
            
            // Load updated stats
            val updatedStats = repository.getShoppingStats()
            
            _uiState.value = _uiState.value.copy(
                isAiProcessing = false,
                totalEmission = analysisResult.totalEmission,
                shoppingItems = analysisResult.items,
                ecoTips = analysisResult.ecoTips,
                showCalculateButton = false,
                showSuccessMessage = true,
                stats = updatedStats
            )
            
            Log.d("Shopping Estimator", "Analysis complete and saved. Total emission: ${analysisResult.totalEmission} kg CO₂")
                
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
    
    fun loadStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getShoppingStats()
                _uiState.value = _uiState.value.copy(stats = stats)
            } catch (e: Exception) {
                Log.e("Shopping Estimator", "Error loading stats: ${e.message}", e)
            }
        }
    }
    
    fun dismissSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
    
    // Receipt validation removed - now handled by Gemini AI
    
    // Manual validation removed - now handled by Gemini AI
    
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
            
            **CLOTHING & TEXTILES:**
            - Cotton T-shirt: 8.5 | Polyester T-shirt: 5.5 | Cotton Shirt: 12.3 | Silk Shirt: 18.7
            - Denim Jeans: 33.4 | Cotton Trousers: 15.8 | Cotton Dress: 22.5
            - Wool Sweater: 45.6 | Cotton Sweater: 28.3 | Leather Jacket: 89.5 | Denim Jacket: 42.8
            - Silk Saree: 45.8 | Cotton Saree: 18.3 | Cotton Kurta: 14.2 | Silk Kurta: 28.5
            - Cotton Salwar: 11.8 | Cotton Dupatta: 6.5 | Cotton Underwear: 2.1 | Cotton Bra: 3.8
            - Cotton Socks: 1.9 | Wool Socks: 4.2 | Cotton Pajamas: 8.9 | Silk Nightwear: 18.5
            
            **FOOTWEAR:**
            - Leather Formal Shoes: 19.2 | Canvas Sneakers: 14.0 | Leather Sneakers: 16.8
            - Running Shoes: 18.3 | Leather Sandals: 12.5 | Rubber Flip-flops: 3.8
            - Leather Boots: 28.4 | High Heels: 22.1 | Leather Flats: 15.3 | Sports Shoes: 19.8
            - Crocs: 5.2 | Home Slippers: 2.1
            
            **ELECTRONICS & APPLIANCES:**
            - Basic Smartphone: 89.5 | Premium Smartphone: 156.8 | 7" Tablet: 145.2 | 10" Tablet: 189.7
            - Basic Laptop: 450.2 | Gaming Laptop: 789.5 | Desktop PC: 623.8
            - 24" Monitor: 234.5 | 32" Monitor: 356.7 | 32" LED TV: 320.5 | 43" LED TV: 445.3
            - 55" LED TV: 612.8 | 55" OLED TV: 798.4 | Single Door Fridge: 892.3 | Double Door Fridge: 1245.7
            - Semi-Auto Washing Machine: 456.2 | Auto Washing Machine: 542.3 | Microwave: 234.8
            - 1 Ton AC: 678.9 | 1.5 Ton AC: 823.4 | Ceiling Fan: 45.6 | Table Fan: 28.3
            - Mixer Grinder: 67.8 | Electric Kettle: 23.4 | Rice Cooker: 34.5 | Iron: 18.7
            - Hair Dryer: 12.8 | Vacuum Cleaner: 89.4 | Electric Razor: 23.4
            
            **PERSONAL CARE:**
            - Shampoo 250ml: 0.85 | Shampoo 500ml: 1.42 | Conditioner 250ml: 0.92 | Body Wash 250ml: 0.78
            - Soap Bar: 0.42 | Face Wash 100ml: 0.65 | Moisturizer 100ml: 0.89 | Sunscreen 100ml: 1.12
            - Toothpaste 100g: 0.73 | Toothbrush: 0.18 | Mouthwash 250ml: 0.54
            - Deodorant Spray: 1.25 | Deodorant Roll: 0.98 | Perfume 50ml: 2.34 | Cologne 100ml: 3.45
            - Lipstick: 0.67 | Foundation: 1.23 | Mascara: 0.89 | Eyeliner: 0.45 | Nail Polish: 0.34
            - Shaving Cream: 0.76 | Disposable Razor: 0.12
            
            **HOUSEHOLD ITEMS:**
            - Detergent Powder 1kg: 2.1 | Detergent Liquid 1L: 2.3 | Fabric Softener 1L: 1.8
            - Dish Soap 500ml: 0.89 | Floor Cleaner 1L: 1.45 | Toilet Cleaner 500ml: 1.23
            - Glass Cleaner 500ml: 0.98 | Air Freshener: 1.56 | Toilet Paper 4-roll: 3.8
            - Tissue Paper 100-sheet: 1.2 | Kitchen Towel: 0.95 | Aluminum Foil: 2.3 | Cling Wrap: 1.8
            - Garbage Bags 30pc: 1.2 | Plastic Container Set: 4.5 | Glass Container Set: 6.8
            - Steel Utensils Set: 12.3 | Non-stick Pan: 8.9 | Pressure Cooker 3L: 15.4
            - Dinner Plates Set: 7.6 | Cotton Bed Sheets: 12.8 | Cotton Pillow: 4.5
            - Cotton Blanket: 18.7 | Cotton Curtains: 15.3
            
            **RESPONSE FORMAT** (JSON only):
            {
                "isValid": true,
                "totalEmission": 45.2,
                "items": [
                    {
                        "name": "Cotton T-shirt",
                        "category": "Clothing",
                        "co2Emission": 8.5,
                        "icon": "👕"
                    }
                ],
                "ecoTips": [
                    "Choose organic cotton clothing",
                    "Buy local products",
                    "Consider second-hand items",
                    "Look for energy-efficient electronics",
                    "Reduce plastic packaging"
                ]
            }
            
            **CRITICAL INSTRUCTIONS**:
            - Use EXACT emission factors from above - NO estimates or approximations
            - Match closest item from database (e.g., "blue t-shirt" → "Cotton T-shirt: 8.5")
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
