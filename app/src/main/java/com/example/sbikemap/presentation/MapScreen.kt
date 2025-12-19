package com.example.sbikemap.presentation

import MapLocationHandler
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.sbikemap.R
import com.example.sbikemap.utils.RouteRenderer
import com.example.sbikemap.utils.UserMarker
import com.example.sbikemap.utils.bitmapFromDrawableRes
import com.example.sbikemap.utils.requestCyclingRoute
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.common.MapboxOptions
import com.mapbox.common.TileStore
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.options.RoutingTilesOptions
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.tripdata.maneuver.model.Maneuver
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.discover.Discover
import com.mapbox.search.discover.DiscoverOptions
import com.mapbox.search.discover.DiscoverQuery
import com.mapbox.search.discover.DiscoverResult
import kotlinx.coroutines.launch
import com.example.sbikemap.utils.WeatherRepository
import com.example.sbikemap.utils.WeatherResponse
import com.mapbox.geojson.LineString
import com.example.sbikemap.utils.RouteWeatherLogic
import com.example.sbikemap.utils.RouteWeatherPoint
import com.example.sbikemap.presentation.components.WeatherRouteMarker
import com.example.sbikemap.presentation.viewmodel.MapViewModel
import com.example.sbikemap.utils.AIJourneyPlanner
import com.example.sbikemap.utils.VoiceHelper
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.mapbox.turf.TurfMisc

data class MapStyleItem(
    val name: String,
    val styleUri: String,
    val icon: ImageVector
)
// Data class lưu thông tin tuyến đường
data class RouteInfo(
    val distanceMeters: Double,
    val durationSeconds: Double
)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
fun MapScreen(
    permissionsGranted: Boolean,
    navController: androidx.navigation.NavController,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val START_POINT = Point.fromLngLat(105.8544, 21.0285)
    val DESTINATION_ICON_ID = "DESTINATION_PIN_ICON"
    val START_ICON_ID = "START_PIN_ICON"

    // State Variables
    var puckBearingSource by remember { mutableStateOf(PuckBearing.HEADING) }
    var showStyleSheet by remember { mutableStateOf(false) }
//    var isFirstLocate by remember { mutableStateOf(true) }
    // Logic tính toán điểm bắt đầu thực tế
    val actualStartPoint = mapViewModel.customOriginPoint ?: mapViewModel.userLocationPoint

    // --- STATE CHO NAVIGATION UI ---
    var navigationState by remember { mutableStateOf(NavigationState()) }

    // --- 1. KHỞI TẠO CÁC FORMATTER RIÊNG BIỆT (FIX LỖI UNRESOLVED REFERENCE) ---
    val distanceFormatterOptions = remember { DistanceFormatterOptions.Builder(context).build() }
    // Formatter khoảng cách (VD: 5.2 km)
    val distanceRemainingFormatter = remember { DistanceRemainingFormatter(distanceFormatterOptions) }
    // Formatter thời gian (VD: 15 min)
    val timeRemainingFormatter = remember { TimeRemainingFormatter(context) }
    // API lấy thông tin Maneuver (rẽ trái/phải)
    val maneuverApi = remember { MapboxManeuverApi(MapboxDistanceFormatter(distanceFormatterOptions)) }

    // 1. Đảm bảo Token đã được set toàn cục
    SideEffect {
        MapboxOptions.accessToken = context.getString(R.string.mapbox_access_token)
    }
    // Khởi tạo Discover API (Thay cho SearchEngine)
    val discover = remember { Discover.create() }
    // Khởi tạo SearchEngine với Settings mặc định (Không truyền Token vào đây)
    val searchEngine = remember {
        SearchEngine.createSearchEngine(
            SearchEngineSettings()
        )
    }
    //Logic quyết định khi nào thì hiện giao diện 2 ô (Expanded)
    val shouldShowExpandedUI = mapViewModel.isSearching || mapViewModel.selectedDestination != null || mapViewModel.customOriginPoint != null
    val SEARCH_RESULT_ICON_ID = "SEARCH_RESULT_ICON" // ID cho icon kết quả tìm kiếm
    // State để lưu tham chiếu bản đồ dùng cho việc tính toán camera sau này
    var mapboxMapInstance by remember { mutableStateOf<com.mapbox.maps.MapboxMap?>(null) }
    val currentCategoryResults by rememberUpdatedState(mapViewModel.categoryResults)
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(10.0)
            center(START_POINT)
            pitch(0.0)
            bearing(0.0)
        }
    }
    val mapboxNavigation = remember {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            // [CẬP NHẬT] Cấu hình Offline
            // 1. Tạo TileStore mặc định (Nơi dữ liệu Offline được lưu)
            val tileStore = TileStore.create()

            // 2. Chỉ định Navigation sử dụng TileStore này để tìm đường
            val routingTilesOptions = RoutingTilesOptions.Builder()
                .tileStore(tileStore)
                .build()

            // 3. Tạo Navigation Options với cấu hình Offline
            val navOptions = NavigationOptions.Builder(context)
                .routingTilesOptions(routingTilesOptions)
                .build()

            // 4. Khởi tạo Navigation
            MapboxNavigationProvider.create(navOptions)
        }
    }

    // Hàm xử lý hoàn thành chuyến đi (Lưu và thoát)
    val onFinishTrip = {
        val currentRoute = mapViewModel.routeInfo
        // Chỉ lưu nếu chuyến đi > 10 giây để tránh spam
        if (currentRoute != null && currentRoute.durationSeconds > 10) {
            mapViewModel.saveTripToHistory(
                context = context,
                durationSeconds = currentRoute.durationSeconds,
                distanceMeters = currentRoute.distanceMeters
            )
        }

        // Reset trạng thái
        mapViewModel.isNavigating = false
        mapViewModel.routeInfo = null
        mapViewModel.selectedDestination = null
        mapboxNavigation.setNavigationRoutes(emptyList())
    }

    // Khi chọn địa điểm mới -> Gọi API lấy thời tiết
    LaunchedEffect(mapViewModel.selectedDestination) {
        if (mapViewModel.selectedDestination != null) {
            WeatherRepository.fetchWeather(
                context,
                mapViewModel.selectedDestination!!.latitude(),
                mapViewModel.selectedDestination!!.longitude()
            ) { response ->
                mapViewModel.weatherAtDestination = response
            }
        } else {
            mapViewModel.weatherAtDestination = null
        }
    }

    fun reverseGeocode(point: Point, isOrigin: Boolean) {
        if (isOrigin) mapViewModel.originName = "Đang lấy địa chỉ..." else mapViewModel.destinationName = "Đang lấy địa chỉ..."

        val options = ReverseGeoOptions(center = point, limit = 1)

        // Gọi hàm search với interface SearchCallback chuẩn
        searchEngine.search(options, object : SearchCallback {
            // Hàm trả về kết quả thành công
            override fun onResults(
                results: List<SearchResult>,
                responseInfo: com.mapbox.search.ResponseInfo
            ) {
                // Lấy tên địa điểm, nếu không có thì lấy tọa độ làm tên
                val address = results.firstOrNull()?.name
                    ?: "${point.latitude().toString().take(7)}, ${point.longitude().toString().take(7)}"

                if (isOrigin) mapViewModel.originName = address else mapViewModel.destinationName = address
            }

            // Hàm trả về lỗi
            override fun onError(e: Exception) {
                if (isOrigin) mapViewModel.originName = "Vị trí đã chọn" else mapViewModel.destinationName = "Vị trí đã chọn"
            }
        })
    }



    // Tự động tính toán khi routeInfo thay đổi (có đường mới hoặc xóa đường)
    LaunchedEffect(mapViewModel.routeInfo) {
        if (mapViewModel.routeInfo != null) {
            // Lấy danh sách tuyến đường
            val routes = mapboxNavigation.getNavigationRoutes()
            val primaryRoute = routes.firstOrNull()

            // 1. Lấy chuỗi polyline (String) bằng property (không dùng dấu ngoặc)
            val routeGeometryStr = primaryRoute?.directionsRoute?.geometry()

            // 2. Chuyển đổi String -> LineString (để dùng được với Turf/WeatherLogic)
            // Mapbox mặc định dùng độ chính xác là 6 (PRECISION_6)
            val geometry = if (routeGeometryStr != null) {
                LineString.fromPolyline(routeGeometryStr, 6)
            } else {
                null
            }

            if (geometry != null) {
                // Gọi hàm logic tính toán
                mapViewModel.routeWeatherList = RouteWeatherLogic.generateRouteWeatherMarkers(context, geometry)
            }
        } else {
            mapViewModel.routeWeatherList = emptyList()
        }
    }
    // Khôi phục lại tuyến đường cũ nếu có (Quan trọng khi quay lại màn hình)
    LaunchedEffect(Unit) {
        if (mapViewModel.routeInfo != null && mapViewModel.selectedDestination != null) {

            // Kiểm tra xem trong ViewModel đã lưu danh sách tuyến đường chưa.
            // Nếu có rồi -> Set luôn vào Mapbox (Vẽ ngay lập tức, KHÔNG TỐN API)
            if (mapViewModel.currentRoutes.isNotEmpty()) {
                mapboxNavigation.setNavigationRoutes(mapViewModel.currentRoutes)
            }
            // Nếu chưa có (hoặc bị mất) -> Mới phải gọi API tải lại
            else {
                val start = mapViewModel.customOriginPoint ?: mapViewModel.userLocationPoint
                if (start != null) {
                    // Cập nhật callback nhận vào 'routes' thay vì '_ , _'
                    requestCyclingRoute(context, mapboxNavigation, start, mapViewModel.selectedDestination!!) { routes ->
                        // Cập nhật lại danh sách routes vào ViewModel để lần sau dùng lại
                        if (routes.isNotEmpty()) {
                            mapViewModel.currentRoutes = routes
                        }
                    }
                }
            }
        }
    }

    // Hàm tìm kiếm theo danh mục (Nearby Search)
    fun searchCategoryNearby(categoryQuery: String) {
        // 1. Kiểm tra vị trí hiện tại
        val center = mapViewModel.userLocationPoint
        if (center == null) {
            Toast.makeText(context, "Đang lấy vị trí của bạn... Hãy thử lại sau giây lát.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "Đang tìm $categoryQuery...", Toast.LENGTH_SHORT).show()

        // 2. Gọi hàm search trong Coroutine Scope
        scope.launch {
            val response = discover.search(
                query = DiscoverQuery.Category.create(categoryQuery), // Tạo truy vấn danh mục chuẩn
                proximity = center, // Ưu tiên tìm quanh vị trí User
                options = DiscoverOptions(limit = 20) // Giới hạn 20 kết quả
            )

            // 3. Xử lý kết quả trả về
            response.onValue { results ->
                // A. Lưu kết quả vào biến State (Lưu ý: biến này phải là List<DiscoverResult>)
                mapViewModel.categoryResults = results

                if (results.isNotEmpty()) {
                    // B. Lấy danh sách tọa độ các điểm tìm được
                    val points = results.map { it.coordinate }

                    // C. Tính toán khung hình Camera để bao quát tất cả (User + Các quán)
                    if (mapboxMapInstance != null) {
                        val allPoints = points + center
                        val cameraOptions = mapboxMapInstance!!.cameraForCoordinates(
                            allPoints,
                            // Padding: Trên=160 (tránh thanh Search), Dưới=160 (tránh BottomSheet), Trái/Phải=100
                            EdgeInsets(160.0, 100.0, 160.0, 100.0)
                        )
                        // D. Bay camera tới khung hình đó
                        mapViewportState.flyTo(cameraOptions)
                    }
                } else {
                    Toast.makeText(context, "Không tìm thấy địa điểm nào gần đây.", Toast.LENGTH_SHORT).show()
                }
            }.onError { e ->
                // Xử lý lỗi
                android.util.Log.e("MapSearch", "Lỗi Discover: ${e.message}")
                Toast.makeText(context, "Lỗi tìm kiếm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val routeLineApi = remember { MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build()) }
    val routeLineView = remember {
        MapboxRouteLineView(
            MapboxRouteLineViewOptions.Builder(context)
                .routeLineBelowLayerId("road-label")
                .build()
        )
    }

    // Xử lý nút Back khi đang dẫn đường
    BackHandler(enabled = mapViewModel.isNavigating) {
        // Nếu bấm Back khi đang dẫn đường -> Hỏi hoặc thoát dẫn đường
        mapViewModel.isNavigating = false
        // Nếu muốn hủy chuyến đi thì reset hết
        mapViewModel.selectedDestination = null
        mapViewModel.routeInfo = null
        mapboxNavigation.setNavigationRoutes(emptyList())
        mapboxNavigation.mapboxReplayer.stop()
    }

    DisposableEffect(Unit) {
        onDispose { MapboxNavigationProvider.destroy() }
    }

    // LOGIC LẮNG NGHE TIẾN TRÌNH DẪN ĐƯỜNG & GIỌNG NÓI
    DisposableEffect(mapViewModel.isNavigating) {
        if (mapViewModel.isNavigating) {
            val voiceHelper = VoiceHelper(context)
            val progressObserver = RouteProgressObserver { routeProgress ->
                // Lấy Expected (chứa List<Maneuver> hoặc Lỗi)
                val maneuversExpected = maneuverApi.getManeuvers(routeProgress)

                // Thay vì trả về Maneuver (có thể null), ta trả về List (luôn không null)
                // để thỏa mãn @NonNull của hàm fold.
                val maneuversList = maneuversExpected.fold(
                    { emptyList<Maneuver>() }, // Nếu lỗi -> Trả về list rỗng
                    { it }                     // Nếu OK -> Trả về list kết quả
                )

                // Lấy phần tử đầu tiên một cách an toàn
                val currentManeuver = maneuversList.firstOrNull()

                //  Format dữ liệu
                val distanceStr = distanceRemainingFormatter.format(routeProgress.distanceRemaining.toDouble())
                val timeStr = timeRemainingFormatter.format(routeProgress.durationRemaining)

                // Cập nhật UI State
                navigationState = NavigationState(
                    maneuverIcon = when {
                        currentManeuver?.primary?.modifier?.contains("left") == true -> Icons.Default.ArrowBack
                        currentManeuver?.primary?.modifier?.contains("right") == true -> Icons.Default.ArrowForward
                        else -> Icons.Default.KeyboardArrowUp
                    },
                    instruction = getManeuverTranslation(
                        currentManeuver?.primary?.modifier,
                        currentManeuver?.primary?.type
                    ),
                    distanceToTurn = currentManeuver?.primary?.text ?: "",
                    timeRemaining = timeStr.toString(),
                    distanceRemaining = distanceStr.toString()
                )
            }
            //Observer: Lấy câu nhắc giọng nói từ Mapbox
            val voiceObserver = VoiceInstructionsObserver { voiceInstructions ->
                // voiceInstructions.announcement chứa câu nói chuẩn (VD: "Turn right in 200 meters")
                // Mapbox tự tính toán thời điểm nói hợp lý nhất
                val textToSpeak = voiceInstructions.announcement() ?: ""
                // Chỉ đọc nếu có nội dung
                if (textToSpeak.isNotEmpty()) {
                    voiceHelper.speak(textToSpeak)
                }
            }
            // 2. Observer đến nơi (Arrival)
            val arrivalObserver = object : ArrivalObserver {

                // Hàm này BẮT BUỘC phải có (dù không dùng cũng phải để trống)
                // Chạy khi đến các điểm dừng trung gian (waypoints)
                override fun onWaypointArrival(routeProgress: RouteProgress) {}

                // Hàm này chạy khi ĐẾN ĐÍCH CUỐI CÙNG
                override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
                    voiceHelper.speak("Bạn đã đến nơi. Chuyến đi kết thúc.")
                    onFinishTrip()
                    Toast.makeText(context, "Đã đến nơi! Chuyến đi đã được lưu.", Toast.LENGTH_LONG).show()
                }

                // Hàm này chạy khi bắt đầu chặng tiếp theo
                override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {}
            }

            mapboxNavigation.registerRouteProgressObserver(progressObserver)
            mapboxNavigation.registerVoiceInstructionsObserver(voiceObserver)
            mapboxNavigation.registerArrivalObserver(arrivalObserver)
            mapboxNavigation.startTripSession()

            onDispose {
                mapboxNavigation.unregisterRouteProgressObserver(progressObserver)
                mapboxNavigation.unregisterVoiceInstructionsObserver(voiceObserver)
                mapboxNavigation.unregisterArrivalObserver(arrivalObserver)
                mapboxNavigation.stopTripSession()
                voiceHelper.shutdown()
            }
        } else {
            onDispose { }
        }
    }

    // Danh sách kiểu bản đồ
    val mapStyles = listOf(
        MapStyleItem("Phố", Style.MAPBOX_STREETS, Icons.Default.Build),
        MapStyleItem("Vệ tinh", Style.SATELLITE_STREETS, Icons.Default.Build),
        MapStyleItem("Mật độ", Style.TRAFFIC_DAY, Icons.Default.Build),
        MapStyleItem("Tối", Style.TRAFFIC_NIGHT, Icons.Default.Build)
    )

    // Hàm helper để cập nhật thông tin tuyến đường lên UI
    fun updateRouteInfo(routes: List<NavigationRoute>) {
        if (routes.isNotEmpty()) {
            val primaryRoute = routes[0] // Tuyến đầu tiên luôn là tuyến chính
            mapViewModel.routeInfo = RouteInfo(
                distanceMeters = primaryRoute.directionsRoute.distance(),
                durationSeconds = primaryRoute.directionsRoute.duration()
            )
            mapViewModel.currentRoutes = routes // Lưu vào VM
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- BẢN ĐỒ ---
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = mapViewModel.currentStyleUri) }
        ) {
            MapEffect(Unit) { mapView ->
                mapboxMapInstance = mapView.mapboxMap
            }
            if (mapViewModel.selectedDestination != null) {
                UserMarker(point = mapViewModel.selectedDestination!!, iconId = DESTINATION_ICON_ID)
            }
            if (mapViewModel.customOriginPoint != null) {
                UserMarker(point = mapViewModel.customOriginPoint!!, iconId = START_ICON_ID)
            }
            // VẼ MARKER CHO CÁC KẾT QUẢ TÌM ĐƯỢC
            if (mapViewModel.categoryResults.isNotEmpty()) {
                mapViewModel.categoryResults.forEach { result ->
                    result.coordinate?.let { point ->
                        UserMarker(point = point, iconId = SEARCH_RESULT_ICON_ID)
                    }
                }
            }

            // VẼ CÁC WEATHER MARKER LÊN TUYẾN ĐƯỜNG
            mapViewModel.routeWeatherList.forEach { weatherPoint ->
                WeatherRouteMarker(data = weatherPoint)
            }

            if (permissionsGranted) {
                MapEffect(mapViewModel.currentStyleUri) { mapView ->
                    mapView.mapboxMap.getStyle { style ->
                        val scaleFactor = 0.8
                        if (style.getStyleImage(DESTINATION_ICON_ID) == null) {
                            val bitmap = bitmapFromDrawableRes(context, R.drawable.location)
                            if (bitmap != null) style.addImage(DESTINATION_ICON_ID, bitmap)
                        }
                        if (style.getStyleImage(START_ICON_ID) == null) {
                            val bitmap = bitmapFromDrawableRes(context, R.drawable.location2)
                            if (bitmap != null) {
                                val newWidth = (bitmap.width * scaleFactor).toInt()
                                val newHeight = (bitmap.height * scaleFactor).toInt()
                                val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                                    bitmap, newWidth, newHeight, true
                                )
                                style.addImage(START_ICON_ID, scaledBitmap)
                            }
                        }
                        // Nạp icon cho kết quả tìm kiếm (Ví dụ màu Vàng hoặc icon quán cafe)
                        if (style.getStyleImage(SEARCH_RESULT_ICON_ID) == null) {
                            val bitmap = bitmapFromDrawableRes(context, R.drawable.location)
                            if (bitmap != null) {
                                val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * 0.7).toInt(), (bitmap.height * 0.7).toInt(), true)
                                style.addImage(SEARCH_RESULT_ICON_ID, scaled)
                            }
                        }
                    }
                }

                RouteRenderer(mapboxNavigation, routeLineApi, routeLineView)

                // Xử lý Click (Chỉ cho phép chọn điểm mới khi KHÔNG dẫn đường)
                if (!mapViewModel.isNavigating) {
                    MapEffect(Unit) { mapView ->
                        mapView.mapboxMap.addOnMapClickListener { clickedPoint ->

                            // --- TRƯỜNG HỢP 1: KIỂM TRA CHẠM VÀO TUYẾN ĐƯỜNG (DÙNG TURF) ---
                            // Mục đích: Cho phép user chọn tuyến đường thay thế (màu xám)
                            val currentList = mapViewModel.currentRoutes.toMutableList()
                            var closestRoute: com.mapbox.navigation.base.route.NavigationRoute? = null
                            var minDistance = Double.MAX_VALUE
                            val TOUCH_THRESHOLD_METERS = 50.0 // Khoảng cách nhận diện click (50m)

                            // Duyệt qua tất cả các tuyến đường đang hiển thị
                            // Duyệt qua tất cả các tuyến đường đang hiển thị
                            for (route in currentList) {
                                val routeGeometryStr = route.directionsRoute.geometry()
                                if (routeGeometryStr != null) {
                                    val lineString = LineString.fromPolyline(routeGeometryStr, 6)

                                    // [SỬA LẠI ĐOẠN NÀY]
                                    // 1. Tìm điểm nằm trên sợi dây (tuyến đường) gần chỗ click nhất
                                    val nearestFeature = TurfMisc.nearestPointOnLine(clickedPoint, lineString.coordinates())
                                    val snappedPoint = nearestFeature.geometry() as? com.mapbox.geojson.Point

                                    if (snappedPoint != null) {
                                        // 2. Đo khoảng cách từ ngón tay (clickedPoint) đến điểm đó (snappedPoint)
                                        val distance = TurfMeasurement.distance(
                                            clickedPoint,
                                            snappedPoint,
                                            TurfConstants.UNIT_METERS
                                        )

                                        // Tìm tuyến gần nhất
                                        if (distance < minDistance) {
                                            minDistance = distance
                                            closestRoute = route
                                        }
                                    }
                                }
                            }

                            // Nếu chạm trúng một tuyến đường (khoảng cách < 50m)
                            if (closestRoute != null && minDistance < TOUCH_THRESHOLD_METERS) {
                                // Nếu tuyến đó chưa phải là tuyến chính (Index 0) -> Đảo lên đầu
                                if (currentList.indexOf(closestRoute) != 0) {
                                    currentList.remove(closestRoute)
                                    currentList.add(0, closestRoute!!)

                                    // Mapbox vẽ lại: Tuyến đầu xanh, tuyến sau xám
                                    mapboxNavigation.setNavigationRoutes(currentList)

                                    // Cập nhật thông tin UI (Giờ/Phút)
                                    updateRouteInfo(currentList)

                                    Toast.makeText(context, "Đã chọn tuyến đường khác", Toast.LENGTH_SHORT).show()
                                }
                                // Đã xử lý xong click đường -> Dừng lại (không check marker nữa)
                                return@addOnMapClickListener true
                            }

                            // --- TRƯỜNG HỢP 2: KIỂM TRA CHẠM VÀO MARKER KẾT QUẢ ---
                            val results = currentCategoryResults // Lấy danh sách kết quả mới nhất
                            val screenPoint = mapView.mapboxMap.pixelForCoordinate(clickedPoint) // Tọa độ pixel

                            if (results.isNotEmpty()) {
                                val touchRadiusPx = 70.0 // Bán kính chạm (pixel)

                                val matchedResult = results.find { result ->
                                    val markerScreenPoint = mapView.mapboxMap.pixelForCoordinate(result.coordinate)
                                    val dx = screenPoint.x - markerScreenPoint.x
                                    val dy = screenPoint.y - markerScreenPoint.y
                                    // Tính khoảng cách giữa ngón tay và Marker trên màn hình
                                    Math.sqrt(dx * dx + dy * dy) < touchRadiusPx
                                }

                                if (matchedResult != null) {
                                    // A. Click TRÚNG Marker
                                    mapViewModel.selectedDestination = matchedResult.coordinate
                                    mapViewModel.destinationName = matchedResult.name
                                    Toast.makeText(context, "Đã chọn: ${matchedResult.name}", Toast.LENGTH_SHORT).show()

                                    // Vẽ đường
                                    val start = mapViewModel.customOriginPoint ?: mapViewModel.userLocationPoint
                                    // [LƯU Ý] Hàm này cần trả về danh sách routes để hỗ trợ alternative
                                    requestCyclingRoute(context, mapboxNavigation, start, matchedResult.coordinate) { routes ->
                                        updateRouteInfo(routes)
                                    }
                                } else {
                                    // B. Click TRƯỢT Marker khi đang hiện list -> Bỏ qua
                                    return@addOnMapClickListener false
                                }
                            }
                            // --- TRƯỜNG HỢP 3: CHỌN ĐIỂM BẤT KỲ TRÊN BẢN ĐỒ ---
                            else {
                                // Nếu đã chọn đích rồi (đang hiện BottomSheet) thì chặn click lung tung
                                if (mapViewModel.selectedDestination != null) {
                                    return@addOnMapClickListener false
                                }

                                // Chọn điểm mới
                                mapViewModel.selectedDestination = clickedPoint
                                reverseGeocode(clickedPoint, isOrigin = false)
//                                Toast.makeText(context, "Đã chọn điểm trên bản đồ", Toast.LENGTH_SHORT).show()

                                // Vẽ đường
                                val start = mapViewModel.customOriginPoint ?: mapViewModel.userLocationPoint
                                requestCyclingRoute(context, mapboxNavigation, start, clickedPoint) { routes ->
                                    updateRouteInfo(routes)
                                }
                            }
                            true // Báo hiệu đã nhận sự kiện click
                        }
                    }
                }

                // Camera & Location Logic
                MapLocationHandler(
                    mapViewModel = mapViewModel,
                    mapViewportState = mapViewportState,
                    puckBearingSource = puckBearingSource
                )

                // Camera logic riêng khi đang dẫn đường (Zoom sát hơn, nghiêng map)
                MapEffect(mapViewModel.isNavigating) {
                    if (mapViewModel.isNavigating) {
                        mapViewportState.transitionToFollowPuckState(
                            FollowPuckViewportStateOptions.Builder()
                                .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                .zoom(18.0)
                                .pitch(50.0) // Nghiêng 3D
                                .build()
                        )
                    }
                }
                // MapEffect tự động zoom bao quát lộ trình khi điểm đi/đến thay đổi
                MapEffect(mapViewModel.selectedDestination, mapViewModel.customOriginPoint) { mapView ->
                    if (mapViewModel.selectedDestination != null) {
                        val start = mapViewModel.customOriginPoint ?: mapViewModel.userLocationPoint
                        if (start != null) {
                            // Tính toán khung hình chứa cả điểm đi và điểm đến
                            val cameraOptions = mapView.mapboxMap.cameraForCoordinates(
                                listOf(start, mapViewModel.selectedDestination!!),
                                com.mapbox.maps.EdgeInsets(160.0, 100.0, 300.0, 100.0) // Padding: Trên, Trái, Dưới, Phải
                            )

                            // Di chuyển camera tới khung hình đó
                            mapView.mapboxMap.flyTo(cameraOptions)
                        }
                    }
                }
            }
        }

        // LAYER UI DẪN ĐƯỜNG (Overlay)
        if (mapViewModel.isNavigating) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. THANH CHỈ DẪN TRÊN CÙNG
                TurnByTurnOverlay(
                    navState = navigationState,
                    onCancelNavigation = { }
                )

                // 2. BẢNG ĐIỀU KHIỂN DƯỚI ĐÁY (GỌI HÀM MỚI TẠO)
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    NavigationBottomPanel(
                        navState = navigationState,
                        destinationName = mapViewModel.destinationName,
                        onCancel = {
                            // HÀNH ĐỘNG HỦY (CLICK)
                            mapViewModel.isNavigating = false
                            mapViewModel.selectedDestination = null
                            mapViewModel.routeInfo = null
                            mapboxNavigation.setNavigationRoutes(emptyList())
                            Toast.makeText(context, "Đã hủy chuyến đi", Toast.LENGTH_SHORT).show()
                        },
                        onSave = {
                            // HÀNH ĐỘNG LƯU (LONG CLICK)
                            onFinishTrip() // Gọi hàm lưu chuyến đi đã viết sẵn
                        }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 180.dp, end = 16.dp), // Canh vị trí nằm trên bảng điều khiển một chút
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        // KÍCH HOẠT LẠI CHẾ ĐỘ TRACKING LIÊN TỤC
                        mapViewportState.transitionToFollowPuckState(
                            FollowPuckViewportStateOptions.Builder()
                                .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                .zoom(18.0)
                                .pitch(50.0)
                                .build()
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Về giữa")
                }
            }
        }
        // --- UI CŨ (Nút bấm, Style Selector) ---
        else if (permissionsGranted) {
            // Đặt Search Bar ở đây để nó nằm trên bản đồ
            RouteSearchBox(
                isExpanded = shouldShowExpandedUI,
                onExpandRequest = { mapViewModel.isSearching = true },
                originAddress = mapViewModel.originName,
                destinationAddress = mapViewModel.destinationName,
                onOriginSelected = { point, name ->
                    // 1. Cập nhật điểm đi
                    mapViewModel.customOriginPoint = point // Nếu point = null nghĩa là user chọn "Vị trí của bạn"
                    mapViewModel.originName = name // Cập nhật tên ngay khi chọn từ list
                    if (name == "Vị trí của bạn") mapViewModel.originName = "Vị trí của bạn"

                    // 2. Nếu đã có điểm đến -> Tự động vẽ đường lại
                    if (mapViewModel.selectedDestination != null) {
                        val start = point ?: mapViewModel.userLocationPoint
                        requestCyclingRoute(context, mapboxNavigation, start, mapViewModel.selectedDestination!!) { routes ->
                            updateRouteInfo(routes)
                        }
                    }
                },
                onDestinationSelected = { point, name ->
                    // Cập nhật điểm đến
                    mapViewModel.selectedDestination = point
                    mapViewModel.destinationName = name ?: "" // Cập nhật tên ngay khi chọn từ list (hoặc rỗng nếu xóa)
                    if (point == null) {
                        // TRƯỜNG HỢP XÓA (BẤM X):
                        mapViewModel.routeInfo = null // 1. Xóa thông tin khoảng cách/thời gian
                        mapboxNavigation.setNavigationRoutes(emptyList()) // 2. Xóa đường vẽ trên map
                        //Tắt chế độ đang tìm kiếm -> UI sẽ tự thu gọn lại
                        mapViewModel.isSearching = false
                        mapViewModel.categoryResults = emptyList()
                    } else {
                        // Khi chọn được điểm -> Tắt chế độ tìm, nhưng UI vẫn Expand do (selectedDestination != null)
                        mapViewModel.isSearching = false
                        // Cũng nên xóa marker kết quả category nếu người dùng chọn từ autocomplete
                        mapViewModel.categoryResults = emptyList()
                        // TRƯỜNG HỢP CHỌN ĐIỂM MỚI:
                        val start = mapViewModel.customOriginPoint ?: mapViewModel.userLocationPoint
                        requestCyclingRoute(context, mapboxNavigation, start, point) { routes ->
                            updateRouteInfo(routes)
                        }
                    }
                },
                // Xử lý khi chọn danh mục
                onCategorySelected = { categoryQuery ->
                    // 1. Reset các trạng thái cũ
                    mapViewModel.selectedDestination = null
                    mapViewModel.routeInfo = null
                    mapboxNavigation.setNavigationRoutes(emptyList())

                    // 2. Gọi hàm tìm kiếm
                    searchCategoryNearby(categoryQuery)
                },
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = if (shouldShowExpandedUI) 200.dp else 120.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {

                AIScheduleFloatingButton(
                    mapViewModel = mapViewModel,
                    navController = navController
                )
                // Nút Profile
                FloatingActionButton(
                    onClick = { navController.navigate("profile") },
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Person, "Hồ sơ cá nhân", modifier = Modifier.size(24.dp))
                }

                // Nút chọn lớp bản đồ
                FloatingActionButton(
                    onClick = { showStyleSheet = true },
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.layers), contentDescription = "Chọn lớp bản đồ", modifier = Modifier.size(24.dp))
                }

                // Nút định vị / La bàn
                FloatingActionButton(
                    onClick = {
                        puckBearingSource = if (puckBearingSource == PuckBearing.HEADING) PuckBearing.COURSE else PuckBearing.HEADING
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    if (puckBearingSource == PuckBearing.HEADING) Icon(painter = painterResource(id = R.drawable.direction), contentDescription = "La bàn", modifier = Modifier.size(24.dp))
                    else Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Vị trí", modifier = Modifier.size(24.dp))
                }
            }
            // XỬ LÝ HIỂN THỊ THÔNG TIN TUYẾN ĐƯỜNG
            if (mapViewModel.selectedDestination != null && mapViewModel.routeInfo != null) {

                // TRƯỜNG HỢP 1: Đi từ vị trí hiện tại -> HIỆN NÚT START
                if (mapViewModel.customOriginPoint == null) {
                    ExtendedFloatingActionButton(
                        onClick = { mapViewModel.isNavigating = true },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(32.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.PlayArrow, "Start")
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Bắt đầu", fontWeight = FontWeight.Bold)
                            // Hiện luôn thời gian trên nút
                            Text(
                                text = "${formatDuration(mapViewModel.routeInfo!!.durationSeconds)} • ${formatDistance(mapViewModel.routeInfo!!.distanceMeters)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                // TRƯỜNG HỢP 2: Điểm đi Tùy chỉnh -> HIỆN BOTTOM SHEET PREVIEW
                else {
                    RoutePreviewBottomSheet(
                        routeInfo = mapViewModel.routeInfo!!,
                        weather = mapViewModel.weatherAtDestination,
                        originName = mapViewModel.originName,
                        destinationName = mapViewModel.destinationName,
                        navController = navController,
                        mapViewModel = mapViewModel,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
            // Nếu đang mở rộng thanh search nhưng chưa chọn điểm -> Thu gọn lại
            BackHandler(enabled = mapViewModel.isSearching && mapViewModel.selectedDestination == null) {
                mapViewModel.isSearching = false
            }
        }

        // --- BOTTOM SHEET ---
        if (showStyleSheet) {
            ModalBottomSheet(onDismissRequest = { showStyleSheet = false }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    mapStyles.forEach { style ->
                        StyleOptionItem(style, mapViewModel.currentStyleUri == style.styleUri) {
                            mapViewModel.currentStyleUri = style.styleUri
                            showStyleSheet = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIScheduleFloatingButton(
    mapViewModel: MapViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var isLoadingAI by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = {
            // 1. Kiểm tra xem đang bận tải không
            if (isLoadingAI) return@FloatingActionButton

            // 2. Kiểm tra xem đã chọn đường chưa
            val routeInfo = mapViewModel.routeInfo
            val startPoint = mapViewModel.customOriginPoint ?: mapViewModel.userLocationPoint
            val endPoint = mapViewModel.selectedDestination

            if (routeInfo == null || startPoint == null || endPoint == null) {
                Toast.makeText(context, "Vui lòng chọn điểm đến trước để lập lịch!", Toast.LENGTH_SHORT).show()
                return@FloatingActionButton
            }

            if (routeInfo.distanceMeters < 10000) {
                Toast.makeText(context, "Quãng đường quá ngắn để lập lịch (<10km)!", Toast.LENGTH_LONG).show()
                return@FloatingActionButton // Dừng lại, không gọi AI
            }

            // 3. Bắt đầu gọi AI
            isLoadingAI = true
            mapViewModel.getOrFetchJourneyPlan(
                originPoint = startPoint,
                originName = mapViewModel.originName,
                destPoint = endPoint,
                destName = mapViewModel.destinationName,
                distanceMeters = routeInfo.distanceMeters,
                userWeight = 70.0
            ) { result ->
                isLoadingAI = false
                // Chuyển sang màn hình PlanScreen
                navController.currentBackStackEntry?.savedStateHandle?.set("ai_plan_result", result)
                navController.navigate("plan_screen")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface, // Màu nền trắng giống các nút khác
        contentColor = MaterialTheme.colorScheme.primary,   // Màu icon
        modifier = Modifier.size(48.dp) // Kích thước chuẩn
    ) {
        if (isLoadingAI) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Lập lịch AI",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Composable con để vẽ từng ô chọn kiểu bản đồ (cho gọn code chính)
@Composable
fun StyleOptionItem(
    item: MapStyleItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null // Tạm thời tắt hiệu ứng ripple để tránh crash
            ) { onClick() }
    ) {
        // Ô hình ảnh/Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tên kiểu bản đồ
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Hàm tiện ích: Dịch hành động sang tiếng Việt
fun getManeuverTranslation(modifier: String?, type: String?): String {
    if (modifier == null && type == null) return "Đi tiếp"

    val mod = modifier?.lowercase() ?: ""
    val typ = type?.lowercase() ?: ""

    return when {
        mod.contains("left") -> "Rẽ trái"
        mod.contains("right") -> "Rẽ phải"
        mod.contains("slight left") -> "Chếch trái"
        mod.contains("slight right") -> "Chếch phải"
        mod.contains("uturn") -> "Quay đầu"
        mod.contains("straight") -> "Đi thẳng"
        typ.contains("roundabout") -> "Vòng xuyến"
        typ.contains("arrive") -> "Đến nơi"
        else -> "Đi tiếp" // Mặc định nếu không khớp
    }
}

@Composable
fun RoutePreviewBottomSheet(
    routeInfo: RouteInfo,
    weather: WeatherResponse?,
    originName: String,
    destinationName: String,
    navController: NavController,
    mapViewModel: MapViewModel,
    onStartNavigation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State quản lý trạng thái loading
    var isLoadingAI by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 18.dp, topEnd = 24.dp)),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .navigationBarsPadding() // Tránh bị che bởi thanh điều hướng Android
        ) {
            // --- HEADER: Thanh nắm kéo nhỏ (Visual cue) ---
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.LightGray, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- NỘI DUNG CHÍNH: CHIA 2 CỘT ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Đẩy sang 2 bên
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CỘT TRÁI: Thời gian & Khoảng cách
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thời gian dự kiến",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )

                    // Thời gian (Lớn, Xanh)
                    Text(
                        text = formatDuration(routeInfo.durationSeconds),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF0F9D58), // Google Green
                        fontWeight = FontWeight.Bold
                    )

                    // Khoảng cách (Nhỏ hơn)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDistance(routeInfo.distanceMeters),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // CỘT PHẢI: Weather
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    if (weather != null) {
                        WeatherWidget(weather = weather)
                    }
                }
            }
        }
    }
}

// Hàm format giây -> phút/giờ
fun formatDuration(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) "$hours giờ $remainingMinutes phút" else "$minutes phút"
}

// Hàm format mét -> km
fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        String.format("%.1f km", meters / 1000)
    } else {
        "${meters.toInt()} m"
    }
}

@Composable
fun WeatherWidget(weather: WeatherResponse) {
    val iconCode = weather.weather.firstOrNull()?.icon ?: "01d"
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@4x.png"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = Color(0xFFE3F2FD), // Nền xanh nhạt của cả cụm
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${weather.main.temp.toInt()}°C",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // [GIẢI PHÁP MỚI: DÙNG SUBCOMPOSE ĐỂ KIỂM SOÁT]
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(iconUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Weather Icon",
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, androidx.compose.foundation.shape.CircleShape) // Nền trắng
                .padding(4.dp) // Padding để icon nằm gọn trong vòng trắng
        ) {
            val state = painter.state
            if (state is coil.compose.AsyncImagePainter.State.Loading || state is coil.compose.AsyncImagePainter.State.Error) {
                Icon(
                    imageVector = Icons.Default.Warning, // Icon cảnh báo nếu lỗi
                    contentDescription = null,
                    tint = Color.Gray
                )
            } else {
                // Tải thành công -> Vẽ ảnh GỐC, KHÔNG FILTER
                SubcomposeAsyncImageContent(
                    colorFilter = null //  Đảm bảo không tô màu
                )
            }
        }
    }
}