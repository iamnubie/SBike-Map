package com.example.sbikemap.presentation.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbikemap.data.remote.AuthApi
import com.example.sbikemap.data.remote.TripApi
import com.example.sbikemap.data.remote.models.TripHistoryItem
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authApi: AuthApi, // Dùng để update cân nặng
    private val tripApi: TripApi  // Dùng để lấy lịch sử đi
) : ViewModel() {

    // State lưu cân nặng (Hiển thị lên UI)
    var userWeight by mutableStateOf(0.0)

    // State lưu danh sách lịch sử
    var tripHistory by mutableStateOf<List<TripHistoryItem>>(emptyList())

    // Hàm gọi API cập nhật cân nặng
    fun saveUserWeight(weight: Double, context: Context) {
        viewModelScope.launch {
            try {
                // Tạo body JSON: { "weight": 65.5 }
                val body = mapOf("weight" to weight)

                val response = authApi.updateUserPhysicalStats(body)

                if (response.isSuccessful) {
                    userWeight = weight
                    Toast.makeText(context, "Đã lưu thể trạng!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Lỗi server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val response = authApi.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    // Cập nhật State cân nặng -> UI sẽ tự nhảy số
                    userWeight = user.weight ?: 0.0
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Hàm lấy lịch sử chuyến đi
    fun fetchTripHistory() {
        viewModelScope.launch {
            try {
                val response = tripApi.getTripHistory()
                if (response.isSuccessful && response.body() != null) {
                    tripHistory = response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}