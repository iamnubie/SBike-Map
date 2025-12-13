package com.example.sbikemap.presentation
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.mapbox.geojson.Point
//import com.mapbox.maps.extension.compose.MapboxMap
//import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
//
//@Composable
//fun MapScreen() {
//    // 1. Tạo và nhớ trạng thái viewport của bản đồ
//    val mapViewportState = rememberMapViewportState {
//        setCameraOptions {
//            // Thiết lập chế độ xem ban đầu:
//            // - Zoom cấp độ 2.0 (tầm nhìn toàn cầu)
//            // - Trung tâm là một điểm ở Đà Nẵng (kinh độ 108.2772, vĩ độ 16.0544)
//            zoom(2.0)
//            center(Point.fromLngLat(108.2772, 16.0544))
//            pitch(0.0)
//            bearing(0.0)
//        }
//    }
//
//    // 2. Composable chính để hiển thị bản đồ
//    MapboxMap(
//        // Sử dụng Modifier.fillMaxSize() để bản đồ chiếm toàn bộ không gian
//        Modifier.fillMaxSize(),
//        mapViewportState = mapViewportState,
//        // Bạn có thể thêm các tham số khác ở đây như styleUri (phong cách bản đồ),
//        // onMapReady, v.v.
//    )
//}

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import androidx.compose.runtime.LaunchedEffect
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions

@Composable
fun MapScreen() {
    // 1. Tọa độ mẫu (Chuyển từ Đà Nẵng đến Hà Nội)
    val START_POINT = Point.fromLngLat(105.8544, 21.0285) // Hà Nội (Vị trí ban đầu)
    val END_POINT = Point.fromLngLat(106.618600, 10.865540)   // HCM (Vị trí đích)

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            // Thiết lập chế độ xem ban đầu (Đà Nẵng, zoom 9.0)
            zoom(9.0)
            center(START_POINT)
            pitch(0.0)
            bearing(0.0)
        }
    }

    // 2. HIỆU ỨNG CHUYỂN ĐỘNG FLY-TO
    LaunchedEffect(Unit) {
        // Chỉ chạy một lần sau khi Composable được tạo (Unit key)

        // Tạo tùy chọn camera đích
        val targetCameraOptions = CameraOptions.Builder()
            .center(END_POINT) // Vị trí đích
            .zoom(16.0)        // Zoom đích (gần hơn)
            .pitch(45.0)       // Góc nghiêng để tạo hiệu ứng 3D đẹp hơn
            .build()

        val animationOptions = MapAnimationOptions.Builder()
            .duration(6000L) // Sử dụng hàm duration()
            .build()
        // Kích hoạt chuyển động "bay" trong 6 giây (6000ms)
        mapViewportState.flyTo(
            cameraOptions = targetCameraOptions,
            animationOptions = animationOptions
        )
    }

    // 3. Composable chính để hiển thị bản đồ
    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        style = { MapStyle(style = Style.STANDARD)}
        //style = { MapStyle(style = "mapbox://styles/your-mapbox-username/your-custom-style-url") }
    )
}