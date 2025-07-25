package com.app.carbonbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.carbonbuddy.viewmodel.TransportTrackerViewModel
import android.widget.Toast

@Composable
fun TransportTrackerScreen() {
    val context = LocalContext.current
    val viewModel: TransportTrackerViewModel = viewModel { TransportTrackerViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Transport modes with icons and emission factors
    val transportModes = listOf(
        TransportMode("🚗", "Car", "Private Vehicle", 0.192, Color(0xFFFF5722)),
        TransportMode("🚌", "Bus", "Public Transport", 0.105, Color(0xFF2196F3)),
        TransportMode("🏍️", "Motorbike", "Two Wheeler", 0.089, Color(0xFFFF9800)),
        TransportMode("🚆", "Train", "Rail Transport", 0.041, Color(0xFF9C27B0))
    )
    
    val selectedTransport = transportModes.find { it.name == uiState.selectedMode } ?: transportModes[0]
    
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
            // Header
            Text(
                "🚗 Transport Carbon Tracker",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }
        
        item {
            // Transport Mode Selection
            TransportModeSelector(
                transportModes = transportModes,
                selectedMode = uiState.selectedMode,
                onModeSelected = viewModel::selectMode
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Distance Input
            DistanceInputCard(
                distance = uiState.distance,
                onDistanceChange = viewModel::updateDistance,
                selectedTransport = selectedTransport,
                onCalculate = viewModel::calculateEmission,
                isCalculating = uiState.isCalculating
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Results
        uiState.result?.let { emission ->
            item {
                EmissionResultCard(
                    emission = emission,
                    distance = uiState.distance.toDoubleOrNull() ?: 0.0,
                    transportMode = selectedTransport
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                EcoTipsCard(transportMode = selectedTransport)
            }
        }
        
        // Success Message
        if (uiState.showSuccessMessage) {
            Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show()
            viewModel.dismissSuccessMessage()
        }
    }
}

data class TransportMode(
    val icon: String,
    val name: String,
    val description: String,
    val emissionFactor: Double, // kg CO₂ per km
    val color: Color
)

@Composable
fun TransportModeSelector(
    transportModes: List<TransportMode>,
    selectedMode: String,
    onModeSelected: (String) -> Unit
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
                "Select Transport Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Transport mode grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                transportModes.chunked(2).forEach { rowModes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowModes.forEach { mode ->
                            TransportModeCard(
                                mode = mode,
                                isSelected = selectedMode == mode.name,
                                onSelected = { onModeSelected(mode.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining space if odd number
                        if (rowModes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransportModeCard(
    mode: TransportMode,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelected,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) mode.color.copy(alpha = 0.1f) else Color(0xFFF8F9FA)
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, mode.color)
        } else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                mode.icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                mode.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) mode.color else Color(0xFF424242)
            )
            
            Text(
                mode.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                if (mode.emissionFactor == 0.0) "Zero Emission" else "${(mode.emissionFactor * 1000).toInt()}g CO₂/km",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (mode.emissionFactor == 0.0) Color(0xFF4CAF50) else Color(0xFF757575)
            )
        }
    }
}

@Composable
fun DistanceInputCard(
    distance: String,
    onDistanceChange: (String) -> Unit,
    selectedTransport: TransportMode,
    onCalculate: () -> Unit,
    isCalculating: Boolean = false
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
                "Enter Travel Distance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = distance,
                onValueChange = onDistanceChange,
                label = { Text("Distance (km)") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Route,
                        contentDescription = "Distance",
                        tint = Color(0xFF4CAF50)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    focusedLabelColor = Color(0xFF4CAF50)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCalculate,
                enabled = distance.isNotBlank() && !isCalculating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Calculating...",
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = "Calculate",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Calculate Carbon Footprint",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EmissionResultCard(
    emission: Double,
    distance: Double,
    transportMode: TransportMode
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emission == 0.0) Color(0xFFE8F5E9) else Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🌍 Carbon Footprint Result",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Distance
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Distance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575)
                    )
                    Text(
                        "${distance.toInt()} km",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
                }
                
                // Transport Mode
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575)
                    )
                    Text(
                        "${transportMode.icon} ${transportMode.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = transportMode.color
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Emission Result
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (emission == 0.0) Color(0xFF4CAF50) else Color(0xFFFFF3E0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (emission == 0.0) "🌱 Zero Emissions!" else "CO₂ Emissions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (emission == 0.0) Color.White else Color(0xFF757575)
                    )
                    
                    Text(
                        if (emission == 0.0) "Eco-Friendly Choice!" else "${String.format("%.2f", emission * 1000)} grams CO₂",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (emission == 0.0) Color.White else Color(0xFFFF6F00)
                    )
                    
                    if (emission > 0) {
                        Text(
                            "≈ ${String.format("%.3f", emission)} kg CO₂",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EcoTipsCard(transportMode: TransportMode) {
    val tips = when (transportMode.name) {
        "Car" -> listOf(
            "🚗 Consider carpooling to reduce emissions",
            "⚡ Switch to electric or hybrid vehicles",
            "🚌 Use public transport for longer distances",
            "🚲 Bike or walk for short trips"
        )
        "Bus" -> listOf(
            "🚌 Great choice! Public transport reduces emissions",
            "🚆 Consider trains for longer intercity travel",
            "🚲 Combine with cycling for first/last mile",
            "👥 Encourage others to use public transport"
        )
        "Motorbike" -> listOf(
            "🏍️ Good choice! Lower emissions than cars",
            "🛡️ Always wear helmet and protective gear",
            "🔧 Regular maintenance improves fuel efficiency",
            "🚌 Consider public transport for longer trips"
        )
        "Train" -> listOf(
            "🚆 Excellent choice! Trains are very efficient",
            "📱 Book tickets online to save paper",
            "🚲 Consider bike+train combinations",
            "🌍 One of the most eco-friendly transport options"
        )
        else -> emptyList()
    }
    
    if (tips.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "💡 Eco Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                tips.forEach { tip ->
                    Text(
                        tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}