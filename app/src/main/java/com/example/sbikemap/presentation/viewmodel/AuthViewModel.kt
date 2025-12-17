package com.example.sbikemap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbikemap.App
import com.example.sbikemap.data.remote.AppContainer
import com.example.sbikemap.data.remote.models.AuthResponse
import com.example.sbikemap.data.remote.models.UpdateUserRequest
import com.example.sbikemap.data.repository.AuthRepository
import com.example.sbikemap.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()             // Trạng thái ban đầu
    object Loading : LoginState()          // Đang xử lý
    data class Success(val destination: String) : LoginState() // Thành công, chuyển đến Home
    data class Error(val message: String) : LoginState()    // Lỗi (Firebase hoặc Backend)
    object FirebaseSuccess : LoginState()   // Chỉ mới thành công trên Firebase
}

class AuthViewModel : ViewModel() {

    private val repository: AuthRepository = App.container.authRepository
    private val tokenManager: TokenManager = App.container.tokenManager

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val response: AuthResponse) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // Hàm gọi từ View để kích hoạt Login
    fun handleFirebaseLogin(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // 1. Gọi Backend API bằng Firebase ID Token
                val response = repository.loginWithFirebaseToken(idToken)

                // 2. Lưu token vào SharedPrefs/DataStore
                tokenManager.saveAuthData(
                    response.accessToken,
                    response.email,
                    response.username
                )

                _authState.value = AuthState.Success(response)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Lỗi kết nối Backend")
            }
        }
    }

    fun logout() {
        // Xóa Access Token khỏi Shared Preferences
        viewModelScope.launch {

            // 1. Tùy chọn: Gọi API backend để hủy Refresh Token
            try {
                repository.logoutUser()
            } catch (e: Exception) {
                // Nếu lỗi mạng (ví dụ: mất kết nối), ta vẫn tiếp tục xóa token cục bộ
                println("Lỗi gọi API Logout Backend: ${e.message}")
            } finally {
                // 2. Xóa Access Token và Email khỏi Shared Preferences
                tokenManager.clearAuthData()
            }
        }
    }
    fun getLoggedInUserEmail(): String {
        return tokenManager.getEmail() ?: "Người dùng chưa đăng nhập"
    }
    fun getLoggedInUserName(): String {
        return tokenManager.getName() ?: "Người dùng SBike"
    }

    fun handleFirebaseRegister(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // Backend sẽ tự động kiểm tra UID, tạo User mới trong DB
                repository.loginWithFirebaseToken(idToken) // Tái sử dụng loginWithFirebaseToken để tạo hồ sơ
                _authState.value = AuthState.Idle // Hoặc tạo trạng thái RegisterSuccess
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Lỗi Backend khi đăng ký: ${e.message}")
            }
        }
    }

    // Hàm update tên
    fun updateUserName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Gọi API Backend
                val request = UpdateUserRequest(username = newName)
                // Lưu ý: repository cần thêm hàm updateProfile gọi sang api.updateProfile(request)
                repository.updateUserProfile(request)

                // 2. Nếu API không lỗi -> Lưu tên mới vào máy
                tokenManager.saveUserName(newName)

                // 3. Báo về UI thành công
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi cập nhật tên")
            }
        }
    }

    // Hàm reset trạng thái
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}