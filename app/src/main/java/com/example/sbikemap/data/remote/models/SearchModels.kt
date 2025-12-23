package com.example.sbikemap.data.remote.models

import com.google.gson.annotations.SerializedName

// Hứng toàn bộ object địa điểm từ Backend
data class PlaceResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("location") val location: PlaceLocation,
    @SerializedName("score") val score: Double // Độ chính xác của AI
)

// Dữ liệu gửi lên để lưu (Save)
data class SavePlaceRequest(
    val name: String,
    val address: String?,
    val category: String?, // Mapbox có trả về category
    val lat: Double,
    val lng: Double
)

// Dữ liệu nhận về khi tìm thông minh
data class SmartPlaceResponse(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String?,
    @SerializedName("location") val location: PlaceLocation,
    @SerializedName("category") val category: String?,
    @SerializedName("score") val score: Double?
)

data class PlaceLocation(
    @SerializedName("type") val type: String = "Point",
    @SerializedName("coordinates") val coordinates: List<Double>
    // Lưu ý: Backend trả về [Longitude, Latitude] (Kinh độ trước)
)