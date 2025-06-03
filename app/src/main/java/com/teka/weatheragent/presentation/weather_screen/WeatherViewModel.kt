package com.teka.weatheragent.presentation.weather_screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather,
    val hourly: HourlyWeather
)

data class CurrentWeather(
    val time: String,
    val temperature_2m: Double
)

data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>
)

data class HourlyWeatherData(
    val time: LocalDateTime,
    val temperature: Double
)

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val currentTemperature: Double,
        val hourlyData: List<HourlyWeatherData>
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double = -1.2833,
        @Query("longitude") longitude: Double = 36.8167,
        @Query("hourly") hourly: String = "temperature_2m",
        @Query("current") current: String = "temperature_2m",
        @Query("past_days") pastDays: Int = 2,
        @Query("forecast_days") forecastDays: Int = 14
    ): WeatherResponse
}

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    private val weatherApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApiService::class.java)
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeatherData() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val response = weatherApi.getWeatherData()
                
                val hourlyData = response.hourly.time.zip(response.hourly.temperature_2m) { time, temp ->
                    HourlyWeatherData(
                        time = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        temperature = temp
                    )
                }
                
                _uiState.value = WeatherUiState.Success(
                    currentTemperature = response.current.temperature_2m,
                    hourlyData = hourlyData
                )
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Failed to load weather data: ${e.message}")
            }
        }
    }
}