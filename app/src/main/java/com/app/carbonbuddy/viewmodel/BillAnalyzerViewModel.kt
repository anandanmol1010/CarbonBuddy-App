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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

enum class BillType(val displayName: String, val icon: String) {
    ELECTRICITY("Electricity", "⚡"),
    GAS("Gas", "🔥"),
    WATER("Water", "💧"),
    INTERNET("Internet", "🌐")
}

data class GeminiAnalysisResponse(
    val billData: BillData,
    val emissionResult: EmissionResult,
    val ecoTips: List<String>,
    val detectedBillType: BillType? = null
)

data class BillData(
    val electricityUnits: Double = 0.0,
    val gasConsumption: Double = 0.0,
    val waterUsage: Double = 0.0,
    val internetData: Double = 0.0 // in GB
)

data class EmissionResult(
    val electricityEmission: Double = 0.0,
    val gasEmission: Double = 0.0,
    val waterEmission: Double = 0.0,
    val internetEmission: Double = 0.0,
    val totalEmission: Double = 0.0
)

data class BillAnalyzerUiState(
    val isLoading: Boolean = false,
    val selectedImageUri: Uri? = null,
    val extractedText: String = "",
    val billData: BillData = BillData(),
    val emissionResult: EmissionResult = EmissionResult(),
    val showManualInput: Boolean = false,
    val showBillTypeDialog: Boolean = false,
    val selectedBillType: BillType? = null,
    val errorMessage: String? = null,
    val ecoTips: List<String> = emptyList(),
    val detectedBillType: BillType? = null,
    val showMismatchWarning: Boolean = false,
    val showCalculateButton: Boolean = false,
    val isAiProcessing: Boolean = false
)

class BillAnalyzerViewModel(private val context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BillAnalyzerUiState())
    val uiState: StateFlow<BillAnalyzerUiState> = _uiState.asStateFlow()
    
    // ML Kit Text Recognizer
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // Gemini AI Model
    private val geminiModel = GenerativeModel(
        modelName = "gemini-2.5-pro",
        apiKey = "AIzaSyA83gnEatNdJbm3otgVvyNOvNqV9_I9bG8"
    )
    
    // Emission factors (kg CO₂ per unit) - for reference
    private val electricityFactor = 0.82
    private val gasFactor = 2.3
    private val waterFactor = 0.3
    private val internetFactor = 0.5 // kg CO₂ per GB
    
    fun onImageSelected(uri: Uri) {
        // Clear previous data when new image is selected
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            showBillTypeDialog = true,
            errorMessage = null,
            // Clear previous results
            extractedText = "",
            billData = BillData(),
            emissionResult = EmissionResult(),
            ecoTips = emptyList(),
            detectedBillType = null,
            showMismatchWarning = false,
            showCalculateButton = false,
            showManualInput = false
        )
    }
    
    fun onBillTypeSelected(billType: BillType) {
        _uiState.value = _uiState.value.copy(
            selectedBillType = billType,
            showBillTypeDialog = false
        )
        _uiState.value.selectedImageUri?.let { uri ->
            processImageWithOCR(uri, billType)
        }
    }
    
    fun dismissBillTypeDialog() {
        _uiState.value = _uiState.value.copy(
            showBillTypeDialog = false,
            selectedImageUri = null
        )
    }
    
    fun processImageWithOCR(uri: Uri, billType: BillType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Real OCR text extraction using ML Kit
                val extractedText = extractTextFromImage(uri)
                
                // Log extracted text for debugging
                Log.d("Anmol Anand", "=== REAL OCR EXTRACTED TEXT ===")
                Log.d("Anmol Anand", "Selected Bill Type: ${billType.displayName}")
                Log.d("Anmol Anand", "Extracted Text: $extractedText")
                Log.d("Anmol Anand", "Text Length: ${extractedText.length} characters")
                Log.d("Anmol Anand", "====================================")
                
                // Show extracted text and Calculate button (don't auto-process with AI)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    extractedText = extractedText,
                    selectedBillType = billType,
                    showCalculateButton = true,
                    errorMessage = null
                )
                
            } catch (e: Exception) {
                Log.e("Anmol Anand", "OCR Error: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to extract text: ${e.message}. Please try manual input.",
                    showManualInput = true
                )
            }
        }
    }
    
    private suspend fun extractTextFromImage(uri: Uri): String {
        return try {
            // Convert URI to Bitmap
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            
            // Create InputImage from bitmap
            val image = InputImage.fromBitmap(bitmap, 0)
            
            // Process image with ML Kit Text Recognition
            val result = textRecognizer.process(image).await()
            
            // Extract all text
            val extractedText = result.text
            
            Log.d("Anmol Anand", "ML Kit processing successful")
            Log.d("Anmol Anand", "Raw extracted text: $extractedText")
            
            extractedText
            
        } catch (e: Exception) {
            Log.e("Anmol Anand", "Error in ML Kit text extraction: ${e.message}", e)
            throw e
        }
    }
    
    private fun parseExtractedText(text: String, billType: BillType): BillData {
        val electricityRegex = Regex("""(?:Units Consumed|Electricity Used|kWh Used):\s*(\d+(?:\.\d+)?)\s*kWh""", RegexOption.IGNORE_CASE)
        val gasRegex = Regex("""(?:Gas Consumption|Gas Used|Consumption):\s*(\d+(?:\.\d+)?)\s*L""", RegexOption.IGNORE_CASE)
        val waterRegex = Regex("""(?:Water Used|Water Usage|Water Consumption):\s*(\d+(?:\.\d+)?)\s*L""", RegexOption.IGNORE_CASE)
        val internetRegex = Regex("""(?:Data Used|Data Consumption|Usage):\s*(\d+(?:\.\d+)?)\s*GB""", RegexOption.IGNORE_CASE)
        
        return when (billType) {
            BillType.ELECTRICITY -> {
                val match = electricityRegex.find(text)
                BillData(electricityUnits = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0)
            }
            BillType.GAS -> {
                val match = gasRegex.find(text)
                BillData(gasConsumption = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0)
            }
            BillType.WATER -> {
                val match = waterRegex.find(text)
                BillData(waterUsage = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0)
            }
            BillType.INTERNET -> {
                val match = internetRegex.find(text)
                BillData(internetData = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0)
            }
        }
    }
    
    private fun calculateEmissions(billData: BillData): EmissionResult {
        val electricityEmission = billData.electricityUnits * electricityFactor
        val gasEmission = billData.gasConsumption * gasFactor
        val waterEmission = billData.waterUsage * waterFactor
        val internetEmission = billData.internetData * internetFactor
        val totalEmission = electricityEmission + gasEmission + waterEmission + internetEmission
        
        return EmissionResult(
            electricityEmission = electricityEmission,
            gasEmission = gasEmission,
            waterEmission = waterEmission,
            internetEmission = internetEmission,
            totalEmission = totalEmission
        )
    }
    
    private fun generateEcoTips(result: EmissionResult, billType: BillType? = null): List<String> {
        val tips = mutableListOf<String>()
        
        if (result.electricityEmission > 50) {
            tips.add("💡 Try switching to LED bulbs and reduce A/C usage")
            tips.add("🔌 Unplug devices when not in use to save energy")
        }
        
        if (result.gasEmission > 100) {
            tips.add("🔥 Consider using energy-efficient cooking methods")
            tips.add("🏠 Improve home insulation to reduce heating needs")
        }
        
        if (result.waterEmission > 30) {
            tips.add("💧 Install water-saving taps and showerheads")
            tips.add("🚿 Take shorter showers and fix any leaks")
        }
        
        if (result.internetEmission > 100) {
            tips.add("🌐 Use Wi-Fi instead of mobile data when possible")
            tips.add("📱 Stream videos in lower quality to reduce data usage")
            tips.add("☁️ Consider green hosting providers for your online activities")
        }
        
        if (result.totalEmission > 200) {
            tips.add("🌱 Consider renewable energy sources like solar panels")
            tips.add("♻️ Adopt more sustainable lifestyle practices")
        }
        
        // Add specific tips based on bill type
        billType?.let { type ->
            when (type) {
                BillType.ELECTRICITY -> tips.add("⚡ Consider time-of-use pricing to save on electricity")
                BillType.GAS -> tips.add("🔥 Regular maintenance of gas appliances improves efficiency")
                BillType.WATER -> tips.add("💧 Collect rainwater for gardening to reduce usage")
                BillType.INTERNET -> tips.add("🌐 Use ad blockers to reduce data consumption")
            }
        }
        
        if (tips.isEmpty()) {
            tips.add("🎉 Great job! Your utility usage is eco-friendly")
        }
        
        return tips
    }
    
    private fun buildManualInputText(billData: BillData): String {
        return "Electricity: ${billData.electricityUnits} kWh\n" +
                "Gas: ${billData.gasConsumption} L\n" +
                "Water: ${billData.waterUsage} L\n" +
                "Internet: ${billData.internetData} GB"
    }
    
    // New function to calculate with AI when button is clicked
    fun calculateWithAI() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiProcessing = true, errorMessage = null)
            
            try {
                val currentState = _uiState.value
                
                // Determine input type and data
                val (inputText, selectedType) = if (currentState.extractedText.isNotEmpty()) {
                    // OCR mode - use extracted text
                    Log.d("Anmol Anand", "=== AI CALCULATION (OCR MODE) ===")
                    currentState.extractedText to currentState.selectedBillType
                } else {
                    // Manual input mode - use manual data
                    Log.d("Anmol Anand", "=== AI CALCULATION (MANUAL MODE) ===")
                    buildManualInputText(currentState.billData) to null
                }
                
                Log.d("Anmol Anand", "Input Data: $inputText")
                Log.d("Anmol Anand", "Selected Type: ${selectedType?.displayName}")
                
                // Use Gemini AI to analyze and calculate emissions
                val geminiResponse = analyzeWithGemini(inputText, selectedType)
                
                // Handle mismatch for OCR mode
                val showMismatch = selectedType != null && 
                                 geminiResponse.detectedBillType != null && 
                                 geminiResponse.detectedBillType != selectedType
                
                if (showMismatch) {
                    Log.d("Anmol Anand", "=== MISMATCH DETECTED ===")
                    Log.d("Anmol Anand", "Selected: ${selectedType?.displayName}")
                    Log.d("Anmol Anand", "Detected: ${geminiResponse.detectedBillType?.displayName}")
                    Log.d("Anmol Anand", "=========================")
                }
                
                _uiState.value = _uiState.value.copy(
                    isAiProcessing = false,
                    billData = geminiResponse.billData,
                    emissionResult = geminiResponse.emissionResult,
                    ecoTips = geminiResponse.ecoTips,
                    detectedBillType = geminiResponse.detectedBillType,
                    showMismatchWarning = showMismatch,
                    showCalculateButton = false // Hide button after calculation
                )
                
            } catch (e: Exception) {
                Log.e("Anmol Anand", "AI Calculation Error: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isAiProcessing = false,
                    errorMessage = "AI calculation failed: ${e.message}. Please try again."
                )
            }
        }
    }
    
    fun processManualInput() {
        // Show calculate button for manual input
        _uiState.value = _uiState.value.copy(
            showCalculateButton = true,
            errorMessage = null
        )
    }
    
    fun updateManualInput(billData: BillData) {
        // Just update the bill data, don't auto-calculate
        _uiState.value = _uiState.value.copy(
            billData = billData
        )
    }
    
    fun toggleManualInput() {
        val currentState = _uiState.value
        if (currentState.showManualInput) {
            // Reset state when hiding manual input
            _uiState.value = BillAnalyzerUiState()
        } else {
            // If opening manual input
            _uiState.value = _uiState.value.copy(
                showManualInput = true,
                showCalculateButton = true // Show button immediately
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun resetState() {
        _uiState.value = BillAnalyzerUiState()
    }
    
    // Gemini AI Analysis Function
    private suspend fun analyzeWithGemini(inputText: String, selectedBillType: BillType?): GeminiAnalysisResponse {
        try {
            val prompt = createGeminiPrompt(inputText, selectedBillType)
            
            Log.d("Anmol Anand", "=== GEMINI AI ANALYSIS ===")
            Log.d("Anmol Anand", "Prompt: $prompt")
            
            val response = geminiModel.generateContent(prompt)
            val aiResponse = response.text ?: throw Exception("Empty AI response")
            
            Log.d("Anmol Anand", "AI Response: $aiResponse")
            Log.d("Anmol Anand", "=========================")
            
            return parseGeminiResponse(aiResponse)
            
        } catch (e: Exception) {
            Log.e("Anmol Anand", "Gemini AI Error: ${e.message}", e)
            throw e
        }
    }
    
    private fun createGeminiPrompt(inputText: String, selectedBillType: BillType?): String {
        // Detect if this is manual input (structured format) or OCR text
        val isManualInput = inputText.contains("Electricity:") && inputText.contains("Gas:") && 
                           inputText.contains("Water:") && inputText.contains("Internet:")
        
        return """
            You are an expert utility bill analyzer and carbon footprint calculator with knowledge of standard emission factors.
            
            TASK: ${if (isManualInput) "Calculate CO₂ emissions from provided consumption values and provide eco-tips" else "Analyze utility bill data, validate category selection, and calculate accurate CO₂ emissions"}.
            
            INPUT DATA TYPE: ${if (isManualInput) "MANUAL INPUT (Structured consumption values)" else "OCR EXTRACTED TEXT (Raw bill content)"}
            $inputText
            
            ${if (selectedBillType != null) "USER SELECTED CATEGORY: ${selectedBillType.displayName}" else "NO CATEGORY SELECTED"}
            
            INSTRUCTIONS:
            ${if (isManualInput) """
            1. Use the provided consumption values directly (they are already extracted)
            2. Determine the primary utility type based on highest consumption
            3. Calculate CO₂ emissions using standard emission factors
            4. Provide 5 SHORT, actionable eco-friendly tips (max 8-10 words each, bullet points style)
            5. Set detectedBillType to the utility with highest consumption (or null if all are zero)
            """ else """
            1. Analyze the input data to detect the actual bill type from OCR text
            2. ${if (selectedBillType != null) "Compare detected type with user-selected category (${selectedBillType.displayName})" else "Determine the most likely bill type"}
            3. Extract consumption values for each utility type found in the bill text
            4. Use standard emission factors to calculate CO₂ emissions
            5. Provide 5 SHORT, actionable eco-friendly tips (max 8-10 words each, bullet points style)
            6. ${if (selectedBillType != null) "If detected type differs from selected category, set categoryMismatch to true" else ""}
            """}
            
            EMISSION FACTORS TO USE:
            - Electricity: ~0.8-0.9 kg CO₂/kWh (use appropriate regional factor)
            - Gas: ~2.0-2.5 kg CO₂/litre (LPG/cooking gas)
            - Water: ~0.3-0.4 kg CO₂/litre (including treatment)
            - Internet: ~0.4-0.6 kg CO₂/GB (data transmission)
            
            IMPORTANT: Respond with ONLY valid JSON, no markdown formatting, no code blocks, no extra text.
            
            Required JSON format:
            {
                "detectedBillType": "ELECTRICITY|GAS|WATER|INTERNET|null",
                "categoryMismatch": false,
                "electricityUnits": 0.0,
                "gasConsumption": 0.0,
                "waterUsage": 0.0,
                "internetData": 0.0,
                "electricityEmission": 0.0,
                "gasEmission": 0.0,
                "waterEmission": 0.0,
                "internetEmission": 0.0,
                "totalEmission": 0.0,
                "ecoTips": [
                    "Tip 1 based on usage",
                    "Tip 2 for reducing emissions",
                    "Tip 3 personalized advice"
                ]
            }
        """.trimIndent()
    }
    
    private fun parseGeminiResponse(response: String): GeminiAnalysisResponse {
        return try {
            // Clean the response - remove markdown code blocks if present
            val cleanedResponse = response
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            Log.d("Anmol Anand", "Cleaned JSON: $cleanedResponse")
            
            // Parse JSON response from Gemini
            val jsonResponse = org.json.JSONObject(cleanedResponse)
            
            val detectedTypeString = jsonResponse.optString("detectedBillType", "null")
            val detectedBillType = when (detectedTypeString) {
                "ELECTRICITY" -> BillType.ELECTRICITY
                "GAS" -> BillType.GAS
                "WATER" -> BillType.WATER
                "INTERNET" -> BillType.INTERNET
                else -> null
            }
            
            val categoryMismatch = jsonResponse.optBoolean("categoryMismatch", false)
            
            val billData = BillData(
                electricityUnits = jsonResponse.optDouble("electricityUnits", 0.0),
                gasConsumption = jsonResponse.optDouble("gasConsumption", 0.0),
                waterUsage = jsonResponse.optDouble("waterUsage", 0.0),
                internetData = jsonResponse.optDouble("internetData", 0.0)
            )
            
            val emissionResult = EmissionResult(
                electricityEmission = jsonResponse.optDouble("electricityEmission", 0.0),
                gasEmission = jsonResponse.optDouble("gasEmission", 0.0),
                waterEmission = jsonResponse.optDouble("waterEmission", 0.0),
                internetEmission = jsonResponse.optDouble("internetEmission", 0.0),
                totalEmission = jsonResponse.optDouble("totalEmission", 0.0)
            )
            
            val ecoTipsArray = jsonResponse.optJSONArray("ecoTips")
            val ecoTips = mutableListOf<String>()
            if (ecoTipsArray != null) {
                for (i in 0 until ecoTipsArray.length()) {
                    ecoTips.add(ecoTipsArray.getString(i))
                }
            }
            
            Log.d("Anmol Anand", "=== PARSED GEMINI RESPONSE ===")
            Log.d("Anmol Anand", "Detected Type: ${detectedBillType?.displayName}")
            Log.d("Anmol Anand", "Category Mismatch (AI): $categoryMismatch")
            Log.d("Anmol Anand", "Total Emission: ${emissionResult.totalEmission} kg CO₂")
            Log.d("Anmol Anand", "Eco Tips: ${ecoTips.size} tips")
            Log.d("Anmol Anand", "==============================")
            
            GeminiAnalysisResponse(
                billData = billData,
                emissionResult = emissionResult,
                ecoTips = ecoTips,
                detectedBillType = detectedBillType
            )
            
        } catch (e: Exception) {
            Log.e("Anmol Anand", "Error parsing Gemini response: ${e.message}", e)
            Log.e("Anmol Anand", "Raw response: $response")
            throw Exception("Failed to parse AI response: ${e.message}")
        }
    }
    
    private fun detectBillTypeFromText(text: String): BillType? {
        val lowerText = text.lowercase()
        
        // Count keyword matches for each type to find the most specific match
        var electricityScore = 0
        var gasScore = 0
        var waterScore = 0
        var internetScore = 0
        
        // Electricity keywords (more specific first)
        if (lowerText.contains("electricity bill")) electricityScore += 3
        if (lowerText.contains("kwh")) electricityScore += 3
        if (lowerText.contains("kvh")) electricityScore += 3
        if (lowerText.contains("units consumed")) electricityScore += 3
        if (lowerText.contains("electricity")) electricityScore += 2
        if (lowerText.contains("power")) electricityScore += 1
        if (lowerText.contains("watt")) electricityScore += 2
        
        // Gas keywords
        if (lowerText.contains("gas bill")) gasScore += 3
        if (lowerText.contains("lpg")) gasScore += 3
        if (lowerText.contains("cooking gas")) gasScore += 3
        if (lowerText.contains("cylinder")) gasScore += 2
        if (lowerText.contains("gas")) gasScore += 1
        
        // Water keywords
        if (lowerText.contains("water bill")) waterScore += 3
        if (lowerText.contains("water supply")) waterScore += 3
        if (lowerText.contains("municipal")) waterScore += 2
        if (lowerText.contains("sewage")) waterScore += 2
        if (lowerText.contains("water")) waterScore += 1
        
        // Internet keywords
        if (lowerText.contains("internet bill")) internetScore += 3
        if (lowerText.contains("broadband")) internetScore += 3
        if (lowerText.contains("data used")) internetScore += 3
        if (lowerText.contains("mbps")) internetScore += 2
        if (lowerText.contains("wifi")) internetScore += 2
        if (lowerText.contains("internet")) internetScore += 2
        if (lowerText.contains("gb") && (lowerText.contains("data") || lowerText.contains("usage"))) internetScore += 2
        
        // Log scoring details
        Log.d("Anmol Anand", "=== DETECTION SCORING ===")
        Log.d("Anmol Anand", "Text to analyze: $lowerText")
        Log.d("Anmol Anand", "Electricity Score: $electricityScore")
        Log.d("Anmol Anand", "Gas Score: $gasScore")
        Log.d("Anmol Anand", "Water Score: $waterScore")
        Log.d("Anmol Anand", "Internet Score: $internetScore")
        
        // Return the type with highest score (minimum threshold of 2)
        val maxScore = maxOf(electricityScore, gasScore, waterScore, internetScore)
        
        Log.d("Anmol Anand", "Max Score: $maxScore")
        Log.d("Anmol Anand", "=========================")
        
        return when {
            maxScore < 2 -> {
                Log.d("Anmol Anand", "Detection Result: Not confident enough (score < 2)")
                null // Not confident enough
            }
            electricityScore == maxScore -> {
                Log.d("Anmol Anand", "Detection Result: ELECTRICITY")
                BillType.ELECTRICITY
            }
            gasScore == maxScore -> {
                Log.d("Anmol Anand", "Detection Result: GAS")
                BillType.GAS
            }
            waterScore == maxScore -> {
                Log.d("Anmol Anand", "Detection Result: WATER")
                BillType.WATER
            }
            internetScore == maxScore -> {
                Log.d("Anmol Anand", "Detection Result: INTERNET")
                BillType.INTERNET
            }
            else -> {
                Log.d("Anmol Anand", "Detection Result: Unknown error")
                null
            }
        }
    }
    
    fun changeBillType() {
        _uiState.value = _uiState.value.copy(
            showBillTypeDialog = true,
            showMismatchWarning = false
        )
    }
    
    fun dismissMismatchWarning() {
        _uiState.value = _uiState.value.copy(
            showMismatchWarning = false
        )
    }
    
    fun correctBillType() {
        val detectedType = _uiState.value.detectedBillType
        if (detectedType != null) {
            // Just update the bill type and hide mismatch warning
            // Don't re-process, just show existing results with correct type
            _uiState.value = _uiState.value.copy(
                selectedBillType = detectedType,
                showMismatchWarning = false
            )
            Log.d("Anmol Anand", "Bill type corrected to: ${detectedType.displayName}")
        }
    }
    
    fun handleManualInputClick() {
        val currentState = _uiState.value
        if (currentState.showManualInput) {
            // If already in manual mode, reset everything and start fresh
            Log.d("Anmol Anand", "Resetting manual input for fresh start")
            _uiState.value = BillAnalyzerUiState(
                showManualInput = true,
                showCalculateButton = true
            )
        } else {
            // If not in manual mode, clear previous data and enter manual mode
            Log.d("Anmol Anand", "Entering manual input mode")
            _uiState.value = BillAnalyzerUiState(
                showManualInput = true,
                showCalculateButton = true
            )
        }
    }
}
