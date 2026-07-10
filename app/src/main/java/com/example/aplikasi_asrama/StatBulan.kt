package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class StatBulan(
    @SerializedName("bulan") val bulan: String,
    @SerializedName("total") val total: Int
)