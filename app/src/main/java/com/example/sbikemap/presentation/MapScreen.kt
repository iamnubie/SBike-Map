package com.example.sbikemap.presentation

/*import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember

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

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(15.0)
            center(START_POINT)
            pitch(0.0)
            bearing(0.0)
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
            if (permissionsGranted) {
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

                    // Cập nhật Camera theo dõi
                    mapViewportState.transitionToFollowPuckState(
                        followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                            .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                            .zoom(16.0)
                            .build()
                    )
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

/*import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener

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

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(10.0)
            center(START_POINT)
            pitch(0.0)
            bearing(0.0)
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
            if (permissionsGranted) {
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
                                // 1. Ngắt listener ngay lập tức để không chạy lại
                                mapView.location.removeOnIndicatorPositionChangedListener(this)

                                // 2. "Nhảy" ngay lập tức tới vị trí người dùng (không animation)
                                // Đặt zoom mức 10 (nhìn thấy quận/huyện)
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
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.sbikemap.R
import com.example.sbikemap.utils.UserMarker
import com.example.sbikemap.utils.bitmapFromDrawableRes
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener

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
                // 1. Effect nạp ảnh vào Style (Chạy mỗi khi đổi kiểu bản đồ)
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
                                // 1. Ngắt listener ngay lập tức để không chạy lại
                                mapView.location.removeOnIndicatorPositionChangedListener(this)

                                // Cập nhật vị trí để vẽ Marker
                                userLocationPoint = point

                                // 2. "Nhảy" ngay lập tức tới vị trí người dùng
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
}