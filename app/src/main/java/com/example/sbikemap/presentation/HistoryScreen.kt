package com.example.sbikemap.presentation

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sbikemap.data.remote.models.TripHistoryItem
import com.example.sbikemap.presentation.viewmodel.ProfileViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    // Gọi API lấy dữ liệu mỗi khi vào màn hình này
    LaunchedEffect(Unit) {
        profileViewModel.fetchTripHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử chuyến đi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa các item
        ) {
            if (profileViewModel.tripHistory.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chưa có chuyến đi nào.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(profileViewModel.tripHistory) { trip ->
                    TripHistoryCard(trip)
                }
            }
        }
    }
}

@Composable
fun TripHistoryCard(trip: TripHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Ngày giờ
            Text(
                text = formatDate(trip.startTime),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Từ -> Đến
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.originName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = Color.Black
                )
            }
            // Đường kẻ nối
            Box(modifier = Modifier.padding(start = 7.dp).height(12.dp).width(2.dp).background(Color.LightGray))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.destinationName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            // Thông số
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TripStat("Quãng đường", "${String.format("%.1f", trip.distanceMeters / 1000)} km")
                TripStat("Thời gian", formatDurationHistory(trip.durationSeconds))
                val caloText = if (trip.caloriesBurned > 0) {
                    "${trip.caloriesBurned.toInt()} kcal"
                } else {
                    "Chưa có TT"
                }
                TripStat("Calo", caloText)
            }
        }
    }
}

@Composable
fun TripStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

// Hàm helper format
fun formatDate(isoString: String): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val parsed = ZonedDateTime.parse(isoString)
            parsed.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))
        } else {
            isoString.take(10)
        }
    } catch (e: Exception) {
        isoString
    }
}

fun formatDurationHistory(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    return if (minutes < 60) "$minutes phút" else "${minutes/60}h ${minutes%60}p"
}