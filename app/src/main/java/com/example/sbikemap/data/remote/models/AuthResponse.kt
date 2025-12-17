package com.example.sbikemap.data.remote.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val email: String,
    @SerializedName("username")
    val username: String,
    val expiresIn: Int,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    val expiresInRefresh: Int
)