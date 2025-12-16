package com.example.sbikemap.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sbikemap.utils.RouteWeatherPoint
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
// [QUAN TRỌNG] Import các extension function này để fix lỗi Unresolved reference
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.viewAnnotationOptions

@Composable
fun WeatherRouteMarker(
    data: RouteWeatherPoint
) {
    // Mapbox ViewAnnotation cho phép render Composable trực tiếp lên Map
    ViewAnnotation(
        options = viewAnnotationOptions {
            geometry(data.point)

            annotationAnchor {
                anchor(ViewAnnotationAnchor.CENTER)
            }

            allowOverlap(true) // Cho phép đè lên đường vẽ
        }
    ) {
        val weather = data.weather
        val iconUrl = "https://openweathermap.org/img/wn/${weather.weather.firstOrNull()?.icon}@2x.png"
        // Lấy description thay vì main, và viết hoa chữ cái đầu cho đẹp
        val rawDescription = weather.weather.firstOrNull()?.description ?: ""
        val displayDescription = rawDescription.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        // Thiết kế UI giống trong ảnh (Rounded Box, Shadow)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Cột nhiệt độ & Thời gian (hoặc mô tả)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${weather.main.temp.toInt()}°",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = displayDescription,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Icon thời tiết
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}