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
    private val TRUNG_MY_TAY_POLYGON: Polygon = Polygon.fromLngLats(
        listOf(listOf(
            Point.fromLngLat(106.6080, 10.8480),
            Point.fromLngLat(106.6250, 10.8480),
            Point.fromLngLat(106.6250, 10.8650),
            Point.fromLngLat(106.6080, 10.8650),
            Point.fromLngLat(106.6080, 10.8480)
        ))
    )

    private const val TILE_REGION_ID = "quan12_trungmytay"

    fun downloadOfflineRegion(context: Context, mapboxNavigation: MapboxNavigation) {

        // 1. Cấu hình TileStore
        val tileStore = TileStore.create().also {
            // [SỬA LỖI] Dùng chuỗi trực tiếp "mapbox_access_token" thay vì hằng số gây lỗi
            it.setOption(
                "mapbox_access_token",
                Value(context.getString(com.example.sbikemap.R.string.mapbox_access_token))
            )
            // Nếu vượt quá, Mapbox tự xóa dữ liệu cũ ít dùng đi
            it.setOption(
                TileStoreOptions.DISK_QUOTA,
                Value(500L * 1024 * 1024) // 500 MB
            )
        }

        // 2. Tạo Descriptor cho BẢN ĐỒ (Hình ảnh)
        val offlineManager = OfflineManager()
        val mapsTilesetDescriptor = offlineManager.createTilesetDescriptor(
            TilesetDescriptorOptions.Builder()
                .styleURI(Style.MAPBOX_STREETS)
                .minZoom(0)
                .maxZoom(16)
                .build() // Không set geometry ở đây nữa
        )

        // 3. [QUAN TRỌNG] Tạo Descriptor cho DẪN ĐƯỜNG (Routing Data)
        // Lấy phiên bản data mới nhất từ MapboxNavigation
        val navTilesetDescriptor = mapboxNavigation.tilesetDescriptorFactory.getLatest()

        // 4. [SỬA LỖI 2] Cấu hình Load Options
        val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
            .geometry(TRUNG_MY_TAY_POLYGON) // [SỬA LỖI] Truyền Polygon trực tiếp vào đây
            .descriptors(listOf(mapsTilesetDescriptor, navTilesetDescriptor)) // Gộp cả Map và Nav
            .acceptExpired(true)
            .build()

        Toast.makeText(context, "Đang tải dữ liệu Offline...", Toast.LENGTH_SHORT).show()

        // 5. Tiến hành tải
        tileStore.loadTileRegion(
            TILE_REGION_ID,
            tileRegionLoadOptions,
            { progress ->
                // Có thể cập nhật UI % tại đây nếu cần
            }
        ) { result ->
            if (result.isValue) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "Tải Offline thành công (Map + Routing)!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val error = result.error
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "Lỗi: ${error?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    // Hàm xóa dữ liệu Offline
    fun removeOfflineRegion(context: Context) {
        // 1. Lấy TileStore (cấu hình y hệt lúc tải)
        val tileStore = TileStore.create().also {
            it.setOption(
                "mapbox_access_token",
                Value(context.getString(com.example.sbikemap.R.string.mapbox_access_token))
            )
        }

        // 2. Gọi lệnh xóa theo ID
        tileStore.removeTileRegion(TILE_REGION_ID)

        // 3. Thông báo cho người dùng
        Toast.makeText(context, "Đã xóa dữ liệu Offline: Quận 12", Toast.LENGTH_SHORT).show()

        // Lưu ý: Mapbox không có callback trả về cho hàm removeTileRegion trong một số phiên bản,
        // nó thực hiện bất đồng bộ nhưng rất nhanh.
    }
}