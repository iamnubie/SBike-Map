package com.example.sbikemap.data.repository

import com.example.sbikemap.data.remote.AuthApi
import com.example.sbikemap.data.remote.models.AuthRequest
import com.example.sbikemap.data.remote.models.AuthResponse
import com.example.sbikemap.data.remote.models.UpdateUserRequest
import okhttp3.MultipartBody
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi
) {
    suspend fun loginWithFirebaseToken(idToken: String): AuthResponse {
        // Gọi API Backend của bạn bằng Firebase ID Token
        val request = AuthRequest(idToken)
        return authApi.firebaseLogin(request)
    }

    // Hủy hiệu lực Refresh Token trên Backend
    suspend fun logoutUser() {
        // Gọi hàm API. Retrofit sẽ tự động thêm Authorization Header (Bearer Token)
        // nếu đã cấu hình OkHttpClient Interceptor (Rất quan trọng cho các request đã bảo vệ)
        authApi.logout()
    }

    // Hàm Cập nhật Profile (Sửa tên)
    suspend fun updateUserProfile(request: UpdateUserRequest): AuthResponse {
        // Gọi sang AuthApi -> Gửi Request PATCH lên Server
        return authApi.updateProfile(request)
    }

    suspend fun uploadAvatar(image: MultipartBody.Part): Map<String, String> {
        return authApi.uploadAvatar(image)
    }
}