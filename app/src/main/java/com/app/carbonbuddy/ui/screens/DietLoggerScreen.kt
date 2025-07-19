package com.app.carbonbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.carbonbuddy.data.FoodDatabase
import com.app.carbonbuddy.data.FoodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietLoggerScreen() {
    var mealTime by remember { mutableStateOf("Breakfast") }
    var selectedFoodItems by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var warning by remember { mutableStateOf(false) }
    var showFoodDialog by remember { mutableStateOf(false) }

    val totalEmission = selectedFoodItems.sumOf { it.emissionFactor }
    warning = totalEmission > 3000
    Column(
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
        Text(
            "Diet Logger",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF388E3C),
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
        )
        
        // Meal Time Selection
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Breakfast", "Lunch").forEach {
                    Button(
                        onClick = { mealTime = it },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mealTime == it) Color(0xFF1976D2) else Color(0xFFF1F8E9),
                            contentColor = if (mealTime == it) Color.White else Color(0xFF1976D2)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(it)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Dinner", "Snack").forEach {
                    Button(
                        onClick = { mealTime = it },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mealTime == it) Color(0xFF1976D2) else Color(0xFFF1F8E9),
                            contentColor = if (mealTime == it) Color.White else Color(0xFF1976D2)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(it)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Searchable Chips Section
        Column(
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            // Search TextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search food items") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    FoodDatabase.foodItems.filter { 
                        it.name.lowercase().contains(searchQuery.lowercase())
                    }
                ) { food ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            if (!selectedFoodItems.contains(food)) {
                                selectedFoodItems = selectedFoodItems + food
                            }
                        },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(food.icon, fontSize = 18.sp)
                                Text(food.name)
                                Text(
                                    "${food.emissionFactor.toInt()}g",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF1F8E9),
                            labelColor = Color(0xFF388E3C)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Items
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(selectedFoodItems) { food ->
                    FilterChip(
                        selected = true,
                        onClick = { /* Do nothing */ },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(food.icon, fontSize = 18.sp)
                                Text(food.name)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFE8F5E9),
                            labelColor = Color(0xFF388E3C)
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.clickable {
                                    selectedFoodItems = selectedFoodItems.filter { it != food }
                                }
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning Message
        if (warning) {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE082)),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "⚠️ High-emission meal. Try substituting with a plant-based option like Tofu.",
                        color = Color(0xFFE65100)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Food Selection Dialog
        if (showFoodDialog) {
            AlertDialog(
                onDismissRequest = { showFoodDialog = false },
                title = { Text("Select Food Items") },
                text = {
                    LazyColumn() {
                        items(FoodDatabase.foodItems.filter { 
                            it.name.lowercase().contains(searchQuery.lowercase()) 
                        }) { food ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!selectedFoodItems.contains(food)) {
                                            selectedFoodItems = selectedFoodItems + food
                                        }
                                        showFoodDialog = false
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    Text(
                                        food.icon,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(food.name)
                                }
                                Text("${food.emissionFactor.toInt()}g CO₂")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFoodDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        Text(
            "Result",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF1976D2)
        )
        Text(
            "${"%.2f".format(totalEmission)} kg CO₂",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color(0xFF388E3C)
        )
    }
}
