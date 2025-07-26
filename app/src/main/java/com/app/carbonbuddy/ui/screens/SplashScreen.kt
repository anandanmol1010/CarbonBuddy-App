package com.app.carbonbuddy.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.carbonbuddy.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    
    // Logo scale animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "logo_scale"
    )
    
    // Logo alpha animation
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "logo_alpha"
    )
    
    // Text alpha animation (delayed)
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "text_alpha"
    )
    
    // Tagline alpha animation (more delayed)
    val taglineAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "tagline_alpha"
    )
    
    // Progress animation
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 4000,
            delayMillis = 1000,
            easing = LinearEasing
        ),
        label = "progress"
    )
    
    // Start animations when composable is first composed
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(5000) // 5 seconds total
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E7D32), // Dark Green
                        Color(0xFF4CAF50), // Medium Green
                        Color(0xFF81C784)  // Light Green
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Icon with animation
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌱",
                    fontSize = 60.sp,
                    modifier = Modifier.alpha(logoAlpha)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Name
            Text(
                text = "CarbonBuddy",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // App Tagline
            Text(
                text = "Track • Reduce • Sustain",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.alpha(taglineAlpha),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Loading Progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(taglineAlpha)
            ) {
                Text(
                    text = "Loading your eco-journey...",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Custom Progress Bar
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        
        // Bottom branding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Powered by StackNova • Made with 💚",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
    }
}
