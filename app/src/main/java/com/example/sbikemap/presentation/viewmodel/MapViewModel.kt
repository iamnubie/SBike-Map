package com.example.sbikemap.presentation.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbikemap.data.remote.TripApi
import com.example.sbikemap.data.remote.models.CreateTripRequest
import com.example.sbikemap.presentation.MapStyleItem
import com.example.sbikemap.presentation.RouteInfo
import com.example.sbikemap.utils.HealthCalculator
import com.example.sbikemap.utils.RouteWeatherPoint
import com.example.sbikemap.utils.WeatherResponse
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.search.discover.DiscoverResult
import kotlinx.coroutines.launch
import java.time.Instant

// ViewModel chịu trách nhiệm lưu giữ trạng thái của bản đồ
class MapViewModel(
    private val tripApi: TripApi
) : ViewModel() {

    // 1. Điểm đi, điểm đến, vị trí User
    var userLocationPoint by mutableStateOf<Point?>(null)
    var selectedDestination by mutableStateOf<Point?>(null)
    var customOriginPoint by mutableStateOf<Point?>(null)

    // Tên hiển thị trên ô tìm kiếm
    var originName by mutableStateOf("Vị trí của bạn")
    var destinationName by mutableStateOf("")

    // 2. Thông tin tuyến đường & Navigation
    var routeInfo by mutableStateOf<RouteInfo?>(null)
    var isNavigating by mutableStateOf(false)

    // 3. Kết quả tìm kiếm & Style bản đồ
    var categoryResults by mutableStateOf<List<DiscoverResult>>(emptyList())
    var currentStyleUri by mutableStateOf(Style.MAPBOX_STREETS)

    // 4. Thời tiết
    var weatherAtDestination by mutableStateOf<WeatherResponse?>(null)
    var routeWeatherList by mutableStateOf<List<RouteWeatherPoint>>(emptyList())

    // 5. Trạng thái tìm kiếm UI
    var isSearching by mutableStateOf(false)
    var currentRoutes by mutableStateOf<List<NavigationRoute>>(emptyList())

    // Hàm lưu chuyến đi (Gọi khi kết thúc dẫn đường)
    fun saveTripToHistory(
        context: Context,
        durationSeconds: Double,
        distanceMeters: Double,
        userWeightKg: Double = 70.0 // Mặc định hoặc lấy từ Profile
    ) {
        viewModelScope.launch {
            try {
                // 1. Tính Calo
                val durationMinutes = durationSeconds / 60.0
                val distanceKm = distanceMeters / 1000.0
                val speedKmh = if (durationMinutes > 0) distanceKm / (durationMinutes / 60.0) else 0.0
                val calories = HealthCalculator.calculateCalories(userWeightKg, durationMinutes, speedKmh)

                // 2. Tạo Request
                val request = CreateTripRequest(
                    originName = originName,
                    destinationName = destinationName.ifEmpty { "Điểm đến đã chọn" },
                    startTime = Instant.now().toString(),
                    durationSeconds = durationSeconds,
                    distanceMeters = distanceMeters,
                    userWeightSnapshot = userWeightKg,
                    caloriesBurned = calories
                )

                // 3. Gọi API
                val response = tripApi.saveTrip(request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Đã lưu chuyến đi!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}