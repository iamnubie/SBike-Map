package com.example.sbikemap.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Data Class để lưu trạng thái dẫn đường (gọn nhẹ)
data class NavigationState(
    val maneuverIcon: ImageVector = Icons.Default.ArrowForward, // Icon rẽ (tạm thời dùng icon mặc định)
    val instruction: String = "Đang tải lộ trình...", // "Rẽ trái vào đường..."
    val distanceToTurn: String = "", // "300m"
    val timeRemaining: String = "", // "15 phút"
    val distanceRemaining: String = "" // "5.2 km"
)

// 2. UI chính: Overlay hiển thị trên bản đồ
@Composable
fun TurnByTurnOverlay(
    navState: NavigationState,
    onCancelNavigation: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- PHẦN TRÊN: HƯỚNG DẪN RẼ (TOP BANNER) ---
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 32.dp), // Né thanh trạng thái
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5)), // Màu xanh Google Maps
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon hướng rẽ
                Icon(
                    imageVector = navState.maneuverIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    // Khoảng cách đến chỗ rẽ
                    Text(
                        text = navState.instruction,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    // Hướng dẫn chữ (e.g., "Rẽ phải vào Trần Duy Hưng")
                    Text(
                        text = navState.distanceToTurn,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2
                    )
                }
            }
        }

        // --- PHẦN DƯỚI: THÔNG TIN CHUYẾN ĐI & NÚT HỦY (BOTTOM PANEL) ---
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Thời gian dự kiến
                    Text(
                        text = navState.timeRemaining,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF00C853), // Màu xanh lá
                        fontWeight = FontWeight.Bold
                    )
                    // Khoảng cách còn lại
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = navState.distanceRemaining,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "•", color = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Đến nơi", color = Color.Gray)
                    }
                }

                // Nút Hủy dẫn đường (Dấu X đỏ)
                IconButton(
                    onClick = onCancelNavigation,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Thoát",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

// COMPOSABLE RIÊNG CHO BẢNG ĐIỀU KHIỂN DƯỚI ĐÁY
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavigationBottomPanel(
    navState: NavigationState,
    destinationName: String,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding() // Tránh thanh vuốt Home ảo
        ) {
            // 1. Thông tin Tuyến đường (Giờ & Mét)
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thời gian (To, Xanh lá)
                Text(
                    text = navState.timeRemaining.ifEmpty { "--" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F9D58) // Google Green
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Khoảng cách (Nhỏ hơn)
                Text(
                    text = "(${navState.distanceRemaining.ifEmpty { "--" }})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Địa điểm đến
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = destinationName.ifEmpty { "Điểm đến đã chọn" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. NÚT ĐA NĂNG (HỦY / LƯU)
            // Thay vì dùng Button thường, ta dùng Box + combinedClickable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)) // Bo góc
                    .background(Color(0xFFD32F2F))   // Màu đỏ
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            // Xử lý Click ngắn -> HỦY
                            onCancel()
                        },
                        onLongClick = {
                            // Xử lý Nhấn giữ -> LƯU
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Rung nhẹ báo hiệu
                            onSave()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Dòng chính
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dừng chuyến đi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    // Dòng phụ hướng dẫn
                    Text(
                        text = "(Nhấn giữ để Lưu lịch sử)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}