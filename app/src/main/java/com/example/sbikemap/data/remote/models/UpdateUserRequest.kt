package com.example.sbikemap.data.remote.models

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("username")
    val username: String
)