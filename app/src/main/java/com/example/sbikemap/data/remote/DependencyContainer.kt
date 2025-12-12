package com.example.sbikemap.data.remote

import com.example.sbikemap.data.remote.AuthApi
import com.example.sbikemap.data.repository.AuthRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.example.sbikemap.utils.TokenManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// Đối tượng đơn giản để quản lý dependencies
//object AppContainer {
//    private const val BASE_URL = "http://10.0.2.2:3000"
//
//    // 1. Khởi tạo Retrofit
//    private val retrofit: Retrofit = Retrofit.Builder()
//        .baseUrl(BASE_URL)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    // 2. Khởi tạo AuthApi
//    private val authApi: AuthApi by lazy {
//        retrofit.create(AuthApi::class.java)
//    }
//
//    // 3. Khởi tạo AuthRepository
//    val authRepository: AuthRepository by lazy {
//        AuthRepository(authApi)
//    }
//}

class AppContainer(private val applicationContext: Context) {

    private val BASE_URL = "http://10.0.2.2:3000"

    // 1. Khởi tạo TokenManager
    val tokenManager: TokenManager by lazy {
        TokenManager(applicationContext)
    }

    // 2. Khởi tạo OkHttpClient (với Interceptor)
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            //  Thêm AuthInterceptor
            .addInterceptor(AuthInterceptor(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // 3. Khởi tạo Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) //  Gắn OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 4. Khởi tạo AuthApi
    private val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    // 5. Khởi tạo AuthRepository
    val authRepository: AuthRepository by lazy {
        AuthRepository(authApi)
    }
}