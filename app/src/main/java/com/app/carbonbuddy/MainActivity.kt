package com.app.carbonbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    Screen.Waste,
    Screen.Shopping
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarbonBuddyTheme {
                val navController = rememberNavController()
                val viewModel = remember { WasteTrackerViewModel() }
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
                            WasteTrackerScreen(viewModel = viewModel)
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
    NavigationBar {
        bottomNavScreens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Text(screen.icon, fontSize = MaterialTheme.typography.titleLarge.fontSize) },
                label = {
                    Text(
                        text = screen.label,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}