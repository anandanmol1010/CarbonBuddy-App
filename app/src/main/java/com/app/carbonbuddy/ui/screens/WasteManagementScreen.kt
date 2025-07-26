package com.app.carbonbuddy.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.app.carbonbuddy.data.WasteConstants
import com.app.carbonbuddy.viewmodel.EditableWasteItem
import com.app.carbonbuddy.viewmodel.WasteManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteManagementScreen(viewModel: WasteManagementViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Show success toast when data is saved
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            Toast.makeText(context, "Waste data saved successfully!", Toast.LENGTH_SHORT).show()
            viewModel.dismissSuccessMessage()
        }
    }
    
    // Clear data when user navigates away from screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearScreenData()
        }
    }

    Box(
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
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "🌱 Smart Waste Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                    Text(
                        "AI-powered waste analysis for a greener future",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF388E3C),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                // Input Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Describe Your Waste",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        OutlinedTextField(
                            value = uiState.userInput,
                            onValueChange = viewModel::updateUserInput,
                            placeholder = {
                                Text(
                                    "Tell me about your waste in detail...\n\nExample: \"Today I threw away 2 plastic bottles (recycled them), 1 kg food waste (composted), and some paper (put in trash)\"\n\nBe specific about quantities and how you disposed of each item!",
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    viewModel.analyzeWaste()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        // Error message
                        if (uiState.errorMessage.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            uiState.errorMessage,
                                            color = Color(0xFFD32F2F),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    

                                    
                                    // Show retry button for rate limit errors
                                    if (uiState.errorMessage.contains("busy") || uiState.errorMessage.contains("quota")) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.analyzeWaste() },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFFD32F2F)
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Try Again")
                                        }
                                    }
                                }
                            }
                        }

                        // Analyze Button
                        Button(
                            onClick = { viewModel.analyzeWaste() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.userInput.isNotBlank() && !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing...")
                            } else {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyze Waste")
                            }
                        }
                    }
                }
            }

            // Results Section
            if (uiState.showResults) {
                item {
                    // Eco Tip Card
                    if (uiState.ecoTip.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    uiState.ecoTip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Detected Waste Items
                items(uiState.editableItems) { item ->
                    WasteItemDisplayCard(item = item)
                }

                item {
                    // Summary and Confirm Section
                    if (uiState.editableItems.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "📊 Impact Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Total CO₂ Impact",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                        Text(
                                            if (uiState.totalCO2Impact <= 0) {
                                                "Saved ${String.format("%.2f", kotlin.math.abs(uiState.totalCO2Impact))} kg CO₂e"
                                            } else {
                                                "Emitted ${String.format("%.2f", uiState.totalCO2Impact)} kg CO₂e"
                                            },
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.totalCO2Impact <= 0) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                        )
                                    }
                                    
                                    Icon(
                                        if (uiState.totalCO2Impact <= 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = if (uiState.totalCO2Impact <= 0) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                // Data automatically saved after AI analysis
                                Toast.makeText(context, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    // Success dialog removed - data is automatically saved
}

@Composable
fun WasteItemDisplayCard(
    item: EditableWasteItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    item.category.icon,
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        item.wasteType,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${item.quantity} ${item.unit} • ${item.disposalMethod.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                val isGood =
                    item.disposalMethod.id == "RECYCLED" || item.disposalMethod.id == "COMPOSTED"
                Text(
                    if (isGood) "Saved" else "Emitted",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGood) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${String.format("%.2f", item.estimatedCO2)} kg CO₂e",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isGood) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableWasteItemCard(
    item: EditableWasteItem,
    onUpdateItem: (EditableWasteItem) -> Unit,
    onRemoveItem: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var selectedDisposal by remember { mutableStateOf(item.disposalMethod) }
    var selectedCategory by remember { mutableStateOf(item.category) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        selectedCategory.icon,
                        fontSize = 20.sp
                    )
                    Column {
                        Text(
                            item.wasteType,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${quantity} ${item.unit} • ${selectedDisposal.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${String.format("%.2f", item.estimatedCO2)} kg CO₂e",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (item.estimatedCO2 > 0) Color(0xFFD32F2F) else Color(0xFF4CAF50)
                    )

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Expandable Edit Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    // Category Selection
                    Text(
                        "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2E7D32)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(WasteConstants.categories) { category ->
                            FilterChip(
                                selected = selectedCategory.id == category.id,
                                onClick = {
                                    selectedCategory = category
                                    onUpdateItem(
                                        item.copy(
                                            category = category,
                                            quantity = quantity.toDoubleOrNull() ?: item.quantity,
                                            disposalMethod = selectedDisposal
                                        )
                                    )
                                },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(category.icon, fontSize = 14.sp)
                                        Text(category.name, fontSize = 12.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE8F5E9),
                                    selectedLabelColor = Color(0xFF2E7D32)
                                )
                            )
                        }
                    }

                    // Quantity Input
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newQuantity ->
                            quantity = newQuantity
                            newQuantity.toDoubleOrNull()?.let { validQuantity ->
                                onUpdateItem(
                                    item.copy(
                                        quantity = validQuantity,
                                        category = selectedCategory,
                                        disposalMethod = selectedDisposal
                                    )
                                )
                            }
                        },
                        label = { Text("Quantity (${item.unit})") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50)
                        )
                    )

                    // Disposal Method Selection
                    Text(
                        "Disposal Method",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2E7D32)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(WasteConstants.disposalMethods) { method ->
                            FilterChip(
                                selected = selectedDisposal.id == method.id,
                                onClick = {
                                    selectedDisposal = method
                                    onUpdateItem(
                                        item.copy(
                                            disposalMethod = method,
                                            category = selectedCategory,
                                            quantity = quantity.toDoubleOrNull() ?: item.quantity
                                        )
                                    )
                                },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(method.icon, fontSize = 14.sp)
                                        Text(method.name, fontSize = 12.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE3F2FD),
                                    selectedLabelColor = Color(0xFF1976D2)
                                )
                            )
                        }
                    }

                    // Remove Button
                    OutlinedButton(
                        onClick = onRemoveItem,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove Item")
                    }
                }
            }
        }
    }
}
