import androidx.compose.runtime.Composable
import com.example.sbikemap.presentation.viewmodel.MapViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
fun MapLocationHandler(
    mapViewModel: MapViewModel,
    mapViewportState: MapViewportState,
    puckBearingSource: PuckBearing
) {
    // Sử dụng MapEffect để lắng nghe thay đổi của nguồn la bàn (Heading/Course)
    MapEffect(puckBearingSource) { mapView ->
        // 1. Cấu hình Puck (Chấm xanh vị trí)
        mapView.location.updateSettings {
            enabled = true
            locationPuck = createDefault2DPuck(withBearing = true)
            pulsingEnabled = true
            puckBearing = puckBearingSource
            puckBearingEnabled = true
        }

        // 2. Logic Zoom
        if (mapViewModel.isFirstLocate) {
            // TRƯỜNG HỢP 1: Lần đầu vào App -> Zoom từ xa vào gần
            val listener = object : OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    // Hủy lắng nghe ngay sau khi lấy được vị trí đầu tiên
                    mapView.location.removeOnIndicatorPositionChangedListener(this)

                    // Lưu vị trí user vào VM
                    mapViewModel.userLocationPoint = point

                    // Đặt Camera ở mức zoom 10 (xa) trước
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder().center(point).zoom(10.0).build()
                    )

                    // Sau đó chuyển hiệu ứng bay vào zoom 16 (gần) + Bám theo puck
                    mapViewportState.transitionToFollowPuckState(
                        FollowPuckViewportStateOptions.Builder()
                            .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                            .zoom(16.0)
                            .build()
                    )

                    // [QUAN TRỌNG] Đánh dấu đã zoom xong.
                    // Biến này nằm trong ViewModel nên sẽ không bị reset khi đổi màn hình.
                    mapViewModel.isFirstLocate = false
                }
            }
            mapView.location.addOnIndicatorPositionChangedListener(listener)
        } else {
            // TRƯỜNG HỢP 2: Quay lại từ màn hình khác -> Chỉ cần bám theo puck (Không zoom lại)
            // Nếu bạn muốn giữ nguyên mức zoom hiện tại của người dùng thì bỏ dòng .zoom(16.0) đi
            mapViewportState.transitionToFollowPuckState(
                FollowPuckViewportStateOptions.Builder()
                    .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                    .zoom(16.0) // Có thể bỏ dòng này nếu muốn giữ mức zoom cũ
                    .build()
            )
        }
    }
}