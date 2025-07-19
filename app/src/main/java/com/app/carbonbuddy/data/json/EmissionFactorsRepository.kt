package com.app.carbonbuddy.data.json

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EmissionFactorsRepository(private val context: Context) {
    suspend fun loadEmissionFactors(): JSONObject = withContext(Dispatchers.IO) {
        val inputStream = context.assets.open("emission_factors.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        JSONObject(json)
    }
}