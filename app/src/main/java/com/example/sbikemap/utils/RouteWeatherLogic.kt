package com.example.sbikemap.utils

import android.content.Context
import com.example.sbikemap.data.remote.models.AirPollutionResponse
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

data class RouteWeatherPoint(
    val point: Point,
    val weather: WeatherResponse,
    val airQuality: AirPollutionResponse? = null
)

object RouteWeatherLogic {

    // Hàm chính: Tính toán điểm và lấy dữ liệu thời tiết + AQI
    suspend fun generateRouteWeatherMarkers(
        context: Context,
        routeGeometry: LineString // Hình học của tuyến đường
    ): List<RouteWeatherPoint> = withContext(Dispatchers.IO) {

        val totalDistanceKm = TurfMeasurement.length(routeGeometry, TurfConstants.UNIT_KILOMETERS)
        val pointsToCheck = mutableListOf<Point>()

        if (totalDistanceKm < 5.0) {
            // Nếu dưới 5km -> Lấy điểm giữa
            val midDistance = totalDistanceKm / 2
            val midPoint = TurfMeasurement.along(routeGeometry, midDistance, TurfConstants.UNIT_KILOMETERS)
            pointsToCheck.add(midPoint)
        } else {
            // Nếu trên 5km -> Lấy mỗi 5km một điểm
            var currentDist = 5.0
            while (currentDist < totalDistanceKm) {
                val p = TurfMeasurement.along(routeGeometry, currentDist, TurfConstants.UNIT_KILOMETERS)
                pointsToCheck.add(p)
                currentDist += 5.0
            }
        }

        // Gọi song song cả Weather và AQI cho từng điểm
        val tasks = pointsToCheck.map { point ->
            async {
                val lat = point.latitude()
                val lon = point.longitude()

                // Chạy 2 luồng con song song để tiết kiệm thời gian
                val weatherDeferred = async { WeatherRepository.fetchWeatherSuspend(context, lat, lon) }
                // Lưu ý: Đảm bảo hàm fetchAirQuality đã có trong WeatherRepository
                val aqiDeferred = async {
                    try {
                        WeatherRepository.fetchAirQuality(context, lat, lon)
                    } catch (e: Exception) {
                        null
                    }
                }

                val weather = weatherDeferred.await()
                val aqi = aqiDeferred.await()

                // Chỉ tạo Point khi lấy được thời tiết (AQI có thể null cũng được)
                if (weather != null) {
                    RouteWeatherPoint(point, weather, aqi)
                } else {
                    null
                }
            }
        }

        // Trả về danh sách kết quả (loại bỏ null)
        tasks.awaitAll().filterNotNull()
    }
}