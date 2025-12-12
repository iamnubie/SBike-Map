package com.example.sbikemap.data.remote

import com.example.sbikemap.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1. Lấy Access Token đã lưu
        val accessToken = tokenManager.getAccessToken()

        if (accessToken.isNullOrEmpty()) {
            // 2. Nếu không có token, tiếp tục request gốc (ví dụ: request login)
            return chain.proceed(originalRequest)
        }

        // 3. Xây dựng request mới với Header Authorization
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        // 4. Tiếp tục chuỗi request
        return chain.proceed(newRequest)
    }
}