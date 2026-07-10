package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class DashboardOverviewResponse(
    @SerializedName("totalPenghuni") val totalPenghuni: Int,
    @SerializedName("totalKamar") val totalKamar: Int,
    @SerializedName("kamarTersedia") val kamarTersedia: Int,
    @SerializedName("totalPendapatan") val totalPendapatan: Int
)