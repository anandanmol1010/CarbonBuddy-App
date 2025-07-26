package com.app.carbonbuddy.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val emoji: String,
    val backgroundColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to CarbonBuddy",
            description = "Your personal companion for tracking and reducing your carbon footprint. Let's make the planet greener together!",
            emoji = "🌍",
            backgroundColor = Color(0xFF4CAF50)
        ),
        OnboardingPage(
            title = "Track Your Activities",
            description = "Monitor your transport, diet, shopping, utility bills, and waste to understand your environmental impact.",
            emoji = "📊",
            backgroundColor = Color(0xFF2196F3)
        ),
        OnboardingPage(
            title = "Get Your EcoScore",
            description = "Receive a personalized monthly EcoScore based on your carbon footprint and see how you compare to the average.",
            emoji = "🏆",
            backgroundColor = Color(0xFFFF9800)
        ),
        OnboardingPage(
            title = "Make a Difference",
            description = "Get personalized tips and insights to reduce your carbon footprint and contribute to a sustainable future.",
            emoji = "🌱",
            backgroundColor = Color(0xFF9C27B0)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        pages[pagerState.currentPage].backgroundColor.copy(alpha = 0.1f),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onFinish,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text("Skip")
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Page indicators
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) 
                                    pages[pagerState.currentPage].backgroundColor 
                                else 
                                    Color.Gray.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = pages[pagerState.currentPage].backgroundColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            pages[pagerState.currentPage].backgroundColor
                        )
                    ) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                // Next/Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].backgroundColor
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated emoji
        val infiniteTransition = rememberInfiniteTransition(label = "emoji_animation")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale_animation"
        )

        Text(
            text = page.emoji,
            fontSize = 120.sp,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = page.backgroundColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF424242),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
