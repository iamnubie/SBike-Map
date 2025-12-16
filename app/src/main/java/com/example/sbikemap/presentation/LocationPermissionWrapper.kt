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
import android.os.Build
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.remember

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionWrapper(navController: androidx.navigation.NavController) {
    val permissionsToRequest = remember {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // Chỉ thêm quyền thông báo nếu là Android 13 (Tiramisu) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list
    }

    // 2. Khởi tạo state với danh sách quyền đã tạo ở trên
    val permissionState = rememberMultiplePermissionsState(
        permissions = permissionsToRequest
    )

    when {
        // Tất cả quyền đã được cấp -> Vào màn hình bản đồ
        permissionState.allPermissionsGranted -> {
            MapScreen(permissionsGranted = true, navController = navController)
        }

        // Cần hiện lời giải thích
        permissionState.shouldShowRationale -> {
            PermissionRationaleScreen(
                onPermissionRequested = {
                    permissionState.launchMultiplePermissionRequest()
                }
            )
        }

        // Trường hợp còn lại: Chưa xin lần nào hoặc bị từ chối
        // (Lưu ý: Logic mặc định của Accompanist ban đầu sẽ rơi vào đây để xin quyền lần đầu)
        else -> {
            // Nếu chưa xin quyền lần nào thì xin luôn, còn nếu bị từ chối vĩnh viễn thì hiện màn hình lỗi
            if (!permissionState.allPermissionsGranted && !permissionState.shouldShowRationale) {
                // Một chút thủ thuật: Để tránh hiện màn hình lỗi ngay lập tức khi mới mở app,
                // ta kiểm tra xem danh sách quyền bị từ chối có rỗng không (tức là chưa request bao giờ)
                // Tuy nhiên để đơn giản cho flow của bạn, ta có thể hiển thị RationaleScreen ở lần đầu tiên luôn.
                PermissionRationaleScreen(
                    onPermissionRequested = {
                        permissionState.launchMultiplePermissionRequest()
                    }
                )
            } else {
                PermissionDeniedScreen()
            }
        }
    }
}

@Composable
fun PermissionRationaleScreen(onPermissionRequested: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
                    text = "Cấp Quyền Ứng Dụng",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Cập nhật nội dung văn bản
                Text(
                    text = "Để sử dụng tính năng dẫn đường, ứng dụng cần quyền truy cập:\n\n" +
                            "1. Vị trí: Để hiển thị bạn trên bản đồ.\n" +
                            "2. Thông báo: Để hiển thị hướng dẫn khi bạn tắt màn hình.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = onPermissionRequested,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text("Tiếp tục")
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
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
                    text = "Thiếu Quyền Quan Trọng",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Bạn đã từ chối quyền Vị trí hoặc Thông báo. Ứng dụng không thể dẫn đường chính xác nếu thiếu các quyền này.\n\nVui lòng vào Cài đặt để cấp lại.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Mở Cài đặt")
                }
            }
        }
    }
}