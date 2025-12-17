package com.example.sbikemap.data.remote

import com.example.sbikemap.data.remote.models.AuthRequest
import com.example.sbikemap.data.remote.models.AuthResponse
import com.example.sbikemap.data.remote.models.UpdateUserRequest
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/firebase-login")
    suspend fun firebaseLogin(@Body request: AuthRequest): AuthResponse

    @POST("/auth/logout")
    // Không cần Body, chỉ cần Header Authorization. Trả về Response body (thường là thành công)
    suspend fun logout(): Any // Có thể đổi Any thành lớp LogoutResponse nếu Backend trả về cấu trúc JSON cụ thể

    // Thêm vào interface AuthApi
    @PATCH("users/profile/me")
    suspend fun updateProfile(@Body request: UpdateUserRequest): AuthResponse // Hoặc trả về User object tùy backend
}