package au.edu.jcu.assessment.utilityapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.edu.jcu.assessment.utilityapp.data.WeatherApiService
import au.edu.jcu.assessment.utilityapp.data.WeatherRepository
import au.edu.jcu.assessment.utilityapp.data.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val data: WeatherResponse) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository(WeatherApiService.create())

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Settings state
    private val _isCelsius = MutableStateFlow(true)
    val isCelsius: StateFlow<Boolean> = _isCelsius.asStateFlow()

    private val _selectedCity = MutableStateFlow("Brisbane")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    init {
        loadWeather()
    }

    fun setUnit(celsius: Boolean) {
        _isCelsius.value = celsius
    }

    fun setLocation(city: String) {
        _selectedCity.value = city
        loadWeatherForCity(city)
    }

    fun loadWeather() {
        loadWeatherForCity(_selectedCity.value)
    }

    private fun loadWeatherForCity(city: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                // Coordinates for Brisbane vs Townsville
                val (lat, lon) = if (city == "Townsville") -19.2590 to 146.8169 else -27.4679 to 153.0281
                val response = repository.fetchWeather(lat, lon)
                _uiState.value = WeatherUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.localizedMessage ?: "Unknown network error")
            }
        }
    }
}