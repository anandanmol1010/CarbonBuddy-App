package com.app.carbonbuddy.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.carbonbuddy.data.TransportEntry
import com.app.carbonbuddy.data.TransportStats
import com.app.carbonbuddy.repository.TransportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class TransportUiState(
    val selectedMode: String = "Car",
    val distance: String = "",
    val result: Double? = null,
    val isCalculating: Boolean = false,
    val stats: TransportStats = TransportStats(),
    val showSuccessMessage: Boolean = false
)

class TransportTrackerViewModel(context: Context) : ViewModel() {
    
    private val repository = TransportRepository(context)
    
    private val _uiState = MutableStateFlow(TransportUiState())
    val uiState: StateFlow<TransportUiState> = _uiState.asStateFlow()
    
    // Transport modes with emission factors (kg CO2 per km)
    private val emissionFactors = mapOf(
        "Car" to 0.192,
        "Bus" to 0.089,
        "Motorbike" to 0.084,
        "Train" to 0.035
    )
    
    init {
        loadStats()
    }
    
    fun selectMode(mode: String) {
        _uiState.value = _uiState.value.copy(
            selectedMode = mode,
            result = null,
            showSuccessMessage = false
        )
    }
    
    fun updateDistance(distance: String) {
        _uiState.value = _uiState.value.copy(
            distance = distance,
            result = null,
            showSuccessMessage = false
        )
    }
    
    fun calculateEmission() {
        val currentState = _uiState.value
        val distanceValue = currentState.distance.toDoubleOrNull()
        
        if (distanceValue == null || distanceValue <= 0) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isCalculating = true)
            
            try {
                val emissionFactor = emissionFactors[currentState.selectedMode] ?: 0.192
                val totalEmission = distanceValue * emissionFactor
                
                // Save to local storage
                val entry = TransportEntry(
                    id = UUID.randomUUID().toString(),
                    transportMode = currentState.selectedMode,
                    distance = distanceValue,
                    emission = totalEmission,
                    timestamp = System.currentTimeMillis()
                )
                
                repository.saveTransportEntry(entry)
                
                // Update UI
                _uiState.value = currentState.copy(
                    result = totalEmission,
                    isCalculating = false,
                    showSuccessMessage = true
                )
                
                // Refresh stats
                loadStats()
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isCalculating = false,
                    result = null
                )
            }
        }
    }
    
    fun loadStats() {
        viewModelScope.launch {
            val stats = repository.getTodayStats()
            _uiState.value = _uiState.value.copy(stats = stats)
        }
    }
    
    fun dismissSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
    
    fun resetCalculation() {
        _uiState.value = _uiState.value.copy(
            distance = "",
            result = null,
            showSuccessMessage = false
        )
    }
    
    fun getStats(): TransportStats {
        return _uiState.value.stats
    }
}
