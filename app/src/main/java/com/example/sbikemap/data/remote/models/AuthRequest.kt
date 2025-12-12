package com.example.sbikemap.data.remote.models

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    // Tên trường phải khớp với trường mà Backend mong đợi (idToken)
    @SerializedName("idToken")
    val idToken: String
)