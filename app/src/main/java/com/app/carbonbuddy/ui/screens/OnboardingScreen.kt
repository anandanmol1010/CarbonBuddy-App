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
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to CarbonBuddy!",
            description = "Track your carbon footprint and make eco-friendly choices.",
            emoji = "🌱"
        ),
        OnboardingPage(
            title = "Log Activities",
            description = "Easily log transport, diet, bills, and waste to see your impact.",
            emoji = "📝"
        ),
        OnboardingPage(
            title = "Earn Rewards",
            description = "Complete challenges and earn badges for sustainable actions!",
            emoji = "🏆"
        )
    )
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
        Spacer(modifier = Modifier.height(40.dp))
        Text(pages[page].emoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(pages[page].title, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF388E3C))
        Spacer(modifier = Modifier.height(12.dp))
        Text(pages[page].description, fontSize = 16.sp, color = Color(0xFF1976D2))
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == page) 16.dp else 8.dp)
                        .background(
                            color = if (i == page) Color(0xFF388E3C) else Color(0xFFB3E5FC),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = {
                if (page < pages.size - 1) page++ else onFinish()
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (page < pages.size - 1) "Next" else "Get Started")
        }
    }
}

data class OnboardingPage(val title: String, val description: String, val emoji: String)