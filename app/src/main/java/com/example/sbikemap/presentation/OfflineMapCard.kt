package com.example.sbikemap.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sbikemap.utils.OfflineUtils
import com.mapbox.navigation.core.MapboxNavigation

@Composable
fun OfflineMapCard(
    context: android.content.Context,
    mapboxNavigation: MapboxNavigation
) {
    // State quản lý trạng thái
    var isDownloaded by remember { mutableStateOf(false) }
    var mapSize by remember { mutableStateOf(0L) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }

    // Kiểm tra trạng thái ngay khi mở màn hình
    LaunchedEffect(Unit) {
        OfflineUtils.checkOfflineRegionStatus(context) { downloaded, size ->
            isDownloaded = downloaded
            mapSize = size
        }
    }

    // Hàm format bytes sang MB
    fun formatSize(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format("%.1f MB", mb)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)), // Màu nền xám xanh nhạt
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = if (isDownloaded) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Bản đồ Offline (Q12)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = if (isDownloading) "Đang tải xuống..."
                        else if (isDownloaded) "Đã sẵn sàng sử dụng"
                        else "Chưa tải dữ liệu",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Section: Dung lượng & Progress
            if (isDownloading) {
                LinearProgressIndicator(
                    progress = { downloadProgress / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                )
                Text(
                    text = "$downloadProgress%",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            } else {
                // Hiển thị dung lượng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dung lượng",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = if (mapSize > 0) formatSize(mapSize) else "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    // Nút Hành động (Tải hoặc Xóa)
                    if (isDownloaded) {
                        OutlinedButton(
                            onClick = {
                                OfflineUtils.removeOfflineRegion(context) {
                                    isDownloaded = false
                                    mapSize = 0L
                                    Toast.makeText(context, "Đã xóa bản đồ Offline", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Xóa")
                        }
                    } else {
                        Button(
                            onClick = {
                                isDownloading = true
                                OfflineUtils.downloadOfflineRegion(
                                    context,
                                    mapboxNavigation,
                                    onProgress = { progress -> downloadProgress = progress },
                                    onComplete = { size ->
                                        isDownloading = false
                                        isDownloaded = true
                                        mapSize = size
                                        Toast.makeText(context, "Tải thành công!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        isDownloading = false
                                        Toast.makeText(context, "Lỗi: $error", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            enabled = !isDownloading
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tải về")
                        }
                    }
                }
            }
        }
    }
}