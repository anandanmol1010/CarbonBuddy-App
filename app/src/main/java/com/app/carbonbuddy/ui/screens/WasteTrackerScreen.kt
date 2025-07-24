package com.app.carbonbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.carbonbuddy.data.WasteConstants
import com.app.carbonbuddy.viewmodel.WasteTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteTrackerScreen(viewModel: WasteTrackerViewModel) {
    var category by remember { mutableStateOf(WasteConstants.categories.first()) }
    var disposal by remember { mutableStateOf(WasteConstants.disposalMethods.first()) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var showAddDialog by remember { mutableStateOf(false) }

    val wasteEntries by viewModel.wasteEntries.collectAsState()
    val totalEmission by viewModel.totalEmission.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val currentEntry by viewModel.currentEntry.collectAsState()

    Column(
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
        Text(
            "Waste Tracker",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF388E3C),
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
        )

        // Waste Category Selection
        var expandedCategory by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = it },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            TextField(
                value = category.name,
                onValueChange = { /* No-op */ },
                label = { Text("Waste Category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expandedCategory
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                WasteConstants.categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    cat.icon,
                                    fontSize = 18.sp,
                                    color = Color(cat.color)
                                )
                                Text(cat.name)
                            }
                        },
                        onClick = {
                            category = cat
                            expandedCategory = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Disposal Method Selection
        var expandedDisposal by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = expandedDisposal,
            onExpandedChange = { expandedDisposal = it },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            TextField(
                value = disposal.name,
                onValueChange = { /* No-op */ },
                label = { Text("Disposal Method") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expandedDisposal
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expandedDisposal,
                onDismissRequest = { expandedDisposal = false }
            ) {
                WasteConstants.disposalMethods.forEach { method ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    method.icon,
                                    fontSize = 18.sp
                                )
                                Text(method.name)
                            }
                        },
                        onClick = {
                            disposal = method
                            expandedDisposal = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quantity Input
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth(0.8f),
            trailingIcon = {
                Text(unit)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Add Waste Button
        Button(
            onClick = {
                val qty = quantity.toDoubleOrNull() ?: 0.0
                viewModel.addWasteEntry(
                    category = category.id,
                    disposal = disposal.id,
                    quantity = qty,
                    unit = unit
                )
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Add Waste Entry")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Total Emission
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Total Daily Emissions")
                Text(
                    "${"%.2f".format(totalEmission)} kg CO₂",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF388E3C)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Waste Entries List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            items(wasteEntries) { entry ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                entry.category.icon,
                                fontSize = 24.sp,
                                color = Color(entry.category.color)
                            )
                            Column {
                                Text(
                                    entry.category.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${"%.1f".format(entry.quantity)} ${entry.unit}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${"%.2f".format(entry.emission)} kg CO₂",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = { viewModel.deleteEntry(entry) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Waste Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Waste Entry") },
            text = {
                Column {
                    Text("Category")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(WasteConstants.categories) { cat ->
                            FilterChip(
                                selected = category.id == cat.id,
                                onClick = { category = cat },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            cat.icon,
                                            fontSize = 18.sp,
                                            color = Color(cat.color)
                                        )
                                        Text(cat.name)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF388E3C)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Disposal Method")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(WasteConstants.disposalMethods) { method ->
                            FilterChip(
                                selected = disposal.id == method.id,
                                onClick = { disposal = method },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            method.icon,
                                            fontSize = 18.sp
                                        )
                                        Text(method.name)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF1976D2)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Success Dialog
    if (showDialog && currentEntry != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text("Waste Added") },
            text = {
                Column {
                    Text(
                        "${"%.1f".format(currentEntry!!.quantity)} ${currentEntry!!.unit} of " +
                        "${currentEntry!!.category.name} (${currentEntry!!.disposalMethod.name})"
                    )
                    Text(
                        "${"%.2f".format(currentEntry!!.emission)} kg CO₂",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF388E3C)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        viewModel.getEcoTips(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDialog() }) {
                    Text("Close")
                }
            }
        )
        }
    }