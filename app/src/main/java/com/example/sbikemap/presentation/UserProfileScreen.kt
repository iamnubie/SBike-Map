package com.example.sbikemap.presentation

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sbikemap.presentation.viewmodel.AuthViewModel
import com.example.sbikemap.utils.OfflineUtils
import com.mapbox.common.TileStore
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.options.RoutingTilesOptions
import com.mapbox.navigation.core.MapboxNavigationProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import android.Manifest
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.sbikemap.data.remote.models.TripHistoryItem
import com.example.sbikemap.presentation.viewmodel.ProfileViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.sbikemap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel
) {
    val context = LocalContext.current
    // State lưu tên hiển thị trên UI (Khởi tạo từ ViewModel)
    var currentDisplayName by remember { mutableStateOf(viewModel.getLoggedInUserName()) }
    // Lấy Email từ ViewModel (Giống HomeScreen)
    val userEmail = viewModel.getLoggedInUserEmail()
    // State điều khiển Dialog sửa tên
    var showEditDialog by remember { mutableStateOf(false) }
    // State lưu URL ảnh đại diện (Lấy từ bộ nhớ khi mở app)
    var currentAvatarUrl by remember { mutableStateOf(viewModel.getAvatarUrl()) }

    // Lấy Tên hiển thị (Display Name)
    val firebaseUser = Firebase.auth.currentUser
    val displayName = viewModel.getLoggedInUserName()

    // State nhập cân nặng
    var weightInput by remember { mutableStateOf("") }
    // 1. Load lịch sử & cân nặng khi mở màn hình
    LaunchedEffect(Unit) {
        profileViewModel.fetchTripHistory()
        profileViewModel.fetchUserProfile()
    }

    // 2. Cập nhật ô nhập khi có dữ liệu cân nặng từ ViewModel
    LaunchedEffect(profileViewModel.userWeight) {
        if (profileViewModel.userWeight > 0) {
            val weightText = profileViewModel.userWeight.toString()
            weightInput = if (weightText.endsWith(".0")) {
                weightText.substringBefore(".0")
            } else {
                weightText
            }
        }
    }

    val mapboxNavigation = remember {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            val tileStore = TileStore.create()
            val routingTilesOptions = RoutingTilesOptions.Builder()
                .tileStore(tileStore)
                .build()
            val navOptions = NavigationOptions.Builder(context)
                .routingTilesOptions(routingTilesOptions)
                .build()
            MapboxNavigationProvider.create(navOptions)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // THÔNG TIN USER (AVATAR, TÊN)
            item {
                UserInfoSection(
                    name = currentDisplayName,
                    email = userEmail,
                    avatarUrl = currentAvatarUrl,
                    onEditClick = { showEditDialog = true },
                    onAvatarChange = { newUri ->
                        Toast.makeText(context, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()
                        viewModel.uploadAvatar(context, newUri) { newUrl ->
                            currentAvatarUrl = newUrl
                            Toast.makeText(context, "Đổi ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // CẬP NHẬT THỂ TRẠNG (CÂN NẶNG)
            item {
                WeightInputCard(
                    weightInput = weightInput,
                    onWeightChange = { newValue -> weightInput = newValue },
                    currentSavedWeight = profileViewModel.userWeight,
                    onSave = { newWeight ->
                        // Gọi ViewModel xử lý lưu
                        profileViewModel.saveUserWeight(newWeight, context)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }

            // PHẦN 3: CÁC TÙY CHỌN (Offline Map)
            item {
                ProfileOptionItem(Icons.Default.Settings, "Cài đặt chung", "Ngôn ngữ, Giao diện") {}

                ProfileOptionItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Tải bản đồ Offline (Q12)",
                    subtitle = "Tải dữ liệu bản đồ & dẫn đường",
                    iconModifier = Modifier.rotate(90f)
                ) { OfflineUtils.downloadOfflineRegion(context, mapboxNavigation) }

                ProfileOptionItem(
                    icon = Icons.Default.Delete,
                    title = "Xóa bản đồ Offline (Q12)",
                    subtitle = "Giải phóng bộ nhớ máy",
                    iconTint = Color.Red
                ) { OfflineUtils.removeOfflineRegion(context) }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // PHẦN 4: LỊCH SỬ CHUYẾN ĐI
            item {
                Text(
                    text = "Lịch sử chuyến đi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            // Render danh sách chuyến đi từ ProfileViewModel
            if (profileViewModel.tripHistory.isEmpty()) {
                item {
                    Text(
                        text = "Chưa có chuyến đi nào.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(profileViewModel.tripHistory) { trip ->
                    TripHistoryCard(trip)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // PHẦN 5: ĐĂNG XUẤT (Cuối cùng)
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        viewModel.logout()
                        Firebase.auth.signOut()
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng xuất", color = Color.Red)
                }
                Spacer(modifier = Modifier.height(32.dp)) // Padding đáy
            }
        }
        // [LOGIC DIALOG SỬA TÊN]
        if (showEditDialog) {
            EditNameDialog(
                currentName = currentDisplayName,
                onDismiss = { showEditDialog = false },
                onConfirm = { newName ->
                    // Gọi ViewModel cập nhật
                    viewModel.updateUserName(
                        newName = newName,
                        onSuccess = {
                            // Cập nhật UI ngay lập tức
                            currentDisplayName = newName
                            showEditDialog = false
                            Toast.makeText(context, "Đổi tên thành công!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun UserInfoSection(
    name: String,
    email: String,
    avatarUrl: String?,
    onEditClick: () -> Unit,
    onAvatarChange: (Uri) -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    // 1. Launcher chọn ảnh (Photo Picker - Chuẩn mới của Android)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onAvatarChange(it) }
    }

    // 2. Launcher xin quyền (cho các máy đời cũ)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Nếu được cấp quyền -> Mở Photo Picker
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            Toast.makeText(context, "Cần quyền truy cập ảnh để đổi Avatar", Toast.LENGTH_SHORT).show()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Avatar
        Box(
            modifier = Modifier.wrapContentSize() // Kích thước vừa đủ ôm lấy nội dung
        ) {
            // LỚP 1: AVATAR CHÍNH (Bị cắt hình tròn)
            Box(
                modifier = Modifier
                    .size(120.dp) // Tăng kích thước lên chút cho đẹp
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        // Sự kiện chạm vào ảnh để đổi
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .memoryCachePolicy(CachePolicy.DISABLED) // Tắt cache bộ nhớ (để luôn load ảnh mới nhất khi vừa đổi)
                            .diskCachePolicy(CachePolicy.ENABLED)  // Vẫn giữ cache ổ cứng cho lần sau mở app
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop, // Quan trọng: crop ảnh để lấp đầy hình tròn
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // LỚP 2: ICON MÁY ẢNH NHỎ (Nằm đè lên trên góc)
            // Vì đặt sau trong code nên nó sẽ vẽ đè lên trên Avatar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Căn xuống góc dưới phải của BOX TỔNG
                    .size(36.dp) // Kích thước vòng tròn trắng chứa icon
                    // Tạo viền trùng màu nền để tạo hiệu ứng "cắt" giống Instagram
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable( // Cũng cho phép bấm vào icon nhỏ này để đổi ảnh
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                    .padding(6.dp) // Padding bên trong để icon không bị sát viền
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Đổi ảnh",
                    tint = Color.Gray,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Row chứa Tên và Nút sửa
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center, // Căn giữa cả hàng
            modifier = Modifier.fillMaxWidth()
        ) {
            // Việc này giúp cân bằng trái phải, đẩy Text vào chính giữa màn hình
            Box(modifier = Modifier.size(48.dp))

            // 2. Tên User
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                // Thêm các thuộc tính này để xử lý nếu tên quá dài
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false) // fill=false để text chỉ chiếm chỗ nó cần, không giãn hết mức
            )

            // 3. Nút Edit bên phải
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Sửa tên",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi tên hiển thị") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Tên mới") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) onConfirm(text)
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = iconModifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
                Text(text = trip.originName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            // Đường kẻ nối
            Box(modifier = Modifier.padding(start = 7.dp).height(12.dp).width(2.dp).background(Color.LightGray))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = trip.destinationName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
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
                    "Chưa có TT" // Hoặc "N/A", "--"
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

@Composable
fun WeightInputCard(
    weightInput: String,
    onWeightChange: (String) -> Unit,
    currentSavedWeight: Double,
    onSave: (Double) -> Unit
) {
    val context = LocalContext.current
    val value = weightInput.toDoubleOrNull()
    val isValid = value != null && value > 0
    val isChanged = isValid && value != currentSavedWeight
    val isErrorTooHigh = value != null && value > 150

    // 1. Lấy controller để quản lý bàn phím và focus
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Title
            Text(
                text = "Cân nặng (Dùng để tính Calo)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )

            // Input + Button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Input box
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = {
                        if (it.length <= 5 &&
                            it.count { c -> c == '.' } <= 1 &&
                            it.all { c -> c.isDigit() || c == '.' }
                        ) {
                            onWeightChange(it)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = {
                        Text(
                            text = "Cân nặng (kg)",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    },
                    isError = isErrorTooHigh,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = Color.Red,
                        errorCursorColor = Color.Red
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Save button
                Button(
                    onClick = {
                        if (value != null && value > 150) {
                            Toast.makeText(context, "Cân nặng tối đa cho phép là 150kg!", Toast.LENGTH_SHORT).show()
                        } else {
                            value?.let {
                                onSave(it)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        }
                    },
                    enabled = isChanged,
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Lưu",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Hàm format ngày giờ
fun formatDate(isoString: String): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val parsed = ZonedDateTime.parse(isoString)
            parsed.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))
        } else {
            isoString.take(10) // Fallback cho Android cũ
        }
    } catch (e: Exception) {
        isoString
    }
}

// Hàm format thời gian (riêng cho History)
fun formatDurationHistory(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    return if (minutes < 60) "$minutes phút" else "${minutes/60}h ${minutes%60}p"
}