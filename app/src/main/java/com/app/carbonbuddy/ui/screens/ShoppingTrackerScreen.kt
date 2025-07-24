package com.app.carbonbuddy.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.carbonbuddy.viewmodel.ShoppingEstimatorViewModel
import com.app.carbonbuddy.viewmodel.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingTrackerScreen() {
    val context = LocalContext.current
    val viewModel: ShoppingEstimatorViewModel = viewModel { ShoppingEstimatorViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFB3E5FC))
                )
            )
            .padding(16.dp)
            .padding(bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header
            Text(
                "🛍️ Smart Shopping Carbon Estimator",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            Text(
                "Upload receipt or enter items manually for carbon footprint analysis",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        item {
            // Upload Section
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.selectedImageUri != null) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = "Selected receipt",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF1976D2)
                            )
                            Text(
                                "Tap to upload receipt image",
                                color = Color(0xFF1976D2),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Upload buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                viewModel.resetState()
                                imagePickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery")
                        }
                        
                        OutlinedButton(
                            onClick = { viewModel.handleManualInputClick() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manual")
                        }
                    }
                    
                    // Analyze button for uploaded image (show after OCR completes successfully)
                    if (uiState.selectedImageUri != null && uiState.extractedText.isNotEmpty() && uiState.showCalculateButton && !uiState.isOcrProcessing && !uiState.isAiProcessing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Button removed as per request
                        // Button(
                        //     onClick = { viewModel.calculateEmissions() },
                        //     modifier = Modifier.fillMaxWidth(),
                        //     colors = ButtonDefaults.buttonColors(
                        //         containerColor = Color(0xFF1976D2)
                        //     ),
                        //     shape = RoundedCornerShape(12.dp)
                        // ) {
                        //     Icon(
                        //         Icons.Default.Calculate,
                        //         contentDescription = "Calculate",
                        //         modifier = Modifier.size(20.dp)
                        //     )
                        //     Spacer(modifier = Modifier.width(8.dp))
                        //     Text(
                        //         "Analyze Carbon Footprint",
                        //         fontSize = 16.sp,
                        //         fontWeight = FontWeight.Bold
                        //     )
                        // }
                    }
                }
            }
        }
        
        // Processing indicator
        if (uiState.isOcrProcessing) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                            "📄 Extracting text from receipt...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Processing indicator
        if (uiState.isAiProcessing) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                            "Analyzing your shopping...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Calculating carbon footprint and generating eco-tips",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Calculate button for OCR
        if (uiState.extractedText.isNotEmpty() && uiState.showCalculateButton && !uiState.isAiProcessing) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "📊 Ready to Analyze",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Receipt text extracted successfully!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.calculateEmissions() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = "Calculate",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Analyze Carbon Footprint",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Manual input section
        if (uiState.showManualInput && !uiState.isAiProcessing && uiState.totalEmission == 0.0) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ManualShoppingInputSection(
                    manualInput = uiState.manualInput,
                    onInputChange = { viewModel.updateManualInput(it) },
                    showCalculateButton = uiState.showCalculateButton,
                    onCalculateClick = { viewModel.calculateEmissions() }
                )
            }
        }
        
        // Results section
        if (uiState.totalEmission > 0) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ShoppingEmissionResultCard(
                    totalEmission = uiState.totalEmission,
                    items = uiState.shoppingItems
                )
            }
        }
        
        // Eco tips section
        if (uiState.ecoTips.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ShoppingEcoTipsCard(uiState.ecoTips)
            }
        }
        
        // Error handling
        uiState.errorMessage?.let { error ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManualShoppingInputSection(
    manualInput: String,
    onInputChange: (String) -> Unit,
    showCalculateButton: Boolean = false,
    onCalculateClick: () -> Unit = {}
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
                "✏️ Manual Input",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF388E3C)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = manualInput,
                onValueChange = onInputChange,
                label = { Text("Enter your shopping items") },
                placeholder = { 
                    Text(
                        "Example:\n• T-shirt\n• iPhone 15\n• 2x Jeans\n• Coffee beans\n\nOr write in any format you prefer!",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8,
                leadingIcon = {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                }
            )
            
            // Analyze button for manual input
            if (showCalculateButton) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onCalculateClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = "Calculate",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Analyze Carbon Footprint",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingEmissionResultCard(
    totalEmission: Double,
    items: List<ShoppingItem>
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
                "🌍 Carbon Footprint Analysis",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Total Emission: ${String.format("%.1f", totalEmission)} kg CO₂",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            items.forEach { item ->
                Text(
                    "${item.icon} ${item.name}: ${item.co2Emission} kg CO₂",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ShoppingEcoTipsCard(ecoTips: List<String>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "🌱 Eco-Friendly Shopping Tips",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF388E3C),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ecoTips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        "• ",
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        tip,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(item: ShoppingItem) {
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
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                item.category,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        Text(
            "${String.format("%.1f", item.co2Emission)} kg CO₂",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
    }
}