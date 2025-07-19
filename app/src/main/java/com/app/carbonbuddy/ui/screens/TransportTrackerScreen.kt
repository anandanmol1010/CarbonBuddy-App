package com.app.carbonbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransportTrackerScreen() {
    var mode by remember { mutableStateOf("Car") }
    var distance by remember { mutableStateOf(0f) }
    var result by remember { mutableStateOf<Double?>(null) }
    val emissionFactor = when (mode) {
        "Car" -> 0.192
        "Bus" -> 0.105
        "Bike" -> 0.0
        "Train" -> 0.041
        else -> 0.0
    }
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
            "Transport Tracker",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF388E3C),
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Car", "Bus", "Bike", "Train").forEach {
                Button(
                    onClick = { mode = it },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == it) Color(0xFF388E3C) else Color(0xFFF1F8E9),
                        contentColor = if (mode == it) Color.White else Color(0xFF388E3C)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(it)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = if (distance == 0f) "" else distance.toString(),
            onValueChange = { v -> distance = v.toFloatOrNull() ?: 0f },
            label = { Text("Distance (km)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { result = distance * emissionFactor },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Calculate CO₂")
        }
        Spacer(modifier = Modifier.height(24.dp))
        result?.let {
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
                    Text(
                        "Result",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        "${"%.2f".format(it * 1000)} grams CO₂ emitted",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF388E3C)
                    )
                }
            }
        }
    }
}