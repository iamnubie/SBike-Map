package com.example.sbikemap.presentation

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.provider.Settings
import android.net.Uri
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionWrapper() {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    when {
        // 1. Quyền đã được cấp: Hiển thị bản đồ
        locationPermissionsState.allPermissionsGranted -> {
            MapScreen(permissionsGranted = true)
        }

        // 2. Cần giải thích (hoặc là lần đầu xin quyền, hoặc bị từ chối lần 1):
        // (Chúng ta gộp logic lần đầu xin quyền vào đây)
        locationPermissionsState.shouldShowRationale -> {
            // Hiển thị giao diện giải thích lý do cần quyền
            PermissionRationaleScreen(
                onPermissionRequested = {
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            )
        }

        // 3. Bị từ chối vĩnh viễn (hoặc lần đầu người dùng từ chối dứt khoát)
        // Nếu không thỏa mãn (1) và (2), chúng ta giả định đây là trạng thái cần hành động thủ công.
        else -> {
            // Hiển thị thông báo yêu cầu người dùng vào Settings để cấp quyền thủ công.
            PermissionDeniedScreen()
        }
    }
}

@Composable
fun PermissionRationaleScreen(onPermissionRequested: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp) // Giới hạn chiều rộng trên thiết bị lớn
                .padding(horizontal = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Thêm bóng đổ nhẹ
        ) {
            Column(
                modifier = Modifier.padding(24.dp), // Padding bên trong Card
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Yêu cầu Cấp Quyền Vị trí",
                    style = MaterialTheme.typography.headlineSmall, // Tiêu đề lớn
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Ứng dụng cần quyền vị trí để hiển thị các trạm xe đạp gần bạn và theo dõi hành trình của bạn trên bản đồ.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = onPermissionRequested,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text("Tiếp tục và Cấp quyền")
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(horizontal = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quyền Vị trí bị Từ chối",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.error // Tô màu lỗi cho tiêu đề
                )

                Text(
                    text = "Ứng dụng không thể hoạt động mà không có quyền vị trí. Vui lòng cấp quyền thủ công.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Mở Cài đặt Ứng dụng
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Mở Cài đặt Ứng dụng")
                }
            }
        }
    }
}