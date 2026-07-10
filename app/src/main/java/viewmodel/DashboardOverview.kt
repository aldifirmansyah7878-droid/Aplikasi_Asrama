package com.example.aplikasi_asrama

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Locale

data class DashboardOverview(
    @SerializedName("totalPenghuni")
    val totalPenghuni: Int = 0,

    @SerializedName("totalKamar")
    val totalKamar: Int = 0,

    @SerializedName("kamarTersedia")
    val kamarTersedia: Int = 0,

    @SerializedName("totalPendapatan")
    val totalPendapatan: Long = 0L
) {
    fun getFormattedPendapatan(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(totalPendapatan)
    }

    fun isValid(): Boolean {
        return totalPenghuni > 0 || totalKamar > 0 || kamarTersedia > 0 || totalPendapatan > 0L
    }
}