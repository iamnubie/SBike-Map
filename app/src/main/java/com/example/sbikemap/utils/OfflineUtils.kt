package com.example.sbikemap.utils

import android.content.Context
import android.widget.Toast
import com.mapbox.bindgen.Value
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.Style
import com.mapbox.maps.TilesetDescriptorOptions
import com.mapbox.navigation.core.MapboxNavigation

object OfflineUtils {
    // Tọa độ bounding box bao quanh phường Trung Mỹ Tây, Quận 12
//    private val TRUNG_MY_TAY_POLYGON: Polygon = Polygon.fromLngLats(
//        listOf(listOf(
//            Point.fromLngLat(106.6080, 10.8480),
//            Point.fromLngLat(106.6250, 10.8480),
//            Point.fromLngLat(106.6250, 10.8650),
//            Point.fromLngLat(106.6080, 10.8650),
//            Point.fromLngLat(106.6080, 10.8480)
//        ))
//    )
    private val QUAN_12_POLYGON: Polygon = Polygon.fromLngLats(
        listOf(listOf(
            // Góc Tây Nam (gần KCN Tân Bình/Trường Chinh)
            Point.fromLngLat(106.6160, 10.8200),

            // Góc Đông Nam (gần Sông Sài Gòn/An Phú Đông)
            Point.fromLngLat(106.7150, 10.8200),

            // Góc Đông Bắc (gần Thạnh Lộc/Giáp Bình Dương)
            Point.fromLngLat(106.7150, 10.9000),

            // Góc Tây Bắc (gần Hiệp Thành/Hóc Môn)
            Point.fromLngLat(106.6160, 10.9000),

            // Đóng vòng lặp (về lại điểm đầu)
            Point.fromLngLat(106.6160, 10.8200)
        ))
    )

//    private const val TILE_REGION_ID = "quan12_trungmytay"
    private const val TILE_REGION_ID = "quan12_full_region"

    fun downloadOfflineRegion(
        context: Context,
        mapboxNavigation: MapboxNavigation,
        onProgress: (Int) -> Unit,
        onComplete: (Long) -> Unit,
        onError: (String) -> Unit
    ) {

        // 1. Cấu hình TileStore
        val tileStore = TileStore.create().also {
            // Dùng chuỗi trực tiếp "mapbox_access_token" thay vì hằng số gây lỗi
            it.setOption(
                "mapbox_access_token",
                Value(context.getString(com.example.sbikemap.R.string.mapbox_access_token))
            )
            // Nếu vượt quá, Mapbox tự xóa dữ liệu cũ ít dùng đi
            it.setOption(
                TileStoreOptions.DISK_QUOTA,
//                Value(500L * 1024 * 1024) // 500 MB
                Value(1024L * 1024 * 1024)
            )
        }

        // 2. Tạo Descriptor cho BẢN ĐỒ (Hình ảnh)
        val offlineManager = OfflineManager()
        val mapsTilesetDescriptor = offlineManager.createTilesetDescriptor(
            TilesetDescriptorOptions.Builder()
                .styleURI(Style.MAPBOX_STREETS)
                .minZoom(0)
                .maxZoom(16)
                .build()
        )

        // 3. [QUAN TRỌNG] Tạo Descriptor cho DẪN ĐƯỜNG (Routing Data)
        // Lấy phiên bản data mới nhất từ MapboxNavigation
        val navTilesetDescriptor = mapboxNavigation.tilesetDescriptorFactory.getLatest()

        // 4. Cấu hình Load Options
        val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
//            .geometry(TRUNG_MY_TAY_POLYGON) // Truyền Polygon trực tiếp vào đây
            .geometry(QUAN_12_POLYGON)
            .descriptors(listOf(mapsTilesetDescriptor, navTilesetDescriptor)) // Gộp cả Map và Nav
            .acceptExpired(true)
            .build()

        Toast.makeText(context, "Đang tải dữ liệu Offline...", Toast.LENGTH_SHORT).show()

        // 5. Tiến hành tải
        tileStore.loadTileRegion(
            TILE_REGION_ID,
            tileRegionLoadOptions,
            { progress ->
                // completedResourceCount / requiredResourceCount
                val percent = if (progress.requiredResourceCount > 0) {
                    (progress.completedResourceCount.toFloat() / progress.requiredResourceCount.toFloat() * 100).toInt()
                } else 0
                onProgress(percent)
            }
        ) { result ->
            if (result.isValue) {
                // Lấy dung lượng sau khi tải xong
                val size = result.value?.completedResourceSize ?: 0L
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onComplete(size)
                }
            } else {
                val error = result.error
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onError(error?.message ?: "Lỗi không xác định")
                }
            }
        }
    }
    // Hàm xóa dữ liệu Offline
    fun removeOfflineRegion(context: Context, onSuccess: () -> Unit) {
        // 1. Lấy TileStore (cấu hình y hệt lúc tải)
        val tileStore = TileStore.create().also {
            it.setOption(
                "mapbox_access_token",
                Value(context.getString(com.example.sbikemap.R.string.mapbox_access_token))
            )
        }

        // 2. Gọi lệnh xóa theo ID
        tileStore.removeTileRegion(TILE_REGION_ID)
        onSuccess()

        // 3. Thông báo cho người dùng
        Toast.makeText(context, "Đã xóa dữ liệu Offline: Quận 12", Toast.LENGTH_SHORT).show()

        // Lưu ý: Mapbox không có callback trả về cho hàm removeTileRegion trong một số phiên bản,
        // nó thực hiện bất đồng bộ nhưng rất nhanh.
    }
    // Hàm kiểm tra trạng thái và dung lượng gói Offline
    fun checkOfflineRegionStatus(context: Context, onResult: (Boolean, Long) -> Unit) {
        val tileStore = TileStore.create().also {
            it.setOption(
                "mapbox_access_token",
                Value(context.getString(com.example.sbikemap.R.string.mapbox_access_token))
            )
        }

        // Kiểm tra xem gói dữ liệu có tồn tại không
        tileStore.getTileRegion(TILE_REGION_ID) { expected ->
            if (expected.isValue) {
                val region = expected.value
                // Nếu region != null nghĩa là đã tải
                if (region != null) {
                    // Trả về true và dung lượng (bytes)
                    onResult(true, region.completedResourceSize)
                } else {
                    onResult(false, 0L)
                }
            } else {
                // Lỗi hoặc chưa có -> coi như chưa tải
                onResult(false, 0L)
            }
        }
    }
}