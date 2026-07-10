package com.example.aplikasi_asrama.api.model

import com.google.gson.annotations.SerializedName

data class PembayaranData(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("penghuni_id") val penghuniId: Int = 0,
    @SerializedName("namaPenghuni") val namaPenghuni: String? = null,
    @SerializedName("nomorKamar") val nomorKamar: String? = null,
    @SerializedName("bulanTahun") val bulanTahun: String = "",
    @SerializedName("jumlah") val jumlah: Int = 0,
    @SerializedName("tanggalTagihan") val tanggalTagihan: String? = null,
    @SerializedName("STATUS") val status: String = "",
    @SerializedName("tanggalBayar") val tanggalBayar: String? = null,
    @SerializedName("buktiPembayaranUri") val buktiUrl: String? = null,
    @SerializedName("metodePembayaran") val metode: String? = null,
    @SerializedName("tanggapanAdmin") val tanggapanAdmin: String? = null,
    @SerializedName("tanggalVerifikasi") val tanggalVerifikasi: String? = null,
    @SerializedName("adminVerifier") val adminVerifier: String? = null,
    @SerializedName("catatanAdmin") val catatanAdmin: String? = null
)

data class PembayaranResponse(
    val status: String,
    val data: List<PembayaranData>? = emptyList()
)