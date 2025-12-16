package com.example.sbikemap.utils

import android.content.Context
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
    val weather: WeatherResponse
)

object RouteWeatherLogic {

    // Hàm chính: Tính toán điểm và lấy dữ liệu thời tiết
    suspend fun generateRouteWeatherMarkers(
        context: Context,
        routeGeometry: LineString // Hình học của tuyến đường
    ): List<RouteWeatherPoint> = withContext(Dispatchers.IO) {

        val totalDistanceKm = TurfMeasurement.length(routeGeometry, TurfConstants.UNIT_KILOMETERS)
        val pointsToCheck = mutableListOf<Point>()

        if (totalDistanceKm < 5.0) {
            // Nếu dưới 5km -> Lấy điểm giữa (50% quãng đường)
            val midDistance = totalDistanceKm / 2
            val midPoint = TurfMeasurement.along(routeGeometry, midDistance, TurfConstants.UNIT_KILOMETERS)
            pointsToCheck.add(midPoint)
        } else {
            // Nếu trên 5km -> Lấy mỗi 5km một điểm
            // Bắt đầu từ km thứ 5, bỏ qua điểm xuất phát (0km) vì thường user đã biết thời tiết ở đó
            var currentDist = 5.0
            while (currentDist < totalDistanceKm) {
                val p = TurfMeasurement.along(routeGeometry, currentDist, TurfConstants.UNIT_KILOMETERS)
                pointsToCheck.add(p)
                currentDist += 5.0
            }
        }

        // Gọi API song song cho tất cả các điểm (nhanh hơn gọi tuần tự)
        val weatherTasks = pointsToCheck.map { point ->
            async {
                val weather = WeatherRepository.fetchWeatherSuspend(context, point.latitude(), point.longitude())
                if (weather != null) RouteWeatherPoint(point, weather) else null
            }
        }

        // Trả về danh sách kết quả (loại bỏ các điểm lỗi null)
        weatherTasks.awaitAll().filterNotNull()
    }
}