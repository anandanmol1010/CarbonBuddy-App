package com.app.carbonbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.carbonbuddy.data.WasteConstants
import com.app.carbonbuddy.data.WasteEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class WasteTrackerViewModel : ViewModel() {
    private val _wasteEntries = MutableStateFlow<List<WasteEntry>>(emptyList())
    val wasteEntries: StateFlow<List<WasteEntry>> = _wasteEntries

    private val _currentEntry = MutableStateFlow<WasteEntry?>(null)
    val currentEntry: StateFlow<WasteEntry?> = _currentEntry

    private val _totalEmission = MutableStateFlow(0.0)
    val totalEmission: StateFlow<Double> = _totalEmission

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    fun addWasteEntry(
        category: String,
        disposal: String,
        quantity: Double,
        unit: String
    ) {
        val entry = WasteEntry(
            category = WasteConstants.categories.find { it.id == category }!!,
            disposalMethod = WasteConstants.disposalMethods.find { it.id == disposal }!!,
            quantity = quantity,
            unit = unit,
            emission = WasteConstants.calculateEmission(category, disposal, quantity)
        )

        viewModelScope.launch {
            _wasteEntries.value = _wasteEntries.value + entry
            calculateTotalEmission()
            _currentEntry.value = entry
            _showDialog.value = true
        }
    }

    fun deleteEntry(entry: WasteEntry) {
        viewModelScope.launch {
            _wasteEntries.value = _wasteEntries.value - entry
            calculateTotalEmission()
        }
    }

    private fun calculateTotalEmission() {
        _totalEmission.value = _wasteEntries.value.sumOf { it.emission }
    }

    fun dismissDialog() {
        _showDialog.value = false
    }

    fun getEcoTips(): String {
        val recycled = _wasteEntries.value.filter { it.disposalMethod.id == "RECYCLED" }
        val landfill = _wasteEntries.value.filter { it.disposalMethod.id == "LANDFILL" }
        
        return if (recycled.isNotEmpty() && landfill.isNotEmpty()) {
            val savedEmission = recycled.sumOf { it.emission } - landfill.sumOf { it.emission }
            "Recycling saved ${"%.1f".format(savedEmission)} kg CO₂ vs landfill!"
        } else {
            "Great job managing your waste!"
        }
    }

    fun getEcoAchievements(): String {
        val weeklyEntries = _wasteEntries.value.filter {
            it.timestamp >= System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        }
        
        val recycled = weeklyEntries.filter { it.disposalMethod.id == "RECYCLED" }
        val composted = weeklyEntries.filter { it.disposalMethod.id == "COMPOSTED" }
        
        return when {
            recycled.isNotEmpty() && composted.isNotEmpty() ->
                "${"%.1f".format(recycled.sumOf { it.quantity })} kg waste recycled and " +
                "${"%.1f".format(composted.sumOf { it.quantity })} kg composted this week!"
            recycled.isNotEmpty() ->
                "${"%.1f".format(recycled.sumOf { it.quantity })} kg waste recycled this week!"
            composted.isNotEmpty() ->
                "${"%.1f".format(composted.sumOf { it.quantity })} kg composted this week!"
            else -> "Start your eco-journey by recycling or composting!"
        }
    }
}
