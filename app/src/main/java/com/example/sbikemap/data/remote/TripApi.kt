package com.example.sbikemap.data.remote

import com.example.sbikemap.data.remote.models.CreateTripRequest
import com.example.sbikemap.data.remote.models.TripHistoryItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TripApi {

    // Endpoint lưu chuyến đi mới
    @POST("trips/save")
    suspend fun saveTrip(@Body request: CreateTripRequest): Response<Any>

    // Endpoint lấy lịch sử chuyến đi
    @GET("trips/history")
    suspend fun getTripHistory(): Response<List<TripHistoryItem>>
}