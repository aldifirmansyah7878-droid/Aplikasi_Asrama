package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String? = null,
    @SerializedName("nama_lengkap") val namaLengkap: String? = null,
    @SerializedName("role") val role: String? = null
)
data class UserListResponse(
    val status: String,
    val data: List<UserData>
)