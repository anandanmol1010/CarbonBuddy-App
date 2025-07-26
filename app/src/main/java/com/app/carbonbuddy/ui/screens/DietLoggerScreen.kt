package com.app.carbonbuddy.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.carbonbuddy.viewmodel.DietLoggerViewModel
import com.app.carbonbuddy.viewmodel.DietAnalysisResult
import com.app.carbonbuddy.viewmodel.DietItem
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietLoggerScreen() {
    val context = LocalContext.current
    val viewModel: DietLoggerViewModel = viewModel { DietLoggerViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Success Message Toast
    if (uiState.showSuccessMessage) {
        Toast.makeText(context, "Meal data saved successfully!", Toast.LENGTH_SHORT).show()
        viewModel.dismissSuccessMessage()
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9), // Light green
                        Color(0xFFF1F8E9), // Very light green
                        Color(0xFFE0F2F1)  // Light teal
                    )
                )
            )
            .padding(16.dp)
            .padding(bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "🍽️ Diet Logger",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF388E3C),
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
            )
        }
        
        // Meal Type Selection
        item {
            MealTypeSelector(
                selectedMealType = uiState.selectedMealType,
                onMealTypeSelected = viewModel::selectMealType
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Meal Description Input - Hide during processing and after analysis
        if (!uiState.isAnalyzing && uiState.analysisResult == null) {
            item {
                MealDescriptionInput(
                    mealDescription = uiState.mealDescription,
                    onDescriptionChange = viewModel::updateMealDescription,
                    isAnalyzing = uiState.isAnalyzing,
                    onAnalyzeClick = viewModel::analyzeMeal,
                    onClearClick = viewModel::resetMeal
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Processing Indicator
        if (uiState.isAnalyzing) {
            item {
                ProcessingIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Error Message
        uiState.errorMessage?.let { error ->
            item {
                ErrorCard(error = error, onDismiss = viewModel::clearError)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // High Emission Warning
        if (uiState.showHighEmissionWarning) {
            item {
                HighEmissionWarning()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Analysis Results
        uiState.analysisResult?.let { result ->
            item {
                AnalysisResultCard(result = result)
            }
        }
    }
}

@Composable
fun MealTypeSelector(
    selectedMealType: String,
    onMealTypeSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Select Meal Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Breakfast", "Lunch").forEach { mealType ->
                Button(
                    onClick = { onMealTypeSelected(mealType) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMealType == mealType) Color(0xFF4CAF50) else Color(0xFFF1F8E9),
                        contentColor = if (selectedMealType == mealType) Color.White else Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text(mealType)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Dinner", "Snacks").forEach { mealType ->
                Button(
                    onClick = { onMealTypeSelected(mealType) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMealType == mealType) Color(0xFF4CAF50) else Color(0xFFF1F8E9),
                        contentColor = if (selectedMealType == mealType) Color.White else Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text(mealType)
                }
            }
        }
    }
}

@Composable
fun MealDescriptionInput(
    mealDescription: String,
    onDescriptionChange: (String) -> Unit,
    isAnalyzing: Boolean,
    onAnalyzeClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Describe Your Meal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = mealDescription,
                onValueChange = onDescriptionChange,
                label = { Text("What did you eat?") },
                placeholder = {
                    Text(
                        "Example:\n• I had dal, rice, curd, one boiled egg, and a glass of milk\n• Chicken biryani with raita and salad\n• Toast with butter, scrambled eggs, and orange juice",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8,
                leadingIcon = {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                },
                enabled = !isAnalyzing
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Analyze Button
                Button(
                    onClick = onAnalyzeClick,
                    enabled = !isAnalyzing && mealDescription.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing...")
                    } else {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Analyze",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Meal")
                    }
                }
                
                // Clear Button
                OutlinedButton(
                    onClick = onClearClick,
                    enabled = !isAnalyzing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
fun ProcessingIndicator() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF9C27B0),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "🧠 Analyzing your meal...",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                error,
                color = Color(0xFFD32F2F),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
fun HighEmissionWarning() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "High Emission Warning",
                tint = Color(0xFFE65100),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "⚠️ High-emission meal (>3kg CO₂). Consider plant-based alternatives like tofu, lentils, or vegetables.",
                color = Color(0xFFE65100),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AnalysisResultCard(result: DietAnalysisResult) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Total Emission
            Text(
                "🌍 Carbon Footprint Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Total Emission Display
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.totalEmissionGrams > 3000) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Emission",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        "${String.format("%.0f", result.totalEmissionGrams)} g CO₂",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.totalEmissionGrams > 3000) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Item Breakdown
            Text(
                "Per-Item Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            result.items.forEach { item ->
                DietItemRow(item = item)
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Suggestions
            if (result.suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SuggestionsCard(suggestions = result.suggestions)
            }
        }
    }
}

@Composable
fun DietItemRow(item: DietItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            item.icon,
            fontSize = 20.sp,
            modifier = Modifier.width(32.dp)
        )
        
        Text(
            item.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            "${String.format("%.0f", item.emissionGrams)}g CO₂",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
    }
}

@Composable
fun SuggestionsCard(suggestions: List<String>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "🌱 Eco-Friendly Suggestions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        "• ",
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
