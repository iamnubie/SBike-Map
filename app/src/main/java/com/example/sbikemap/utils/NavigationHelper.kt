package com.example.sbikemap.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMapScope
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions

// 1. Hàm tách biệt logic gọi API tìm đường
fun requestCyclingRoute(
    context: Context,
    mapboxNavigation: MapboxNavigation,
    origin: Point?,
    destination: Point,
    onRoutesFound: (List<NavigationRoute>) -> Unit = { }
) {
    if (origin == null) {
        Toast.makeText(context, "Đang lấy vị trí của bạn...", Toast.LENGTH_SHORT).show()
        return
    }

    mapboxNavigation.requestRoutes(
        RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .profile(DirectionsCriteria.PROFILE_CYCLING) // Chế độ xe đạp
            .enableRefresh(false) // Tắt refresh để tránh lỗi xe đạp
            .alternatives(true)
            .layersList(emptyList()) // Tắt layer ô tô
            .annotationsList(
                listOf(
                    DirectionsCriteria.ANNOTATION_DISTANCE,
                    DirectionsCriteria.ANNOTATION_DURATION,
                    DirectionsCriteria.ANNOTATION_SPEED
                )
            )
            .coordinatesList(listOf(origin, destination))
            .build(),
        object : NavigationRouterCallback {
            override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {}
            override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                val isNetworkError = reasons.any {
                    it.message.contains("INTERNET_DISCONNECTED") || it.type.toString() == "NETWORK_ERROR"
                }
                val errorMessage = if (isNetworkError) {
                    "Vui lòng kiểm tra kết nối Internet."
                } else {
                    "Không tìm thấy đường phù hợp (Lỗi: ${reasons.firstOrNull()?.message})."
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }

            override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                android.util.Log.d("MapboxRoute", "Tìm thấy ${routes.size} tuyến đường")
                mapboxNavigation.setNavigationRoutes(routes)

                // [ĐÃ SỬA] Truy cập vào 'directionsRoute' để lấy thông tin
                if (routes.isNotEmpty()) {
                    onRoutesFound(routes)
                }
            }
        }
    )
}

// 2. Composable quản lý việc vẽ tuyến đường (Route Renderer)
// Tách logic lắng nghe RoutesObserver và vẽ Line ra khỏi màn hình chính
@Composable
fun MapboxMapScope.RouteRenderer(
    mapboxNavigation: MapboxNavigation,
    routeLineApi: MapboxRouteLineApi,
    routeLineView: MapboxRouteLineView
) {
    MapEffect(Unit) { mapView ->
        val routesObserver = RoutesObserver { routeUpdate ->
            if (routeUpdate.navigationRoutes.isNotEmpty()) {
                routeLineApi.setNavigationRoutes(routeUpdate.navigationRoutes) { drawData ->
                    mapView.mapboxMap.getStyle { style ->
                        routeLineView.renderRouteDrawData(style, drawData)
                    }
                }
            } else {
                routeLineApi.setNavigationRoutes(emptyList()) { drawData ->
                    mapView.mapboxMap.getStyle { style ->
                        routeLineView.renderRouteDrawData(style, drawData)
                    }
                }
            }
        }

        // Đăng ký và Hủy đăng ký tự động
        mapboxNavigation.registerRoutesObserver(routesObserver)
    }
}