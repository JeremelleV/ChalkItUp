package com.example.chalkitup.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalkitup.domain.model.WeatherResponse
import com.example.chalkitup.network.WeatherApi
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class WeatherViewModel : ViewModel() {
    private val _weather = mutableStateOf<WeatherResponse?>(null)
    val weather: State<WeatherResponse?> = _weather

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(WeatherApi::class.java)

    init {
        fetchWeather()
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                val response = apiService.getWeather(apiKey = "19c78cadfbff4dd5b8f21601250703", location = "Edmonton")
                _weather.value = response
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather: ${e.message}")
            }
        }
    }
}
