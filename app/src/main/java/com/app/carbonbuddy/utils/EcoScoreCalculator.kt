package com.app.carbonbuddy.utils

import kotlin.math.max

/**
 * EcoScore Calculator for CarbonBuddy App
 * Calculates monthly EcoScore based on user's carbon emissions vs global average
 */
object EcoScoreCalculator {
    
    // Monthly average CO₂ emission per person (in kg)
    private const val MONTHLY_AVERAGE_KG = 550.0
    
    /**
     * Calculate EcoScore based on user's monthly emission
     * @param userMonthlyEmission User's total monthly CO₂ emission in kg
     * @return EcoScore percentage (0-100)
     */
    fun calculateEcoScore(userMonthlyEmission: Double): Double {
        val score = ((MONTHLY_AVERAGE_KG - userMonthlyEmission) / MONTHLY_AVERAGE_KG) * 100
        return max(0.0, score)
    }
    
    /**
     * Get EcoRating based on score
     * @param score EcoScore percentage
     * @return Rating string with emoji
     */
    fun getEcoRating(score: Double): String {
        return when {
            score >= 80 -> "Eco Champion 🏆"
            score >= 60 -> "Eco Warrior 🌟"
            score >= 40 -> "Eco Conscious 🌿"
            score >= 20 -> "Eco Beginner 🟡"
            score >= 1 -> "Eco Learner 🟠"
            else -> "Eco Alert 🔴"
        }
    }
    
    /**
     * Get color for EcoScore display
     * @param score EcoScore percentage
     * @return Color value for UI
     */
    fun getEcoScoreColor(score: Double): Long {
        return when {
            score >= 80 -> 0xFF4CAF50 // Green - Champion
            score >= 60 -> 0xFF8BC34A // Light Green - Warrior
            score >= 40 -> 0xFFCDDC39 // Lime - Conscious
            score >= 20 -> 0xFFFFEB3B // Yellow - Beginner
            score >= 1 -> 0xFFFF9800  // Orange - Learner
            else -> 0xFFF44336        // Red - Alert
        }
    }
    
    /**
     * Get motivational message based on score
     * @param score EcoScore percentage
     * @return Motivational message
     */
    fun getMotivationalMessage(score: Double): String {
        return when {
            score >= 80 -> "Outstanding! You're making a real difference! 🌍"
            score >= 60 -> "Great job! Keep up the excellent work! 💪"
            score >= 40 -> "Good progress! You're on the right track! 🌱"
            score >= 20 -> "Nice start! Small steps lead to big changes! 👍"
            score >= 1 -> "Every effort counts! Keep improving! 🌿"
            else -> "Let's work together to reduce your footprint! 🚀"
        }
    }
    
    /**
     * Calculate improvement needed to reach next level
     * @param currentScore Current EcoScore
     * @param userMonthlyEmission Current monthly emission
     * @return Emission reduction needed in kg
     */
    fun getImprovementNeeded(currentScore: Double, userMonthlyEmission: Double): Double {
        val nextThreshold = when {
            currentScore < 1 -> 20.0
            currentScore < 20 -> 40.0
            currentScore < 40 -> 60.0
            currentScore < 60 -> 80.0
            else -> return 0.0 // Already at top level
        }
        
        // Calculate emission needed for next threshold
        val targetEmission = MONTHLY_AVERAGE_KG * (1 - nextThreshold / 100)
        return max(0.0, userMonthlyEmission - targetEmission)
    }
}
