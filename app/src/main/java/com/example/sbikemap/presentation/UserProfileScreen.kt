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
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
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

    DisposableEffect(Unit) {
        onDispose {
            MapboxNavigationProvider.destroy()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phần Avatar và Tên
            UserInfoSection(
                name = currentDisplayName,
                email = userEmail,
                avatarUrl = currentAvatarUrl,
                onEditClick = { showEditDialog = true },
                onAvatarChange = { newUri ->
                    // Xử lý khi người dùng chọn ảnh xong
                    Toast.makeText(context, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()

                    // Gọi ViewModel để upload
                    viewModel.uploadAvatar(context, newUri) { newUrl ->
                        // Khi upload thành công (Callback onSuccess):
                        currentAvatarUrl = newUrl // Cập nhật State -> UI tự đổi ảnh mới
                        Toast.makeText(context, "Đổi ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Danh sách chức năng
            HorizontalDivider()

            ProfileOptionItem(
                icon = Icons.Default.Settings,
                title = "Cài đặt chung",
                subtitle = "Ngôn ngữ, Giao diện"
            ) {
                // TODO: Navigate to general settings
            }

            ProfileOptionItem(
                icon = Icons.Default.ExitToApp,
                title = "Tải bản đồ Offline (Q12)",
                subtitle = "Tải dữ liệu bản đồ & dẫn đường",
                iconTint = MaterialTheme.colorScheme.primary,
                iconModifier = Modifier.rotate(90f)
            ) {
                OfflineUtils.downloadOfflineRegion(context, mapboxNavigation)
            }

            ProfileOptionItem(
                icon = Icons.Default.Delete,
                title = "Xóa bản đồ Offline (Q12)",
                subtitle = "Giải phóng bộ nhớ máy",
                iconTint = Color.Red
            ) {
                OfflineUtils.removeOfflineRegion(context)
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. Nút Đăng xuất
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