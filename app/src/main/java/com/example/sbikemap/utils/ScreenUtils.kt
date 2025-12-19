package com.example.sbikemap.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        // 1. Lưu lại trạng thái xoay ban đầu
        val originalOrientation = activity.requestedOrientation
        // 2. Khóa màn hình theo hướng mong muốn (VD: Portrait)
        activity.requestedOrientation = orientation

        // 3. Khôi phục lại trạng thái cũ khi thoát khỏi màn hình này
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}

// Hàm tìm Activity từ Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}