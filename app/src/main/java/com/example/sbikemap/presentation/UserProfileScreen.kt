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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.sbikemap.R
import com.mapbox.navigation.core.MapboxNavigation

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

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7))) { // Màu nền xám nhạt hiện đại

        // 1. HEADER CONG MÀU XANH (Nền trên cùng)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // 2. TOP BAR (Trong suốt để đè lên nền xanh)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Hồ sơ cá nhân",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. NỘI DUNG CHÍNH (Cuộn được)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // AVATAR & INFO (Nổi lên trên nền xanh)
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    UserInfoCard(
                        name = currentDisplayName,
                        email = userEmail,
                        avatarUrl = currentAvatarUrl,
                        onEditClick = { showEditDialog = true },
                        onAvatarChange = { newUri ->
                            Toast.makeText(context, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()
                            viewModel.uploadAvatar(context, newUri) { newUrl ->
                                currentAvatarUrl = newUrl
                                Toast.makeText(context, "Đổi ảnh thành công!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- KHỐI: SỨC KHỎE (Cân nặng) ---
                item {
                    SectionTitle("Chỉ số sức khỏe")
                    WeightInputCard(
                        weightInput = weightInput,
                        onWeightChange = { newValue -> weightInput = newValue },
                        currentSavedWeight = profileViewModel.userWeight,
                        onSave = { newWeight ->
                            profileViewModel.saveUserWeight(newWeight, context)
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- KHỐI: TIỆN ÍCH (Lịch sử, Map Offline) ---
                item {
                    SectionTitle("Tiện ích")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column {
                            ProfileOptionItem(
                                icon = Icons.Default.DateRange,
                                title = "Lịch sử chuyến đi",
                                subtitle = "Xem lại hành trình đã qua",
                                onClick = { navController.navigate("history_screen") }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

                            // Nhúng Card Offline Map vào đây nhưng bỏ viền để liền mạch
                            Box(modifier = Modifier.padding(horizontal = 0.dp)) {
                                OfflineMapCard(context = context, mapboxNavigation = mapboxNavigation)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- KHỐI: CÀI ĐẶT KHÁC (Đăng xuất) ---
                item {
                    SectionTitle("Hệ thống")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column {
                            // Nút Đăng xuất
                            Surface(
                                onClick = {
                                    viewModel.logout()
                                    Firebase.auth.signOut()
                                    navController.navigate("login") { popUpTo("profile") { inclusive = true } }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFFFEBEE), CircleShape), // Nền đỏ nhạt
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ExitToApp, null, tint = Color.Red)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Đăng xuất",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Red,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // Dialog
        if (showEditDialog) {
            EditNameDialog(
                currentName = currentDisplayName,
                onDismiss = { showEditDialog = false },
                onConfirm = { newName ->
                    viewModel.updateUserName(newName, {
                        currentDisplayName = newName;
                        showEditDialog = false;
                        Toast.makeText(context, "Đổi tên thành công!", Toast.LENGTH_SHORT).show() },
                        { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                    )
                }
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF475467),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun UserInfoCard(
    name: String,
    email: String,
    avatarUrl: String?,
    onEditClick: () -> Unit,
    onAvatarChange: (Uri) -> Unit
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) {
        uri -> uri?.let { onAvatarChange(it) }
    }
    val avatarInteractionSource = remember { MutableInteractionSource() }
    val editIconInteractionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)), // Đổ bóng
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Avatar với viền
            Box {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(4.dp, Color.White, CircleShape) // Viền trắng
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable(
                            interactionSource = avatarInteractionSource,
                            indication = null
                        ) { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNullOrEmpty()) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                    } else {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).diskCachePolicy(CachePolicy.ENABLED).memoryCachePolicy(CachePolicy.DISABLED).build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                // Icon Edit Camera nhỏ
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(
                            interactionSource = editIconInteractionSource,
                            indication = null
                        ) { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tên và Email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp).padding(start = 8.dp)) {
                    Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667085)
            )
        }
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
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF2F4F7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF101828)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF667085)
                )
            }
            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
        }
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
