package com.example.sbikemap.presentation

/*import android.widget.Toast
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions

data class MapStyleItem(
    val name: String,
    val styleUri: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(permissionsGranted: Boolean) {
    // Tọa độ mẫu (Hà Nội)
    val START_POINT = Point.fromLngLat(105.8544, 21.0285)

    // Tạo biến trạng thái để lưu chế độ Bearing (Mặc định là HEADING để test xoay điện thoại)
    var puckBearingSource by remember { mutableStateOf(PuckBearing.HEADING) }

    // Kiểu bản đồ hiện tại (Mặc định là Streets)
    var currentStyleUri by remember { mutableStateOf(Style.MAPBOX_STREETS) }

    // Ẩn/Hiện Bottom Sheet chọn kiểu bản đồ
    var showStyleSheet by remember { mutableStateOf(false) }

    // Biến trạng thái để kiểm soát việc chỉ zoom lần đầu tiên
    var isFirstLocate by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val MARKER_ICON_ID = "USER_PIN_ICON"
    var userLocationPoint by remember { mutableStateOf<Point?>(null) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(10.0)
            center(START_POINT)
            pitch(0.0)
            bearing(0.0)
        }
    }

    // Khởi tạo MapboxNavigation (Singleton lifecycle)
    val mapboxNavigation = remember {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(context).build()
            )
        }
    }

    // API và View để vẽ tuyến đường (Route Line)
    val routeLineApi = remember {
        MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    }
    val routeLineView = remember {
        MapboxRouteLineView(
            MapboxRouteLineViewOptions.Builder(context)
                // Đặt tuyến đường nằm dưới layer của biểu tượng vị trí (Puck)
                .routeLineBelowLayerId("mapbox-location-indicator-layer")
                .build()
        )
    }

    // Quản lý vòng đời Navigation (Cleanup khi thoát màn hình)
    DisposableEffect(Unit) {
        onDispose {
            // Lưu ý: Tùy vào logic app, bạn có thể muốn giữ lại Navigation instance
            // hoặc destroy nó. Ở đây ta destroy để sạch memory theo hướng dẫn.
            MapboxNavigationProvider.destroy()
        }
    }

    // Danh sách kiểu bản đồ
    val mapStyles = listOf(
        MapStyleItem("Phố", Style.MAPBOX_STREETS, Icons.Default.Build),
        MapStyleItem("Vệ tinh", Style.SATELLITE_STREETS, Icons.Default.Build),
        MapStyleItem("Địa hình", Style.OUTDOORS, Icons.Default.Build),
        MapStyleItem("Tối", Style.TRAFFIC_NIGHT, Icons.Default.Build)
    )

    // Sử dụng Box để đặt nút bấm đè lên bản đồ
    Box(modifier = Modifier.fillMaxSize()) {

        // --- BẢN ĐỒ ---
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = currentStyleUri) }
        ) {
            // Chỉ hiển thị khi đã có tọa độ
            if (userLocationPoint != null) {
                UserMarker(
                    point = userLocationPoint!!,
                    iconId = MARKER_ICON_ID // Truyền ID (String) thay vì Bitmap
                ) {
                    // Sự kiện click vào marker (nếu cần)
                }
            }
            if (permissionsGranted) {
                // Effect nạp ảnh vào Style (Chạy mỗi khi đổi kiểu bản đồ)
                MapEffect(currentStyleUri) { mapView ->
                    mapView.mapboxMap.getStyle { style ->
                        // Nếu Style chưa có ảnh này thì nạp vào
                        if (style.getStyleImage(MARKER_ICON_ID) == null) {
                            val bitmap = bitmapFromDrawableRes(context, R.drawable.location)
                            if (bitmap != null) {
                                style.addImage(MARKER_ICON_ID, bitmap)
                            }
                        }
                    }
                }

                // GỌI COMPOSABLE VẼ TUYẾN ĐƯỜNG
                RouteRenderer(mapboxNavigation, routeLineApi, routeLineView)

                // Xử lý Click Bản đồ
                MapEffect(Unit) { mapView ->
                    mapView.mapboxMap.addOnMapClickListener { destination ->
                        // Gọi hàm tìm đường
                        requestCyclingRoute(context, mapboxNavigation, userLocationPoint, destination)

                        Toast.makeText(context, "Đang tìm đường...", Toast.LENGTH_SHORT).show()
                        true
                    }
                }
                // Truyền 'puckBearingSource' vào key của MapEffect.
                // Khi biến này thay đổi, khối code bên trong sẽ chạy lại để cập nhật cài đặt.
                MapEffect(puckBearingSource) { mapView ->

                    // Cập nhật cài đặt Location
                    mapView.location.updateSettings {
                        enabled = true
                        locationPuck = createDefault2DPuck(withBearing = true)
                        pulsingEnabled = true
                        // Cập nhật nguồn hướng dựa trên biến state
                        puckBearing = puckBearingSource
                    }

                    // LOGIC TẠO HIỆU ỨNG ZOOM-IN
                    if (isFirstLocate) {
                        val listener = object : OnIndicatorPositionChangedListener {
                            override fun onIndicatorPositionChanged(point: Point) {
                                // Ngắt listener ngay lập tức để không chạy lại
                                mapView.location.removeOnIndicatorPositionChangedListener(this)

                                // Cập nhật vị trí để vẽ Marker
                                userLocationPoint = point

                                // "Nhảy" ngay lập tức tới vị trí người dùng
                                mapView.mapboxMap.setCamera(
                                    CameraOptions.Builder()
                                        .center(point)
                                        .zoom(10.0)
                                        .build()
                                )

                                // 3. Sau đó mới chạy hiệu ứng Zoom vào (transition)
                                mapViewportState.transitionToFollowPuckState(
                                    followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                                        .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                        .zoom(16.0) // Zoom cận cảnh
                                        .build()
                                )

                                // Đánh dấu đã định vị xong
                                isFirstLocate = false
                            }
                        }
                        // Đăng ký listener
                        mapView.location.addOnIndicatorPositionChangedListener(listener)
                    }
                }

                // MapEffect riêng để cập nhật Bearing khi người dùng bấm nút
                // Tách riêng để tránh việc re-render làm reset lại hiệu ứng zoom đầu
                MapEffect(puckBearingSource) { mapView ->
                    if (!isFirstLocate) { // Chỉ update khi đã định vị xong
                        mapView.location.updateSettings {
                            puckBearing = puckBearingSource
                        }
                        mapViewportState.transitionToFollowPuckState(
                            followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                                .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                .zoom(16.0)
                                .build()
                        )
                    }
                }
            }
        }

        // B. CÁC NÚT ĐIỀU KHIỂN NỔI (FLOATING BUTTONS)
        if (permissionsGranted) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd) // Góc trên bên phải
                    .padding(top = 48.dp, end = 16.dp), // Cách lề để tránh thanh trạng thái
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Nút Mở bảng chọn lớp (Layers)
                FloatingActionButton(
                    onClick = { showStyleSheet = true },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Chọn lớp bản đồ")
                }
            }

            // 2. Nút Chuyển chế độ Bearing (Góc dưới phải)
            FloatingActionButton(
                onClick = {
                    puckBearingSource = if (puckBearingSource == PuckBearing.HEADING) {
                        PuckBearing.COURSE
                    } else {
                        PuckBearing.HEADING
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
            ) {
                if (puckBearingSource == PuckBearing.HEADING) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "La bàn")
                } else {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Dẫn đường")
                }
            }
        }

        // C. BOTTOM SHEET (Bảng chọn kiểu bản đồ)
        if (showStyleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStyleSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp) // Padding dưới để tránh mép màn hình
                ) {
                    Text(
                        text = "Loại bản đồ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                    )

                    // Hiển thị 4 lựa chọn theo hàng ngang
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        mapStyles.forEach { styleItem ->
                            StyleOptionItem(
                                item = styleItem,
                                isSelected = currentStyleUri == styleItem.styleUri,
                                onClick = {
                                    currentStyleUri = styleItem.styleUri
                                    showStyleSheet = false // Đóng bảng sau khi chọn
                                }
                            )
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
}*/

/*import android.content.Intent
import android.view.LayoutInflater
import android.view.View
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.sbikemap.R
import com.example.sbikemap.databinding.MapboxActivityTurnByTurnExperienceBinding
import com.example.sbikemap.utils.RouteRenderer
import com.example.sbikemap.utils.UserMarker
import com.example.sbikemap.utils.bitmapFromDrawableRes
import com.example.sbikemap.utils.requestCyclingRoute
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
// --- Navigation SDK Imports ---
import com.mapbox.api.directions.v5.models.RouteOptions
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

data class MapStyleItem(
    val name: String,
    val styleUri: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
fun MapScreen(permissionsGranted: Boolean) {
    // Tọa độ mẫu (Hà Nội)
    val START_POINT = Point.fromLngLat(105.8544, 21.0285)

    // Tạo biến trạng thái để lưu chế độ Bearing (Mặc định là HEADING để test xoay điện thoại)
    var puckBearingSource by remember { mutableStateOf(PuckBearing.HEADING) }

    // Kiểu bản đồ hiện tại (Mặc định là Streets)
    var currentStyleUri by remember { mutableStateOf(Style.MAPBOX_STREETS) }

    // Ẩn/Hiện Bottom Sheet chọn kiểu bản đồ
    var showStyleSheet by remember { mutableStateOf(false) }

    // Biến trạng thái để kiểm soát việc chỉ zoom lần đầu tiên
    var isFirstLocate by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val MARKER_ICON_ID = "USER_PIN_ICON"
    var userLocationPoint by remember { mutableStateOf<Point?>(null) }
    var selectedDestination by remember { mutableStateOf<Point?>(null) }
    var isNavigating by remember { mutableStateOf(false) }
    // --- KHỞI TẠO CÁC API HỖ TRỢ UI (Move từ Activity sang) ---
    val distanceFormatterOptions = remember { DistanceFormatterOptions.Builder(context).build() }

    val maneuverApi = remember {
        MapboxManeuverApi(MapboxDistanceFormatter(distanceFormatterOptions))
    }

    val tripProgressApi = remember {
        MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(context)
                .distanceRemainingFormatter(DistanceRemainingFormatter(distanceFormatterOptions))
                .timeRemainingFormatter(TimeRemainingFormatter(context))
                .percentRouteTraveledFormatter(PercentDistanceTraveledFormatter())
                .estimatedTimeToArrivalFormatter(EstimatedTimeToArrivalFormatter(context, TimeFormat.NONE_SPECIFIED))
                .build()
        )
    }

    // Biến lưu binding của XML overlay để cập nhật dữ liệu
    var navigationBinding by remember { mutableStateOf<MapboxActivityTurnByTurnExperienceBinding?>(null) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(10.0)
            center(START_POINT)
            pitch(0.0)
            bearing(0.0)
        }
    }

    // Khởi tạo MapboxNavigation (Singleton lifecycle)
    val mapboxNavigation = remember {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(context).build()
            )
        }
    }

    // API và View để vẽ tuyến đường (Route Line)
    val routeLineApi = remember {
        MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    }
    val routeLineView = remember {
        MapboxRouteLineView(
            MapboxRouteLineViewOptions.Builder(context)
                // Đặt tuyến đường nằm dưới layer của biểu tượng vị trí (Puck)
                .routeLineBelowLayerId("mapbox-location-indicator-layer")
                .build()
        )
    }
    // Nếu đang dẫn đường, nút Back sẽ tắt dẫn đường
    BackHandler(enabled = isNavigating) {
        isNavigating = false
        mapboxNavigation.setNavigationRoutes(emptyList()) // Xóa đường
        mapboxNavigation.mapboxReplayer.stop() // Dừng xe
    }

    // Quản lý vòng đời Navigation (Cleanup khi thoát màn hình)
    DisposableEffect(Unit) {
        onDispose {
            // Lưu ý: Tùy vào logic app, bạn có thể muốn giữ lại Navigation instance
            // hoặc destroy nó. Ở đây ta destroy để sạch memory theo hướng dẫn.
            MapboxNavigationProvider.destroy()
        }
    }

    // --- LOGIC LẮNG NGHE TIẾN TRÌNH (OBSERVER) ---
    DisposableEffect(isNavigating) {
        if (isNavigating) {
            val routeProgressObserver = RouteProgressObserver { routeProgress ->
                // 1. Cập nhật Camera bám theo xe (nếu cần)
                // viewportDataSource.onRouteProgressChanged(routeProgress) -> Cái này Compose tự lo bằng FollowPuckViewportState

                // 2. Cập nhật Banner hướng dẫn (Maneuver)
                val maneuvers = maneuverApi.getManeuvers(routeProgress)
                maneuvers.fold(
                    { }, // Handle error
                    {
                        navigationBinding?.maneuverView?.visibility = View.VISIBLE
                        navigationBinding?.maneuverView?.renderManeuvers(maneuvers)!!
                    }
                )

                // 3. Cập nhật Thanh tiến trình (Trip Progress)
                navigationBinding?.tripProgressCard?.visibility = View.VISIBLE
                navigationBinding?.tripProgressView?.render(
                    tripProgressApi.getTripProgress(routeProgress)
                )
            }

            // Đăng ký lắng nghe
            mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)

            // Cleanup khi dừng dẫn đường
            onDispose {
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
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

    // Sử dụng Box để đặt nút bấm đè lên bản đồ
    Box(modifier = Modifier.fillMaxSize()) {

        // --- BẢN ĐỒ ---
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = currentStyleUri) }
        ) {
            // Chỉ hiển thị khi đã có tọa độ
            if (userLocationPoint != null) {
                UserMarker(
                    point = userLocationPoint!!,
                    iconId = MARKER_ICON_ID // Truyền ID (String) thay vì Bitmap
                ) {
                    // Sự kiện click vào marker (nếu cần)
                }
            }
            if (permissionsGranted) {
                // Effect nạp ảnh vào Style (Chạy mỗi khi đổi kiểu bản đồ)
                MapEffect(currentStyleUri) { mapView ->
                    mapView.mapboxMap.getStyle { style ->
                        // Nếu Style chưa có ảnh này thì nạp vào
                        if (style.getStyleImage(MARKER_ICON_ID) == null) {
                            val bitmap = bitmapFromDrawableRes(context, R.drawable.location)
                            if (bitmap != null) {
                                style.addImage(MARKER_ICON_ID, bitmap)
                            }
                        }
                    }
                }

                // GỌI COMPOSABLE VẼ TUYẾN ĐƯỜNG
                RouteRenderer(mapboxNavigation, routeLineApi, routeLineView)

                // Xử lý Click Bản đồ
                MapEffect(Unit) { mapView ->
                    mapView.mapboxMap.addOnMapClickListener { destination ->
                        // Lưu điểm đến vào biến state
                        selectedDestination = destination
                        // Gọi hàm tìm đường
                        requestCyclingRoute(context, mapboxNavigation, userLocationPoint, destination)

                        Toast.makeText(context, "Đang tìm đường...", Toast.LENGTH_SHORT).show()
                        true
                    }
                }
                // Truyền 'puckBearingSource' vào key của MapEffect.
                // Khi biến này thay đổi, khối code bên trong sẽ chạy lại để cập nhật cài đặt.
                MapEffect(puckBearingSource) { mapView ->

                    // Cập nhật cài đặt Location
                    mapView.location.updateSettings {
                        enabled = true
                        locationPuck = createDefault2DPuck(withBearing = true)
                        pulsingEnabled = true
                        // Cập nhật nguồn hướng dựa trên biến state
                        puckBearing = puckBearingSource
                    }

                    // LOGIC TẠO HIỆU ỨNG ZOOM-IN
                    if (isFirstLocate) {
                        val listener = object : OnIndicatorPositionChangedListener {
                            override fun onIndicatorPositionChanged(point: Point) {
                                // Ngắt listener ngay lập tức để không chạy lại
                                mapView.location.removeOnIndicatorPositionChangedListener(this)

                                // Cập nhật vị trí để vẽ Marker
                                userLocationPoint = point

                                // "Nhảy" ngay lập tức tới vị trí người dùng
                                mapView.mapboxMap.setCamera(
                                    CameraOptions.Builder()
                                        .center(point)
                                        .zoom(10.0)
                                        .build()
                                )

                                // 3. Sau đó mới chạy hiệu ứng Zoom vào (transition)
                                mapViewportState.transitionToFollowPuckState(
                                    followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                                        .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                        .zoom(16.0) // Zoom cận cảnh
                                        .build()
                                )

                                // Đánh dấu đã định vị xong
                                isFirstLocate = false
                            }
                        }
                        // Đăng ký listener
                        mapView.location.addOnIndicatorPositionChangedListener(listener)
                    }
                }

                // MapEffect riêng để cập nhật Bearing khi người dùng bấm nút
                // Tách riêng để tránh việc re-render làm reset lại hiệu ứng zoom đầu
                MapEffect(puckBearingSource) { mapView ->
                    if (!isFirstLocate) { // Chỉ update khi đã định vị xong
                        mapView.location.updateSettings {
                            puckBearing = puckBearingSource
                        }
                        mapViewportState.transitionToFollowPuckState(
                            followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                                .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                .zoom(16.0)
                                .build()
                        )
                    }
                }
                MapEffect(isNavigating) {
                    if (isNavigating) {
                        mapViewportState.transitionToFollowPuckState(
                            FollowPuckViewportStateOptions.Builder()
                                .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                                .zoom(18.0) // Zoom sâu hơn khi dẫn đường
                                .pitch(50.0) // Nghiêng bản đồ cho giống 3D
                                .build()
                        )
                    }
                }
            }
        }
        // --- 2. LỚP GIAO DIỆN DẪN ĐƯỜNG (Overlay XML) ---
        // Chỉ hiện khi biến isNavigating = true
        if (isNavigating) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    // Nạp file XML đã sửa (chỉ còn nút bấm và banner)
                    val binding = MapboxActivityTurnByTurnExperienceBinding.inflate(LayoutInflater.from(ctx))
                    navigationBinding = binding // Lưu lại binding để cập nhật dữ liệu

                    // Xử lý sự kiện nút "Dừng" (dấu X)
                    binding.stop.setOnClickListener {
                        isNavigating = false
                        mapboxNavigation.setNavigationRoutes(emptyList()) // Xóa tuyến đường
                        mapboxNavigation.mapboxReplayer.stop() // Dừng giả lập
                        navigationBinding = null
                    }

                    binding.root
                },
                modifier = Modifier.fillMaxSize() // Phủ kín màn hình (nhưng trong suốt)
            )
        }

        // B. CÁC NÚT ĐIỀU KHIỂN NỔI (FLOATING BUTTONS)
        if (permissionsGranted && !isNavigating) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd) // Góc trên bên phải
                    .padding(top = 48.dp, end = 16.dp), // Cách lề để tránh thanh trạng thái
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Nút Mở bảng chọn lớp (Layers)
                FloatingActionButton(
                    onClick = { showStyleSheet = true },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Chọn lớp bản đồ")
                }
            }

            // --- NÚT BẮT ĐẦU DẪN ĐƯỜNG ---
            FloatingActionButton(
                onClick = {
                    // 1. HỦY MapboxNavigation hiện tại để tránh xung đột
                    if (MapboxNavigationProvider.isCreated()) {
                        MapboxNavigationProvider.destroy()
                    }

                    // 2. Sau đó mới mở màn hình mới
                    val intent = Intent(context, com.example.sbikemap.presentation.TurnByTurnExperienceActivity::class.java)
                    context.startActivity(intent)

                    // --- ĐÓNG GÓI DỮ LIỆU GỬI ĐI ---
                    userLocationPoint?.let {
                        intent.putExtra("origin_lon", it.longitude())
                        intent.putExtra("origin_lat", it.latitude())
                    }
                    selectedDestination?.let {
                        intent.putExtra("dest_lon", it.longitude())
                        intent.putExtra("dest_lat", it.latitude())
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .align(Alignment.BottomStart) // Đặt ở góc TRÁI dưới
                    .padding(32.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start Navigation")
            }

            // 2. Nút Chuyển chế độ Bearing (Góc dưới phải)
            FloatingActionButton(
                onClick = {
                    puckBearingSource = if (puckBearingSource == PuckBearing.HEADING) {
                        PuckBearing.COURSE
                    } else {
                        PuckBearing.HEADING
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
            ) {
                if (puckBearingSource == PuckBearing.HEADING) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "La bàn")
                } else {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Dẫn đường")
                }
            }
        }

        // C. BOTTOM SHEET (Bảng chọn kiểu bản đồ)
        if (showStyleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStyleSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp) // Padding dưới để tránh mép màn hình
                ) {
                    Text(
                        text = "Loại bản đồ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                    )

                    // Hiển thị 4 lựa chọn theo hàng ngang
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        mapStyles.forEach { styleItem ->
                            StyleOptionItem(
                                item = styleItem,
                                isSelected = currentStyleUri == styleItem.styleUri,
                                onClick = {
                                    currentStyleUri = styleItem.styleUri
                                    showStyleSheet = false // Đóng bảng sau khi chọn
                                }
                            )
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
*/

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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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

data class MapStyleItem(
    val name: String,
    val styleUri: String,
    val icon: ImageVector
)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
fun MapScreen(permissionsGranted: Boolean) {
    val context = LocalContext.current
    val START_POINT = Point.fromLngLat(105.8544, 21.0285)
    val MARKER_ICON_ID = "DESTINATION_PIN_ICON"

    // State Variables
    var puckBearingSource by remember { mutableStateOf(PuckBearing.HEADING) }
    var currentStyleUri by remember { mutableStateOf(Style.MAPBOX_STREETS) }
    var showStyleSheet by remember { mutableStateOf(false) }
    var isFirstLocate by remember { mutableStateOf(true) }
    var userLocationPoint by remember { mutableStateOf<Point?>(null) }
    var selectedDestination by remember { mutableStateOf<Point?>(null) }

    // --- STATE CHO NAVIGATION UI ---
    var isNavigating by remember { mutableStateOf(false) }
    var navigationState by remember { mutableStateOf(NavigationState()) }

    // --- 1. KHỞI TẠO CÁC FORMATTER RIÊNG BIỆT (FIX LỖI UNRESOLVED REFERENCE) ---
    val distanceFormatterOptions = remember { DistanceFormatterOptions.Builder(context).build() }

    // Formatter khoảng cách (VD: 5.2 km)
    val distanceRemainingFormatter = remember { DistanceRemainingFormatter(distanceFormatterOptions) }

    // Formatter thời gian (VD: 15 min)
    val timeRemainingFormatter = remember { TimeRemainingFormatter(context) }

    // API lấy thông tin Maneuver (rẽ trái/phải)
    val maneuverApi = remember { MapboxManeuverApi(MapboxDistanceFormatter(distanceFormatterOptions)) }

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
            if (selectedDestination != null) {
                UserMarker(point = selectedDestination!!, iconId = MARKER_ICON_ID)
            }

            if (permissionsGranted) {
                MapEffect(currentStyleUri) { mapView ->
                    mapView.mapboxMap.getStyle { style ->
                        if (style.getStyleImage(MARKER_ICON_ID) == null) {
                            val bitmap = bitmapFromDrawableRes(context, R.drawable.location)
                            if (bitmap != null) style.addImage(MARKER_ICON_ID, bitmap)
                        }
                    }
                }

                RouteRenderer(mapboxNavigation, routeLineApi, routeLineView)

                // Xử lý Click (Chỉ cho phép chọn điểm mới khi KHÔNG dẫn đường)
                if (!isNavigating) {
                    MapEffect(Unit) { mapView ->
                        mapView.mapboxMap.addOnMapClickListener { destination ->
                            selectedDestination = destination
                            requestCyclingRoute(context, mapboxNavigation, userLocationPoint, destination)
                            Toast.makeText(context, "Đã chọn điểm đến", Toast.LENGTH_SHORT).show()
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
            }
        }

        // --- LAYER UI DẪN ĐƯỜNG (Overlay) ---
        if (isNavigating) {
            TurnByTurnOverlay(
                navState = navigationState,
                onCancelNavigation = {
                    isNavigating = false
                    selectedDestination = null
                    mapboxNavigation.setNavigationRoutes(emptyList())
                }
            )
        }
        // --- UI CŨ (Nút bấm, Style Selector) ---
        else if (permissionsGranted) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp),
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
                    .padding(32.dp)
            ) {
                Icon(
                    if (puckBearingSource == PuckBearing.HEADING) Icons.Default.KeyboardArrowUp else Icons.Default.LocationOn,
                    "La bàn"
                )
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