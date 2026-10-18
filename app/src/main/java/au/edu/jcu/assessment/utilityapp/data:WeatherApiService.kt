package au.edu.jcu.assessment.utilityapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather
)

data class CurrentWeather(
    val time: String,
    val interval: Int,
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") lat: Double = -27.4679, // Brisbane default
        @Query("longitude") lon: Double = 153.0281,
        @Query("current") current: String = "temperature,windspeed,weathercode"
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"

        fun create(): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}

class WeatherRepository(private val apiService: WeatherApiService) {
    suspend fun fetchWeather(lat: Double, lon: Double) = apiService.getCurrentWeather(lat, lon)
}