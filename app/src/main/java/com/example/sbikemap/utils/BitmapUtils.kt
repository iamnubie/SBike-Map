package com.example.sbikemap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation

// --- 1. COMPOSABLE HIỂN THỊ CON DẤU (MARKER) ---
@Composable
fun UserMarker(
    point: Point,
    iconId: String,
    onClick: () -> Unit = {}
) {
    PointAnnotation(
        point = point,
        onClick = {
            onClick()
            true
        }
    ) {
        // Cấu hình trong block lambda này
        iconImage = IconImage(iconId) // Tham chiếu đến ảnh đã add trong Style
        iconSize = 0.1     // Kích thước
        iconAnchor = com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor.BOTTOM // (Tùy chọn) Neo icon ở đáy
    }
}

// --- 2. HÀM TIỆN ÍCH CHUYỂN ĐỔI VECTOR -> BITMAP ---
fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, resourceId) ?: return null

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}