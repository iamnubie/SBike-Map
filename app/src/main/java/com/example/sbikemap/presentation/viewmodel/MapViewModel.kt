package com.example.sbikemap.presentation.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbikemap.data.remote.AuthApi
import com.example.sbikemap.data.remote.TripApi
import com.example.sbikemap.data.remote.models.AirPollutionResponse
import com.example.sbikemap.data.remote.models.CreateTripRequest
import com.example.sbikemap.presentation.MapStyleItem
import com.example.sbikemap.presentation.RouteInfo
import com.example.sbikemap.utils.AIJourneyPlanner
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
    private val tripApi: TripApi,
    private val authApi: AuthApi
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
    var airQualityAtDestination by mutableStateOf<AirPollutionResponse?>(null)

    // 5. Trạng thái tìm kiếm UI
    var isSearching by mutableStateOf(false)
    var currentRoutes by mutableStateOf<List<NavigationRoute>>(emptyList())
    var isFirstLocate by mutableStateOf(true)

    // Hàm lưu chuyến đi (Gọi khi kết thúc dẫn đường)
    fun saveTripToHistory(
        context: Context,
        durationSeconds: Double,
        distanceMeters: Double
        // Bỏ tham số userWeightKg đi vì ta tự fetch bên trong
    ) {
        viewModelScope.launch {
            try {
                var currentWeight = 0.0 // [SỬA]: Mặc định là 0.0

                try {
                    val profileResponse = authApi.getUserProfile()
                    if (profileResponse.isSuccessful && profileResponse.body() != null) {
                        // Nếu user có weight thì lấy, nếu null thì về 0.0
                        currentWeight = profileResponse.body()!!.weight ?: 0.0
                    }
                } catch (e: Exception) {
                    // Lỗi mạng -> Giữ nguyên 0.0
                    e.printStackTrace()
                }

                val durationMinutes = durationSeconds / 60.0
                val distanceKm = distanceMeters / 1000.0
                val speedKmh = if (durationMinutes > 0) distanceKm / (durationMinutes / 60.0) else 0.0

                // Chỉ tính toán nếu cân nặng > 0, ngược lại Calo = 0
                val calories = if (currentWeight > 0) {
                    HealthCalculator.calculateCalories(currentWeight, durationMinutes, speedKmh)
                } else {
                    0.0
                }

                val request = CreateTripRequest(
                    originName = originName,
                    destinationName = destinationName.ifEmpty { "Điểm đến đã chọn" },
                    startTime = Instant.now().toString(),
                    durationSeconds = durationSeconds,
                    distanceMeters = distanceMeters,
                    userWeightSnapshot = currentWeight, // Lưu 0.0 vào lịch sử để biết lúc này chưa có cân nặng
                    caloriesBurned = calories // Lưu 0.0
                )

                val response = tripApi.saveTrip(request)
                if (response.isSuccessful) {
                    // Thông báo khác đi một chút tùy vào có calo hay không
                    val msg = if (calories > 0) "Đã lưu! (${calories.toInt()} kcal)" else "Đã lưu chuyến đi!"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Lỗi lưu: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Lỗi kết nối khi lưu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- LOGIC CACHE AI ---
    private var lastPlannedOrigin: Point? = null
    private var lastPlannedDestination: Point? = null
    private var cachedPlanResult: String? = null

    // Hàm gọi AI thông minh (tự check cache)
    fun getOrFetchJourneyPlan(
        originPoint: Point,
        originName: String,
        destPoint: Point,
        destName: String,
        distanceMeters: Double,
        userWeight: Double,
        onResult: (String) -> Unit
    ) {
        // 1. Kiểm tra xem có trùng với lần trước không
        if (cachedPlanResult != null &&
            originPoint == lastPlannedOrigin &&
            destPoint == lastPlannedDestination) {
            // Trả về cache ngay lập tức, không gọi API
            onResult(cachedPlanResult!!)
            return
        }

        // 2. Nếu khác, gọi API mới
        viewModelScope.launch {
            val result = AIJourneyPlanner.planJourney(
                originName = originName,
                destinationName = destName,
                distanceMeters = distanceMeters,
                userWeightKg = userWeight // Lấy từ Profile user
            )

            // 3. Lưu vào cache
            if (!result.startsWith("Lỗi")) { // Chỉ cache nếu thành công
                lastPlannedOrigin = originPoint
                lastPlannedDestination = destPoint
                cachedPlanResult = result
            }

            onResult(result)
        }
    }

}