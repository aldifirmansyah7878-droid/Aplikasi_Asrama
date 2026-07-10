package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class NotifikasiData(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("judul") val judul: String,
    @SerializedName("pesan") val pesan: String,
    @SerializedName("type") val type: String,
    @SerializedName("reference_id") val referenceId: Int? = null,
    @SerializedName("is_read") val isRead: Int = 0,
    @SerializedName("created_at") val createdAt: String
)

data class NotifikasiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<NotifikasiData>?
)