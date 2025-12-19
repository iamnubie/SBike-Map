package com.example.sbikemap.data.remote.models

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("_id")
    val id: String,
    val username: String,
    val email: String,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("weight")
    val weight: Double? = null
)