package com.example.aplikasi_asrama

import com.google.gson.annotations.SerializedName

data class KamarData(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nomorKamar") val nomorKamar: String = "",
    @SerializedName("lantai") val lantai: Int = 0,
    @SerializedName("kapasitas") val kapasitas: Int = 0,
    @SerializedName("terisi") val terisi: Int = 0,
    @SerializedName("hargaPerBulan") val hargaPerBulan: Int = 0,
    @SerializedName("fasilitas") val fasilitas: String? = null,
    @SerializedName("status") val status: String = "Tersedia",
    @SerializedName("tanggal") val tanggal: String? = null
)