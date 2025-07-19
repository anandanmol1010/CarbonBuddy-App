package com.app.carbonbuddy.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val ecoScore: Int = 82,
    val transport: Double = 12.3,
    val food: Double = 8.1,
    val bills: Double = 5.2,
    val waste: Double = 2.0
)

class HomeDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    // TODO: Add logic to fetch and update data from repository
}