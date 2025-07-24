package com.app.carbonbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.carbonbuddy.ui.theme.CarbonBuddyTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.carbonbuddy.ui.screens.*
import com.app.carbonbuddy.ui.screens.ShoppingTrackerScreen
import com.app.carbonbuddy.viewmodel.WasteTrackerViewModel
import com.app.carbonbuddy.viewmodel.WasteManagementViewModel
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String, val label: String, val icon: String) {
    object Home : Screen("home_dashboard", "Home", "🏠")
    object Transport : Screen("transport_tracker", "Transport", "🚗")
    object Diet : Screen("diet_logger", "Diet", "🥗")
    object Utility : Screen("utility_bill_analyzer", "Bills", "💡")
    object Waste : Screen("waste_tracker", "Waste", "🗑️")
    object Shopping : Screen("shopping", "Shopping", "🛒")
    object Onboarding : Screen("onboarding", "Onboarding", "🌱")
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Transport,
    Screen.Diet,
    Screen.Utility,
    Screen.Shopping,
    Screen.Waste
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarbonBuddyTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val wasteViewModel = remember { WasteTrackerViewModel() }
                val wasteManagementViewModel = remember { WasteManagementViewModel(context) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Home.route) { HomeDashboardScreen() }
                        composable(Screen.Transport.route) { TransportTrackerScreen() }
                        composable(Screen.Diet.route) { DietLoggerScreen() }
                        composable(Screen.Utility.route) { UtilityBillAnalyzerScreen() }
                        composable(Screen.Waste.route) { 
                            WasteManagementScreen(viewModel = wasteManagementViewModel)
                        }
                        composable(Screen.Shopping.route) { ShoppingTrackerScreen() }
                        composable(Screen.Onboarding.route) { OnboardingScreen(onFinish = { /* TODO: Navigate to Home */ }) }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFF1F8E9).copy(alpha = 0.3f),
                            Color(0xFFE8F5E9).copy(alpha = 0.5f),
                            Color(0xFFF1F8E9).copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavScreens.forEach { screen ->
                EnhancedNavItem(
                    screen = screen,
                    isSelected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EnhancedNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.7f,
        animationSpec = tween(300),
        label = "alpha_animation"
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .scale(animatedScale)
            .size(width = 56.dp, height = 64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFF4CAF50).copy(alpha = 0.15f)
            } else {
                Color.Transparent
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = Color(0xFF4CAF50).copy(alpha = 0.3f)
            )
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with background circle for selected state
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (isSelected) {
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        } else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = screen.icon,
                    fontSize = 18.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = animatedAlpha
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Label with animation
            Text(
                text = screen.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp
                ),
                color = if (isSelected) {
                    Color(0xFF4CAF50)
                } else {
                    Color(0xFF757575)
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer {
                    alpha = animatedAlpha
                }
            )
        }
    }
}