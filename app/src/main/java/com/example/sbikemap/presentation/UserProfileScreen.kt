package com.example.sbikemap.presentation

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

    // Lấy Tên hiển thị (Display Name)
    val firebaseUser = Firebase.auth.currentUser
    val displayName = viewModel.getLoggedInUserName()
    // Ưu tiên lấy từ Firebase Auth, nếu không có thì lấy phần đầu email (trước @)
//    val displayName = firebaseUser?.displayName?.takeIf { !it.isNullOrBlank() }
//        ?: userEmail.substringBefore("@")

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
                onEditClick = { showEditDialog = true }
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
    onEditClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
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