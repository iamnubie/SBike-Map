package com.example.sbikemap.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.sbikemap.presentation.MapStyleItem
import com.example.sbikemap.presentation.RouteInfo
import com.example.sbikemap.utils.RouteWeatherPoint
import com.example.sbikemap.utils.WeatherResponse
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.search.discover.DiscoverResult

// ViewModel chịu trách nhiệm lưu giữ trạng thái của bản đồ
class MapViewModel : ViewModel() {

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

}