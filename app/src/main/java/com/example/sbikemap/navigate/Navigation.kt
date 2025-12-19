package com.example.sbikemap.navigate

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sbikemap.data.remote.AppContainer
import com.example.sbikemap.presentation.HistoryScreen
import com.example.sbikemap.presentation.HomeScreen
import com.example.sbikemap.presentation.LocationPermissionWrapper
import com.example.sbikemap.presentation.LoginScreen
import com.example.sbikemap.presentation.MapScreen
import com.example.sbikemap.presentation.PlanScreen
import com.example.sbikemap.presentation.SignupScreen
import com.example.sbikemap.presentation.UserProfileScreen
import com.example.sbikemap.presentation.viewmodel.AuthViewModel
import com.example.sbikemap.presentation.viewmodel.MapViewModel
import com.example.sbikemap.presentation.viewmodel.ProfileViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun Navigate(
    container: AppContainer
){
    val navController = rememberNavController()
    val startDest = if (Firebase.auth.currentUser != null) {
        "map_route" // Đã đăng nhập -> Vào thẳng Map
    } else {
        "login"     // Chưa đăng nhập -> Vào màn Login
    }
    NavHost(navController, startDestination = startDest) {
        composable("login") {
            // AuthViewModel cần Repository và TokenManager
            val authViewModel: AuthViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return AuthViewModel() as T
                    }
                }
            )
            LoginScreen(navController, authViewModel)
        }
        composable("signup") { SignupScreen(navController)}
        composable("home") { HomeScreen(navController)}
        composable("map_route") {
            // MapViewModel cần TripApi
            val mapViewModel: MapViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MapViewModel(
                            tripApi = container.tripApi, // Truyền TripApi vào đây
                            authApi = container.authApi
                        ) as T
                    }
                }
            )

            // Bạn cần sửa LocationPermissionWrapper để nhận mapViewModel
            // và truyền nó cho MapScreen bên trong
            LocationPermissionWrapper(navController, mapViewModel)
        }
        composable("profile") {
            // [CẬP NHẬT] ProfileViewModel cần AuthApi và TripApi
            val profileViewModel: ProfileViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ProfileViewModel(
                            authApi = container.authApi,
                            tripApi = container.tripApi
                        ) as T
                    }
                }
            )

            UserProfileScreen(
                navController = navController,
                // authViewModel có thể dùng lại cách khởi tạo mặc định nếu nó tự lấy dependencies
                profileViewModel = profileViewModel
            )
        }
        composable("history_screen") {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ProfileViewModel(
                            authApi = container.authApi,
                            tripApi = container.tripApi
                        ) as T
                    }
                }
            )

            HistoryScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
        composable("plan_screen") {
            // Lấy dữ liệu từ màn hình trước đó gửi sang
            val result = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("ai_plan_result")
                ?: "Không có dữ liệu hành trình."

            PlanScreen(
                navController = navController,
                initialPlanContent = result
            )
        }
    }
}