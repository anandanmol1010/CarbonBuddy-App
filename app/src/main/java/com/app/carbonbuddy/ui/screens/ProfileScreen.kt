package com.app.carbonbuddy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFB3E5FC))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFB3E5FC)),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 48.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Eco User", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF388E3C))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Streak: 7 days", fontSize = 16.sp, color = Color(0xFF1976D2))
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Achievements", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1976D2))
                Spacer(modifier = Modifier.height(8.dp))
                Text("🌱 3-day Veg Streak")
                Text("🚶 Walk Champ")
                Text("♻️ Waste Reducer")
            }
        }
    }
}