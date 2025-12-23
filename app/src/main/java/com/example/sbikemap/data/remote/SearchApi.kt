package com.example.sbikemap.data.remote

import com.example.sbikemap.data.remote.models.SavePlaceRequest
import com.example.sbikemap.data.remote.models.SmartPlaceResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SearchApi {
    @GET("search/smart")
    suspend fun smartSearch(@Query("q") query: String): List<SmartPlaceResponse>

    @POST("search/save")
    suspend fun savePlace(@Body request: SavePlaceRequest): Any
}