package com.example.sbikemap.data.remote.models

import com.google.gson.annotations.SerializedName

// Dùng để hứng dữ liệu lịch sử từ Server về (GET)
data class TripHistoryItem(
    @SerializedName("_id") val _id: String, // Map với field "_id" của MongoDB
    val originName: String,
    val destinationName: String,
    val startTime: String,
    val durationSeconds: Double,
    val distanceMeters: Double,
    val caloriesBurned: Double
)

// Dùng để gửi dữ liệu chuyến đi mới lên Server (POST)
data class CreateTripRequest(
    val originName: String,
    val destinationName: String,
    val startTime: String,
    val durationSeconds: Double,
    val distanceMeters: Double,
    val userWeightSnapshot: Double,
    val caloriesBurned: Double
)