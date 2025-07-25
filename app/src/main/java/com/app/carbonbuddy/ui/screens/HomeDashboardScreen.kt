package com.app.carbonbuddy.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.carbonbuddy.viewmodel.TransportTrackerViewModel

@Composable
fun HomeDashboardScreen() {
    val context = LocalContext.current
    val transportViewModel: TransportTrackerViewModel = viewModel { TransportTrackerViewModel(context) }
    val transportUiState by transportViewModel.uiState.collectAsState()
    
    // Load transport stats when screen opens
    LaunchedEffect(Unit) {
        transportViewModel.loadStats()
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
            // Welcome Header
            WelcomeHeader()
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // EcoScore Dashboard
            EcoScoreDashboard()
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Quick Stats
            QuickStatsRow(transportUiState.stats)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Categories Section
            CategoriesSection(transportUiState.stats)
        }
    }
}

@Composable
fun WelcomeHeader() {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Earth Icon
            val infiniteTransition = rememberInfiniteTransition(label = "earth_rotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF2E7D32)
                            )
                        ),
                        shape = CircleShape
                    )
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🌍",
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    "Welcome to",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
                Text(
                    "CarbonBuddy",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                )
                Text(
                    "Track your carbon footprint",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun EcoScoreDashboard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🌱 Your EcoScore",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Circular Progress Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                CircularProgressIndicator(
                    progress = 0.82f,
                    modifier = Modifier.size(120.dp),
                    color = Color(0xFF4CAF50),
                    strokeWidth = 8.dp,
                    trackColor = Color(0xFFE8F5E9)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "82%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        "Great!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "You're doing great! Keep up the eco-friendly habits.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickStatsRow(transportStats: com.app.carbonbuddy.data.TransportStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickStatCard(
            icon = "📊",
            title = "Today",
            value = "${String.format("%.1f", transportStats.todayEmission)} kg",
            subtitle = "CO₂ emitted",
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        
        QuickStatCard(
            icon = "📅",
            title = "Monthly",
            value = "${String.format("%.1f", transportStats.monthlyEmission)} kg",
            subtitle = "CO₂ emitted",
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
            
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CategoriesSection(transportStats: com.app.carbonbuddy.data.TransportStats) {
    Column {
        Text(
            "📈 Emissions by Category",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Categories Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Transport and Diet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnhancedCategoryCard(
                    category = "Transport",
                    icon = "🚗",
                    emission = "${String.format("%.1f", transportStats.monthlyEmission)} kg CO₂",
                    percentage = 35, // You might want to calculate this dynamically
                    color = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
                EnhancedCategoryCard(
                    category = "Diet",
                    icon = "🥗",
                    emission = "1.5 kg CO₂",
                    percentage = 23,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Row 2: Bills and Shopping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnhancedCategoryCard(
                    category = "Bills",
                    icon = "💡",
                    emission = "1.2 kg CO₂",
                    percentage = 18,
                    color = Color(0xFFFFEB3B),
                    modifier = Modifier.weight(1f)
                )
                EnhancedCategoryCard(
                    category = "Shopping",
                    icon = "🛍️",
                    emission = "1.8 kg CO₂",
                    percentage = 27,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "♻️ Waste Impact",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            WasteImpactCard()
        }
    }
}

@Composable
fun EnhancedCategoryCard(
    category: String,
    icon: String,
    emission: String,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    icon,
                    fontSize = 28.sp
                )
                
                Text(
                    "$percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            
            Text(
                emission,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun WasteImpactCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "♻️",
                    fontSize = 28.sp
                )
                
                Text(
                    "Net Impact: -1.2 kg CO₂",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Waste Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Waste breakdown
            WasteBreakdownRow(
                icon = "♻️",
                action = "Recycled",
                amount = "5.2 kg",
                impact = "-0.8 kg CO₂",
                color = Color(0xFF4CAF50)
            )
            
            WasteBreakdownRow(
                icon = "🌱",
                action = "Composted", 
                amount = "3.1 kg",
                impact = "-0.6 kg CO₂",
                color = Color(0xFF8BC34A)
            )
            
            WasteBreakdownRow(
                icon = "🗑️",
                action = "Landfill",
                amount = "1.5 kg", 
                impact = "+0.2 kg CO₂",
                color = Color(0xFFFF5722)
            )
        }
    }
}

@Composable
fun WasteBreakdownRow(
    icon: String,
    action: String,
    amount: String,
    impact: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Column {
                Text(
                    action,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF424242)
                )
                Text(
                    amount,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
        }
        
        Text(
            impact,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
