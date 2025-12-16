package com.example.sbikemap.utils

import android.util.Log
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import android.content.Context
import com.example.sbikemap.R

data class WeatherResponse(
    @SerializedName("weather") val weather: List<WeatherDescription>,
    @SerializedName("main") val main: MainWeather,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("name") val cityName: String
)

data class WeatherDescription(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String, // Ví dụ: "mưa nhẹ"
    @SerializedName("icon") val icon: String // Mã icon ảnh
)

data class MainWeather(
    @SerializedName("temp") val temp: Double, // Nhiệt độ
    @SerializedName("humidity") val humidity: Int // Độ ẩm
)

data class Wind(
    @SerializedName("speed") val speed: Double // Tốc độ gió
)

// --- 2. RETROFIT INTERFACE ---

interface WeatherApi {
    @GET("data/2.5/weather")
    fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric", // Độ C
        @Query("lang") lang: String = "vi"        // Tiếng Việt
    ): Call<WeatherResponse>

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherSuspend(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<WeatherResponse>
}

// --- 3. OBJECT QUẢN LÝ GỌI API ---

object WeatherRepository {
    private const val BASE_URL = "https://api.openweathermap.org/"

    private val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    // Hàm gọi lấy thời tiết
    fun fetchWeather(context: Context, lat: Double, lon: Double, onResult: (WeatherResponse?) -> Unit) {
        val apiKey = context.getString(R.string.openweather_api_key)
        api.getCurrentWeather(lat, lon, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    Log.e("WeatherUtils", "Lỗi API: ${response.code()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("WeatherUtils", "Lỗi mạng: ${t.message}")
                onResult(null)
            }
        })
    }

    // Lấy thời tiết cho 1 điểm (suspend)
    suspend fun fetchWeatherSuspend(context: Context, lat: Double, lon: Double): WeatherResponse? {
        val apiKey = context.getString(R.string.openweather_api_key)
        return try {
            val response = api.getCurrentWeatherSuspend(lat, lon, apiKey)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}