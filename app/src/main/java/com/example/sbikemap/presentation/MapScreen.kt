package com.example.sbikemap.presentation

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import com.example.sbikemap.R
import com.example.sbikemap.utils.RouteRenderer
import com.example.sbikemap.utils.UserMarker
import com.example.sbikemap.utils.bitmapFromDrawableRes
import com.example.sbikemap.utils.requestCyclingRoute
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
// --- Navigation SDK Imports ---
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.MapboxOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ResponseInfo
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.tripdata.maneuver.model.Maneuver
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchOptions
import com.mapbox.search.discover.Discover
import com.mapbox.search.discover.DiscoverOptions
import com.mapbox.search.discover.DiscoverQuery
import com.mapbox.search.discover.DiscoverResult
import kotlinx.coroutines.launch

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
fun MapScreen(permissionsGranted: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val START_POINT = Point.fromLngLat(105.8544, 21.0285)
    val DESTINATION_ICON_ID = "DESTINATION_PIN_ICON"
    val START_ICON_ID = "START_PIN_ICON"

    // State Variables
    var puckBearingSource by remember { mutableStateOf(PuckBearing.HEADING) }
    var currentStyleUri by remember { mutableStateOf(Style.MAPBOX_STREETS) }
    var showStyleSheet by remember { mutableStateOf(false) }
    var isFirstLocate by remember { mutableStateOf(true) }
    var userLocationPoint by remember { mutableStateOf<Point?>(null) }
    var selectedDestination by remember { mutableStateOf<Point?>(null) }
    var customOriginPoint by remember { mutableStateOf<Point?>(null) }
    var routeInfo by remember { mutableStateOf<RouteInfo?>(null) }
    // Logic tính toán điểm bắt đầu thực tế
    val actualStartPoint = customOriginPoint ?: userLocationPoint

    // --- STATE CHO NAVIGATION UI ---
    var isNavigating by remember { mutableStateOf(false) }
    var navigationState by remember { mutableStateOf(NavigationState()) }
    // State lưu TÊN ĐỊA ĐIỂM để hiển thị lên ô nhập
    var originName by remember { mutableStateOf("Vị trí của bạn") }
    var destinationName by remember { mutableStateOf("") }

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

    // 2. [ĐÃ SỬA] Khởi tạo SearchEngine với Settings mặc định (Không truyền Token vào đây)
    val searchEngine = remember {
        SearchEngine.createSearchEngine(
            SearchEngineSettings()
        )
    }
    //State theo dõi việc người dùng có đang chủ động nhấn vào thanh search để tìm không
    var isSearching by remember { mutableStateOf(false) }
    //Logic quyết định khi nào thì hiện giao diện 2 ô (Expanded)
    val shouldShowExpandedUI = isSearching || selectedDestination != null || customOriginPoint != null
    var categoryResults by remember { mutableStateOf<List<DiscoverResult>>(emptyList()) } //State lưu danh sách kết quả tìm kiếm theo danh mục
    val SEARCH_RESULT_ICON_ID = "SEARCH_RESULT_ICON" // ID cho icon kết quả tìm kiếm
    // State để lưu tham chiếu bản đồ dùng cho việc tính toán camera sau này
    var mapboxMapInstance by remember { mutableStateOf<com.mapbox.maps.MapboxMap?>(null) }
    val currentCategoryResults by rememberUpdatedState(categoryResults)

    fun reverseGeocode(point: Point, isOrigin: Boolean) {
        if (isOrigin) originName = "Đang lấy địa chỉ..." else destinationName = "Đang lấy địa chỉ..."

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

                if (isOrigin) originName = address else destinationName = address
            }

            // Hàm trả về lỗi
            override fun onError(e: Exception) {
                if (isOrigin) originName = "Vị trí đã chọn" else destinationName = "Vị trí đã chọn"
            }
        })
    }

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
            MapboxNavigationProvider.create(NavigationOptions.Builder(context).build())
        }
    }

    // Hàm tìm kiếm theo danh mục (Nearby Search)
    fun searchCategoryNearby(categoryQuery: String) {
        // 1. Kiểm tra vị trí hiện tại
        val center = userLocationPoint
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
                categoryResults = results

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
                .routeLineBelowLayerId("mapbox-location-indicator-layer")
                .build()
        )
    }

    // Xử lý nút Back khi đang dẫn đường
    BackHandler(enabled = isNavigating) {
        isNavigating = false
        selectedDestination = null
        routeInfo = null
        mapboxNavigation.setNavigationRoutes(emptyList())
        mapboxNavigation.mapboxReplayer.stop()
    }

    DisposableEffect(Unit) {
        onDispose { MapboxNavigationProvider.destroy() }
    }

    // --- LOGIC LẮNG NGHE TIẾN TRÌNH DẪN ĐƯỜNG ---
    DisposableEffect(isNavigating) {
        if (isNavigating) {
            val progressObserver = RouteProgressObserver { routeProgress ->
                // 1. Lấy Expected (chứa List<Maneuver> hoặc Lỗi)
                val maneuversExpected = maneuverApi.getManeuvers(routeProgress)

                // 2. [SỬA LỖI QUAN TRỌNG]
                // Thay vì trả về Maneuver (có thể null), ta trả về List (luôn không null)
                // để thỏa mãn @NonNull của hàm fold.
                val maneuversList = maneuversExpected.fold(
                    { emptyList<Maneuver>() }, // Nếu lỗi -> Trả về list rỗng
                    { it }                     // Nếu OK -> Trả về list kết quả
                )

                // 3. Lấy phần tử đầu tiên một cách an toàn
                val currentManeuver = maneuversList.firstOrNull()

                // 4. Format dữ liệu
                val distanceStr = distanceRemainingFormatter.format(routeProgress.distanceRemaining.toDouble())
                val timeStr = timeRemainingFormatter.format(routeProgress.durationRemaining)

                // 5. Cập nhật UI State
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

            mapboxNavigation.registerRouteProgressObserver(progressObserver)
            mapboxNavigation.startTripSession()

            onDispose {
                mapboxNavigation.unregisterRouteProgressObserver(progressObserver)
                mapboxNavigation.stopTripSession()
            }
        } else {
            onDispose { }
        }
    }

    // Danh sách kiểu bản đồ
    val mapStyles = listOf(
        MapStyleItem("Phố", Style.MAPBOX_STREETS, Icons.Default.Build),
        MapStyleItem("Vệ tinh", Style.SATELLITE_STREETS, Icons.Default.Build),
        MapStyleItem("Địa hình", Style.OUTDOORS, Icons.Default.Build),
        MapStyleItem("Tối", Style.TRAFFIC_NIGHT, Icons.Default.Build)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // --- BẢN ĐỒ ---
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = currentStyleUri) }
        ) {
            MapEffect(Unit) { mapView ->
                mapboxMapInstance = mapView.mapboxMap
            }
            if (selectedDestination != null) {
                UserMarker(point = selectedDestination!!, iconId = DESTINATION_ICON_ID)
            }
            if (customOriginPoint != null) {
                UserMarker(point = customOriginPoint!!, iconId = START_ICON_ID)
            }
            // VẼ MARKER CHO CÁC KẾT QUẢ TÌM ĐƯỢC
            if (categoryResults.isNotEmpty()) {
                categoryResults.forEach { result ->
                    result.coordinate?.let { point ->
                        UserMarker(point = point, iconId = SEARCH_RESULT_ICON_ID)
                    }
                }
            }

            if (permissionsGranted) {
                MapEffect(currentStyleUri) { mapView ->
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
                if (!isNavigating) {
                    MapEffect(Unit) { mapView ->
                        mapView.mapboxMap.addOnMapClickListener { clickedPoint ->

                            // Lấy danh sách kết quả MỚI NHẤT từ biến rememberUpdatedState
                            val results = currentCategoryResults

                            // TRƯỜNG HỢP 1: ĐANG HIỂN THỊ DANH SÁCH KẾT QUẢ (MARKERS)
                            if (results.isNotEmpty()) {
                                // Tính toán va chạm dựa trên PIXEL màn hình (Chính xác hơn độ)
                                val clickScreenPoint = mapView.mapboxMap.pixelForCoordinate(clickedPoint)
                                val touchRadiusPx = 70.0 // Bán kính chạm khoảng 50 pixel (ngón tay)

                                val matchedResult = results.find { result ->
                                    // Chuyển tọa độ Marker thành tọa độ màn hình (x, y)
                                    val markerScreenPoint = mapView.mapboxMap.pixelForCoordinate(result.coordinate)

                                    // Tính khoảng cách giữa ngón tay và Marker
                                    val dx = clickScreenPoint.x - markerScreenPoint.x
                                    val dy = clickScreenPoint.y - markerScreenPoint.y
                                    val distance = Math.sqrt(dx * dx + dy * dy)

                                    // Nếu khoảng cách < 50px -> Coi như đã click trúng
                                    distance < touchRadiusPx
                                }

                                if (matchedResult != null) {
                                    // A. Click TRÚNG Marker kết quả
                                    selectedDestination = matchedResult.coordinate // Lấy tọa độ chính xác của quán
                                    destinationName = matchedResult.name // Lấy đúng tên quán
                                    Toast.makeText(context, "Đã chọn: ${matchedResult.name}", Toast.LENGTH_SHORT).show()

                                    // Vẽ đường ngay lập tức
                                    val start = customOriginPoint ?: userLocationPoint
                                    requestCyclingRoute(context, mapboxNavigation, start, matchedResult.coordinate) { dist, dur ->
                                        routeInfo = RouteInfo(dist, dur)
                                    }

                                    // KHÔNG XÓA categoryResults ĐỂ USER CÓ THỂ CHỌN ĐIỂM KHÁC
                                    // categoryResults = emptyList() <--- Đã bỏ dòng này theo ý bạn
                                } else {
                                    // B. Click TRƯỢT ra ngoài khoảng trống khi đang hiện list
                                    // -> Không làm gì cả (Return false) để tránh chọn nhầm điểm lung tung
                                    return@addOnMapClickListener false
                                }
                            }
                            // TRƯỜNG HỢP 2: KHÔNG CÓ MARKER (CLICK BẢN ĐỒ BÌNH THƯỜNG)
                            else {
                                // Nếu đã có đích đến (đang hiện BottomSheet) thì chặn click (phải bấm X mới chọn lại được)
                                if (selectedDestination != null) {
                                    return@addOnMapClickListener false
                                }

                                // Chọn điểm bất kỳ trên bản đồ
                                selectedDestination = clickedPoint
                                reverseGeocode(clickedPoint, isOrigin = false) // Lấy tên đường
                                Toast.makeText(context, "Đã chọn điểm trên bản đồ", Toast.LENGTH_SHORT).show()

                                // Vẽ đường
                                val start = customOriginPoint ?: userLocationPoint
                                requestCyclingRoute(context, mapboxNavigation, start, clickedPoint) { dist, dur ->
                                    routeInfo = RouteInfo(dist, dur)
                                }
                            }
                            true
                        }
                    }
                }

                // Camera & Location Logic
                MapEffect(puckBearingSource) { mapView ->
                    mapView.location.updateSettings {
                        enabled = true
                        locationPuck = createDefault2DPuck(withBearing = true)
                        pulsingEnabled = true
                        puckBearing = puckBearingSource
                        puckBearingEnabled = true
                    }
                    if (isFirstLocate) {
                        val listener = object : OnIndicatorPositionChangedListener {
                            override fun onIndicatorPositionChanged(point: Point) {
                                mapView.location.removeOnIndicatorPositionChangedListener(this)
                                userLocationPoint = point
                                mapView.mapboxMap.setCamera(CameraOptions.Builder().center(point).zoom(10.0).build())
                                mapViewportState.transitionToFollowPuckState(
                                    FollowPuckViewportStateOptions.Builder()
                                        .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                        .zoom(16.0)
                                        .build()
                                )
                                isFirstLocate = false
                            }
                        }
                        mapView.location.addOnIndicatorPositionChangedListener(listener)
                    } else {
                        // Buộc Camera bám theo User + Cập nhật chế độ quay (Heading/Course)
                        mapViewportState.transitionToFollowPuckState(
                            FollowPuckViewportStateOptions.Builder()
                                .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                .zoom(16.0) // Giữ mức zoom cận cảnh
                                .build()
                        )
                    }
                }

                // Camera logic riêng khi đang dẫn đường (Zoom sát hơn, nghiêng map)
                MapEffect(isNavigating) {
                    if (isNavigating) {
                        mapViewportState.transitionToFollowPuckState(
                            FollowPuckViewportStateOptions.Builder()
                                .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                .zoom(18.0)
                                .pitch(50.0) // Nghiêng 3D
                                .build()
                        )
                    }
                }
                // [THÊM MỚI] MapEffect tự động zoom bao quát lộ trình khi điểm đi/đến thay đổi
                MapEffect(selectedDestination, customOriginPoint) { mapView ->
                    if (selectedDestination != null) {
                        val start = customOriginPoint ?: userLocationPoint
                        if (start != null) {
                            // Tính toán khung hình chứa cả điểm đi và điểm đến
                            val cameraOptions = mapView.mapboxMap.cameraForCoordinates(
                                listOf(start, selectedDestination!!),
                                com.mapbox.maps.EdgeInsets(160.0, 100.0, 300.0, 100.0) // Padding: Trên, Trái, Dưới, Phải
                            )

                            // Di chuyển camera tới khung hình đó
                            mapView.mapboxMap.flyTo(cameraOptions)
                        }
                    }
                }
            }
        }

        // --- LAYER UI DẪN ĐƯỜNG (Overlay) ---
        if (isNavigating) {
            TurnByTurnOverlay(
                navState = navigationState,
                onCancelNavigation = {
                    isNavigating = false
                    selectedDestination = null
                    routeInfo = null
                    mapboxNavigation.setNavigationRoutes(emptyList())
                }
            )
        }
        // --- UI CŨ (Nút bấm, Style Selector) ---
        else if (permissionsGranted) {
            // Đặt Search Bar ở đây để nó nằm trên bản đồ
            RouteSearchBox(
                isExpanded = shouldShowExpandedUI,
                onExpandRequest = { isSearching = true },
                originAddress = originName,
                destinationAddress = destinationName,
                onOriginSelected = { point, name ->
                    // 1. Cập nhật điểm đi
                    customOriginPoint = point // Nếu point = null nghĩa là user chọn "Vị trí của bạn"
                    originName = name // Cập nhật tên ngay khi chọn từ list
                    if (name == "Vị trí của bạn") originName = "Vị trí của bạn"

                    // 2. Nếu đã có điểm đến -> Tự động vẽ đường lại
                    if (selectedDestination != null) {
                        val start = point ?: userLocationPoint
                        requestCyclingRoute(context, mapboxNavigation, start, selectedDestination!!) { dist, dur ->
                            routeInfo = RouteInfo(dist, dur)
                        }
                    }
                },
                onDestinationSelected = { point, name ->
                    // Cập nhật điểm đến
                    selectedDestination = point
                    destinationName = name ?: "" // Cập nhật tên ngay khi chọn từ list (hoặc rỗng nếu xóa)
                    if (point == null) {
                        // TRƯỜNG HỢP XÓA (BẤM X):
                        routeInfo = null // 1. Xóa thông tin khoảng cách/thời gian
                        mapboxNavigation.setNavigationRoutes(emptyList()) // 2. Xóa đường vẽ trên map
                        //Tắt chế độ đang tìm kiếm -> UI sẽ tự thu gọn lại
                        isSearching = false
                        categoryResults = emptyList()
                    } else {
                        // Khi chọn được điểm -> Tắt chế độ tìm, nhưng UI vẫn Expand do (selectedDestination != null)
                        isSearching = false
                        // Cũng nên xóa marker kết quả category nếu người dùng chọn từ autocomplete
                        categoryResults = emptyList()
                        // TRƯỜNG HỢP CHỌN ĐIỂM MỚI:
                        val start = customOriginPoint ?: userLocationPoint
                        requestCyclingRoute(context, mapboxNavigation, start, point) { dist, dur ->
                            routeInfo = RouteInfo(dist, dur)
                        }
                    }
                },
                // Xử lý khi chọn danh mục
                onCategorySelected = { categoryQuery ->
                    // 1. Reset các trạng thái cũ
                    selectedDestination = null
                    routeInfo = null
                    mapboxNavigation.setNavigationRoutes(emptyList())

                    // 2. Gọi hàm tìm kiếm
                    searchCategoryNearby(categoryQuery)
                },
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 190.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showStyleSheet = true },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.Edit, "Chọn lớp bản đồ")
                }
            }

            // Nút BẮT ĐẦU (Chỉ hiện khi đã chọn đích)
            if (selectedDestination != null) {
                ExtendedFloatingActionButton(
                    onClick = { isNavigating = true }, // Kích hoạt UI dẫn đường
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(32.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.PlayArrow, "Start")
                    Spacer(Modifier.width(8.dp))
                    Text("Bắt đầu")
                }
            }

            FloatingActionButton(
                onClick = {
                    puckBearingSource = if (puckBearingSource == PuckBearing.HEADING) PuckBearing.COURSE else PuckBearing.HEADING
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (routeInfo != null && customOriginPoint != null) 200.dp else 32.dp, end = 32.dp) // Đẩy nút lên nếu có BottomSheet
            ) {
                Icon(
                    if (puckBearingSource == PuckBearing.HEADING) Icons.Default.KeyboardArrowUp else Icons.Default.LocationOn,
                    "La bàn"
                )
            }
            // 4. [LOGIC UI MỚI] XỬ LÝ HIỂN THỊ THÔNG TIN TUYẾN ĐƯỜNG
            if (selectedDestination != null && routeInfo != null) {

                // TRƯỜNG HỢP 1: Đi từ vị trí hiện tại -> HIỆN NÚT START
                if (customOriginPoint == null) {
                    ExtendedFloatingActionButton(
                        onClick = { isNavigating = true },
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
                                text = "${formatDuration(routeInfo!!.durationSeconds)} • ${formatDistance(routeInfo!!.distanceMeters)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                // TRƯỜNG HỢP 2: Điểm đi Tùy chỉnh -> HIỆN BOTTOM SHEET PREVIEW
                else {
                    RoutePreviewBottomSheet(
                        routeInfo = routeInfo!!,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
            // Nếu đang mở rộng thanh search nhưng chưa chọn điểm -> Thu gọn lại
            BackHandler(enabled = isSearching && selectedDestination == null) {
                isSearching = false
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
                        StyleOptionItem(style, currentStyleUri == style.styleUri) {
                            currentStyleUri = style.styleUri
                            showStyleSheet = false
                        }
                    }
                }
            }
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Tiêu đề
            Text("Thông tin tuyến đường", style = MaterialTheme.typography.titleMedium, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                // Thời gian (Màu xanh lá)
                Text(
                    text = formatDuration(routeInfo.durationSeconds),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF0F9D58), // Google Green
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Khoảng cách (Màu xám)
                Text(
                    text = "(${formatDistance(routeInfo.distanceMeters)})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút "Xem trước" (Giả lập) hoặc chỉ dẫn
            Button(
                onClick = { /* Có thể làm tính năng xem từng bước */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Không thể dẫn đường từ vị trí này", color = Color.Black)
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